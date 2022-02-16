package pl.lrozek.redis.stream.tracing.producer.controller;

import static java.util.Map.of;
import static org.springframework.data.redis.connection.stream.StreamRecords.mapBacked;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class RedisController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @GetMapping
    public String get(@RequestParam(name = "name", defaultValue = "John") String name) {
        log.info("incoming request with name: {}", name);
        MapRecord<String, String, String> record = mapBacked(of("name", name)).withStreamKey("mystream");
        redisTemplate.opsForStream().add(record);
        log.info("following stream entry added: {}", record);
        return name;
    }

}
