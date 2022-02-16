package pl.lrozek.redis.stream.tracing.producer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RedisStreamTracingProducerApplication {

	public static void main(String[] args) {
		SpringApplication.run(RedisStreamTracingProducerApplication.class, args);
	}

}
