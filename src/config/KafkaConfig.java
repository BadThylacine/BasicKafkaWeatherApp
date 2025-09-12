package config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    private final String bootstrapServers = "localhost:9092";

    /**
     * Creates a new Kafka topic for weather data.
     * This method defines the topic configuration including name, partitions, and replication factor.
     *
     * @return NewTopic configured for weather data with:
     *         - Topic name: "weather"
     *         - Partitions: 1 (single partition for simple use case)
     *         - Replication factor: 1 (single broker setup)
     */
    @Bean
    public NewTopic weatherTopic() {
        String topicName = "weather";
        return new NewTopic(topicName, 1, (short) 1);
    }

    /**
     * Configures the Kafka producer factory with serialization and connection settings.
     * This factory creates Kafka producers that will send messages to Kafka brokers.
     *
     * @return ProducerFactory configured with:
     *         - Bootstrap servers: Kafka broker addresses
     *         - Key serializer: Converts message keys to bytes (String format)
     *         - Value serializer: Converts message values to bytes (String format)
     */
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }

    /**
     * Creates a KafkaTemplate for sending messages to Kafka topics.
     * This is the main Spring abstraction for producing messages to Kafka.
     * It provides convenient methods for sending messages synchronously or asynchronously.
     *
     * @return KafkaTemplate that uses the configured producer factory
     */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * Configures the Kafka consumer factory with deserialization and connection settings.
     * This factory creates Kafka consumers that will receive messages from Kafka topics.
     *
     * @return ConsumerFactory configured with:
     *         - Bootstrap servers: Kafka broker addresses
     *         - Consumer group ID: Groups consumers for load balancing
     *         - Key deserializer: Converts message keys from bytes to String
     *         - Value deserializer: Converts message values from bytes to String
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "weather-group");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(config);
    }

    /**
     * Creates a listener container factory for @KafkaListener annotated methods.
     * This factory manages the lifecycle of message listener containers and enables
     * concurrent processing of messages from Kafka topics.
     *
     * Features:
     * - Concurrent message processing (multiple threads)
     * - Automatic message acknowledgment
     * - Error handling and retry mechanisms
     * - Integration with Spring's @KafkaListener annotation
     *
     * @return ConcurrentKafkaListenerContainerFactory for handling incoming messages
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}