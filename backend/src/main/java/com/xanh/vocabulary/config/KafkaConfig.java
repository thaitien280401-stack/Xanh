package com.xanh.vocabulary.config;

import com.xanh.vocabulary.event.PomodoroCompletedEvent;
import com.xanh.vocabulary.event.QuizCompletedEvent;
import com.xanh.vocabulary.kafka.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public NewTopic quizCompletedTopic() {
        return TopicBuilder.name(KafkaTopics.QUIZ_COMPLETED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic pomodoroCompletedTopic() {
        return TopicBuilder.name(KafkaTopics.POMODORO_COMPLETED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic vocabProgressTopic() {
        return TopicBuilder.name(KafkaTopics.VOCAB_PROGRESS_UPDATED).partitions(3).replicas(1).build();
    }

    @Bean
    public ConsumerFactory<String, QuizCompletedEvent> quizCompletedConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerProps(),
                new StringDeserializer(),
                new JsonDeserializer<>(QuizCompletedEvent.class, false));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, QuizCompletedEvent> quizCompletedListenerFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, QuizCompletedEvent>();
        factory.setConsumerFactory(quizCompletedConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, PomodoroCompletedEvent> pomodoroCompletedConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerProps(),
                new StringDeserializer(),
                new JsonDeserializer<>(PomodoroCompletedEvent.class, false));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PomodoroCompletedEvent> pomodoroCompletedListenerFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, PomodoroCompletedEvent>();
        factory.setConsumerFactory(pomodoroCompletedConsumerFactory());
        return factory;
    }

    private Map<String, Object> consumerProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "leaderboard-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        return props;
    }
}
