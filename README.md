# sample project for https://github.com/spring-cloud/spring-cloud-sleuth/issues/2117

### to build and run the application

```shell
docker-compose down -v && docker-compose up --build
```

### to invoke REST API

```shell
curl 127.0.0.1:8080
curl -v -H "x-b3-traceid: 9c3f113adff8d68d" -H "x-b3-spanid: 417003de9381fe54" 127.0.0.1:8080
```

### to see content of redis stream

```shell
docker-compose exec redis redis-cli XREAD STREAMS mystream ID 0 $
```

## current state

- when `REST API` is invoked it is clearly visible in the logs that trace and span ids (`7a1c6b3aa94d7cb5`) were added to the Slf4J MDC and they are visible in the logs in `producer` app

```
2022-02-17 10:46:24.521  INFO [producer,7a1c6b3aa94d7cb5,7a1c6b3aa94d7cb5] 1 --- [nio-8080-exec-3] p.l.r.s.t.p.controller.RedisController   : incoming request with name: John
2022-02-17 10:46:24.523  INFO [producer,7a1c6b3aa94d7cb5,7a1c6b3aa94d7cb5] 1 --- [nio-8080-exec-3] p.l.r.s.t.p.controller.RedisController   : following stream entry added: MapBackedRecord{recordId=*, kvMap={name=John}}
```

- when the stream entry was added to a stream via call `redisTemplate#opsForStream()#add(record)` there is a missing egress instrumentation to propagate trace context.
  This can be checked by reading all stream entries via `redic-cli`. In the below output only one key-value pair is present, ie `name` -> `John`

```shell
docker-compose exec redis redis-cli XREAD STREAMS mystream ID 0 $
1) 1) "mystream"
   2) 1) 1) "1645095058340-0"
         2) 1) "name"
            2) "John"
```

- when consumer receives stream entry, we can see in the logs that there is no trace concext `[consumer,,]`. The reason for that is missing ingress instrumentation, that could pick up propageted trace context, or establish a new one when missing

```
2022-02-17 10:47:58.344  INFO [consumer,,] 1 --- [cTaskExecutor-1] p.l.r.s.t.c.c.RedisListenerConfiguration : received following message MapBackedRecord{recordId=1645095058340-0, kvMap={name=John}}

```

## desired state

### Producer

- there should be an egress instrumentation to propagate trace context built in `RedisTemplate`
- since `redis streams` do not have any notion of headers the only way to achive it is via adding another key-value pair alongside data. The idea is something like that:

```shell
docker-compose exec redis redis-cli XADD mystream \* name John b3 7a1c6b3aa94d7cb5-7a1c6b3aa94d7cb5
```

`name->John` is application data. The egress instrumentation would add another key-value pair to propagate trance context `b3->7a1c6b3aa94d7cb5-7a1c6b3aa94d7cb5`

- when `b3` key-value pair is added we can see that consumer receives it:

```
2022-02-17 11:11:01.579  INFO [consumer,,] 1 --- [cTaskExecutor-1] p.l.r.s.t.c.c.RedisListenerConfiguration : received following message MapBackedRecord{recordId=1645096261573-0, kvMap={name=John, b3=7a1c6b3aa94d7cb5-7a1c6b3aa94d7cb5}}
```

### Consumer

- There should be ingress instrumentation built in `DefaultStreamMessageListenerContainer` that is used to run redis listeners to either:
  - start a new trace context when there is missing `b3` key-value pair
  - when `b3` key-value pair is used it should be picked up
- ingress instrumentation should also remove `b3` key-value pair before all the key-value pairs go through normal processing:
  - deserialization to `ObjectRecord`, ie application model class
  - deserialization to `MapRecord`, where application code receives `Map`

### Goal

- to achive functionality parity with Rabbitmq ingress and egress instrumentation
