package pl.lrozek.redis.stream.tracing.consumer.configuration;

import static org.springframework.data.redis.connection.stream.StreamOffset.fromStart;
import static org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class RedisListenerConfiguration {

    @Bean
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> listener(RedisConnectionFactory connectionFactory) {
        StreamMessageListenerContainer<String, MapRecord<String, String, String>> container = StreamMessageListenerContainer.create(connectionFactory, builder().build());
        container.receive(fromStart("mystream"), message -> log.info("received following message {}", message));
        container.start();
        return container;
    }
}
