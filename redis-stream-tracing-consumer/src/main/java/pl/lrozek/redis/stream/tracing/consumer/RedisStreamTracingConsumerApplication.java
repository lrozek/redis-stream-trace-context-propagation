package pl.lrozek.redis.stream.tracing.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RedisStreamTracingConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(RedisStreamTracingConsumerApplication.class, args);
	}

}
