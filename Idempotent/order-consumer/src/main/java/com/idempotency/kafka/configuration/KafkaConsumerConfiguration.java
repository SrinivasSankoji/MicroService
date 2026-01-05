package com.idempotency.kafka.configuration;

import com.idempotency.kafka.dto.OrderProcessedEventDTO;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfiguration {

    @Value ("${spring.kafka.bootstrap-servers}")
    private String bootstrap;

    /**
     * Transactional producer used by the consumer to publish "order-processed".
     * This is what enables Kafka transactions.
     */
    @Bean
    public ProducerFactory<String, OrderProcessedEventDTO> transactionalProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);
        // recommended for reliability
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        DefaultKafkaProducerFactory<String, OrderProcessedEventDTO> pf = new DefaultKafkaProducerFactory<>(props);
        // Enables Kafka transactions (consume + produce + offset commit atomically in Kafka)
        pf.setTransactionIdPrefix("order-consumer-tx-");
        return pf;
    }

    // ------------------------------------------------------------------
    // 2) DLT producer (NON-transactional, used by error handler)
    // ------------------------------------------------------------------
    @Bean
    public ProducerFactory<Object, Object> dltProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);

        // reliability (optional but recommended)
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);

        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<Object, Object> dltKafkaTemplate(ProducerFactory<Object, Object> dltProducerFactory) {
        return new KafkaTemplate<>(dltProducerFactory);
    }

    // ------------------------------------------------------------------
    // 3) Error handler: retry N times + publish to DLT
    // ------------------------------------------------------------------
    @Bean
    public DefaultErrorHandler defaultErrorHandler(KafkaTemplate<Object, Object> dltKafkaTemplate) {

        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(dltKafkaTemplate, (record, ex) ->
                        // send to <originalTopic>.DLT, same partition
                        new TopicPartition(record.topic() + ".DLT", record.partition())
                );

        // Retry 3 times with 5 seconds backoff (total attempts = 1 + 3 = 4)
        FixedBackOff backOff = new FixedBackOff(5000L, 3);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);

        // Don't retry "bad data" problems; route directly to DLT
        errorHandler.addNotRetryableExceptions(
                org.springframework.kafka.support.serializer.DeserializationException.class,
                org.springframework.messaging.converter.MessageConversionException.class,
                IllegalArgumentException.class
        );

        return errorHandler;
    }


    @Bean
    public KafkaTemplate<String, OrderProcessedEventDTO> processedKafkaTemplate(
            ProducerFactory<String, OrderProcessedEventDTO> transactionalProducerFactory) {
        return new KafkaTemplate<>(transactionalProducerFactory);
    }

    @Bean
    public KafkaTransactionManager<String, OrderProcessedEventDTO> kafkaTransactionManager(
            ProducerFactory<String, OrderProcessedEventDTO> transactionalProducerFactory) {
        return new KafkaTransactionManager<>(transactionalProducerFactory);
    }

    /**
     * This connects the KafkaTransactionManager to the listener container.
     * Spring will:
     * begin Kafka tx -> call listener -> send -> commit offsets -> commit tx
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
            ConsumerFactory<Object, Object> consumerFactory,
            KafkaTransactionManager<String, OrderProcessedEventDTO> kafkaTransactionManager,
            DefaultErrorHandler defaultErrorHandler) {
        var factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        factory.getContainerProperties().setKafkaAwareTransactionManager(kafkaTransactionManager);
        // Bounded retries + DLT (prevents crash loop)
        factory.setCommonErrorHandler(defaultErrorHandler);
        return factory;
    }
}
