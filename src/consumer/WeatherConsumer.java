package consumer;

import model.WeatherData;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherConsumer {

    private final ObjectMapper objectMapper;

    /**
     * Main Kafka message listener that processes incoming weather data messages.
     * This method is automatically triggered whenever a new message arrives on the weather topic.
     *
     * The @KafkaListener annotation configures:
     * - topics: Which Kafka topic(s) to listen to (configured via properties)
     * - groupId: Consumer group for load balancing and fault tolerance
     *
     * Message metadata extraction:
     * - @Payload: The actual message content (JSON string)
     * - @Header annotations: Extract Kafka message metadata like key, topic, partition, offset
     *
     * Process flow:
     * 1. Receives message from Kafka topic
     * 2. Logs message metadata for monitoring/debugging
     * 3. Deserializes JSON string to WeatherData object
     * 4. Delegates to business logic processing method
     * 5. Handles any errors during processing
     *
     * @param message The JSON message payload containing weather data
     * @param key The message key (typically city name for partitioning)
     * @param topic The Kafka topic name where message originated
     * @param partition The partition number within the topic
     * @param offset The message offset within the partition (for ordering/replay)
     */
    @KafkaListener(topics = "${kafka.topic.weather}", groupId = "${kafka.consumer.group-id}")
    public void handleWeatherData(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        try {
            log.info("Received message from topic '{}', partition {}, offset {}, key: {}",
                    topic, partition, offset, key);

            WeatherData weatherData = objectMapper.readValue(message, WeatherData.class);
            log.info("Processing weather data: {}", weatherData);

            // Process the weather data here - you can add your business logic
            processWeatherData(weatherData);

        } catch (Exception e) {
            log.error("Error processing weather data message: {} - Error: {}", message, e.getMessage(), e);
        }
    }

    /**
     * Processes the deserialized weather data with business logic.
     * This method is where you implement your specific requirements for handling weather data.
     *
     * Current implementation:
     * - Logs the processed weather information
     * - Extracts key metrics (city name, temperature)
     * - Provides a foundation for additional processing
     *
     * Potential enhancements you could add:
     * - Database persistence (save weather records)
     * - Alert generation (temperature thresholds, severe weather)
     * - Data aggregation (daily/hourly averages)
     * - Third-party integrations (notifications, APIs)
     * - Data validation and quality checks
     * - Metrics collection for monitoring
     * - Cache updates for fast data retrieval
     *
     * @param weatherData The parsed weather data object containing all weather information
     */
    private void processWeatherData(WeatherData weatherData) {
        // Add your business logic here
        log.info("Processing weather data for city: {} with temperature: {}°C",
                weatherData.getCityName(),
                weatherData.getMain() != null ? weatherData.getMain().getTemp() : "N/A");

        // Example: You could save to database, send alerts, etc.

        // TODO: Add your specific business logic:
        // - Save to database: weatherRepository.save(weatherData)
        // - Send alerts: if (temperature > 30) alertService.sendHeatAlert()
        // - Update cache: cacheService.updateWeatherCache(weatherData)
        // - Calculate metrics: metricsService.updateWeatherStats(weatherData)
    }
}