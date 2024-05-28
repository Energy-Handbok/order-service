package com.khaphp.orderservice.config;

import com.khaphp.orderservice.constant.TopicEventKafka;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        // Creating a Map
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");  //127.0.0.1:9092
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public NewTopic createTopic() {     //NewTopic là dùng của org.apache.kafka.clients.admin.NewTopic
        return new NewTopic(TopicEventKafka.UPDATE_WALLET_BALANCE.name(), 1, (short) 1); //tahy vì create topic = CMD, ta create bằng code
    }

    @Bean
    public NewTopic createTopic2() {
        return new NewTopic(TopicEventKafka.DELETE_TRANSACTION.name(), 1, (short) 1);
    }

    @Bean
    public NewTopic createTopic3() {
        return new NewTopic(TopicEventKafka.UPDATE_STOCK_FOOD.name(), 1, (short) 1);
    }
}
