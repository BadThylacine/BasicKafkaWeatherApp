package producer;

import model.WeatherData;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    @Value("${openweather.api.key}")
    private String apiKey;

    @Value("${openweather.api.url}")
    private String apiUrl;

    @Value("${kafka.topic.weather}")
    private String weatherTopic;

    @Value("${weather.default.city:London}")
    private String defaultCity;

    /**
     * Automatically fetches weather data at scheduled intervals.
     * This method runs continuously based on the configured interval (default: 30 seconds).
     * It fetches weather data for the default city and publishes it to Kafka.
     *
     * Uses reactive programming to handle the async operation without blocking the scheduler thread.
     *
     * @see #fetchWeatherDataAsync(String) for the actual fetching logic
     */
    @Scheduled(fixedRateString = "${weather.fetch.interval:30000}")
    public void fetchWeatherDataScheduled() {
        fetchWeatherDataAsync(defaultCity)
                .subscribe(
                        data -> log.info("✅ Scheduled fetch completed for {}", defaultCity),
                        error -> log.error("❌ Scheduled fetch failed for {}", defaultCity, error)
                );
    }

    /**
     * Manually sends pre-fetched weather data to Kafka.
     * This method is used when you already have a WeatherData object and want to publish it.
     * Typically called from REST endpoints or other services.
     *
     * Process:
     * 1. Converts WeatherData object to JSON string using ObjectMapper
     * 2. Sends to Kafka topic with city name as the message key
     * 3. Logs success/failure with truncated message preview
     *
     * @param weatherData The weather data object to send
     * @throws RuntimeException if serialization or Kafka sending fails
     */
    public void sendWeatherData(WeatherData weatherData) {
        try {
            String message = objectMapper.writeValueAsString(weatherData);
            kafkaTemplate.send(weatherTopic, weatherData.getCityName(), message);
            log.info("✅ Sent weather data to Kafka topic '{}': {}", weatherTopic,
                    message.substring(0, Math.min(message.length(), 100)) + "...");
        } catch (Exception e) {
            log.error("❌ Failed to send weather data to Kafka", e);
            throw new RuntimeException("Failed to send message to Kafka", e);
        }
    }

    /**
     * Asynchronously fetches weather data from OpenWeather API and sends to Kafka.
     * This is the main method for reactive weather data processing.
     *
     * Process:
     * 1. Builds API request URL with city, API key, and metric units
     * 2. Makes non-blocking HTTP GET request using WebClient
     * 3. Automatically deserializes JSON response to WeatherData object
     * 4. On success: serializes to JSON and sends to Kafka topic
     * 5. On error: logs the failure
     *
     * Uses reactive operators:
     * - doOnNext: Side effect when data is received successfully
     * - doOnError: Side effect when an error occurs
     *
     * @param city The city name to fetch weather data for
     * @return Mono<WeatherData> reactive stream that emits the weather data or error
     */
    public Mono<WeatherData> fetchWeatherDataAsync(String city) {
        String uri = String.format("?q=%s&appid=%s&units=metric", city, apiKey);

        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(WeatherData.class)
                .doOnNext(weatherData -> {
                    try {
                        String message = objectMapper.writeValueAsString(weatherData);
                        kafkaTemplate.send(weatherTopic, city, message);
                        log.info("✅ Sent weather data to Kafka: {}",
                                message.substring(0, Math.min(message.length(), 100)) + "...");
                    } catch (Exception e) {
                        log.error("❌ Failed to send weather data to Kafka", e);
                    }
                })
                .doOnError(error -> log.error("❌ Failed to fetch weather data for city: {}", city, error));
    }

    /**
     * Fetches weather data as raw JSON string and sends to Kafka.
     * This method preserves the original JSON structure without object mapping.
     * Useful when you need the raw API response or want to avoid deserialization overhead.
     *
     * Process:
     * 1. Builds API request URL with city, API key, and metric units
     * 2. Makes non-blocking HTTP GET request using WebClient
     * 3. Returns response as raw JSON string
     * 4. On success: sends raw JSON directly to Kafka topic
     * 5. On error: logs the failure
     *
     * Advantages:
     * - Faster processing (no object mapping)
     * - Preserves all API response fields
     * - Lower memory usage
     *
     * @param city The city name to fetch weather data for
     * @return Mono<String> reactive stream that emits the raw JSON response or error
     */
    public Mono<String> fetchWeatherDataAsString(String city) {
        String uri = String.format("?q=%s&appid=%s&units=metric", city, apiKey);

        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(response -> {
                    kafkaTemplate.send(weatherTopic, city, response);
                    log.info("✅ Sent raw weather data to Kafka: {}",
                            response.substring(0, Math.min(response.length(), 100)) + "...");
                })
                .doOnError(error -> log.error("❌ Failed to fetch weather data for city: {}", city, error));
    }
}