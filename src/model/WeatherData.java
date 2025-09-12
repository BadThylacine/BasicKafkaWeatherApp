package model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Main weather data model representing the complete OpenWeather API response.
 * This class maps the JSON response from the OpenWeather API to a Java object.
 *
 * Lombok annotations:
 * - @Data: Generates getters, setters, toString, equals, and hashCode methods
 * - @NoArgsConstructor: Creates a default constructor (required for JSON deserialization)
 * - @AllArgsConstructor: Creates a constructor with all fields
 *
 * Jackson annotations:
 * - @JsonIgnoreProperties: Ignores unknown JSON fields (API may add new fields)
 * - @JsonProperty: Maps JSON field names to Java field names
 *
 * The structure mirrors the OpenWeather API response format for current weather data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherData {

    /**
     * The name of the city/location for this weather data.
     * Maps from the "name" field in the OpenWeather API response.
     */
    @JsonProperty("name")
    private String cityName;

    /**
     * Main weather metrics (temperature, humidity, pressure).
     * Contains the primary weather measurements as a nested object.
     */
    @JsonProperty("main")
    private Main main;

    /**
     * Weather condition information (description, main category).
     * Array because a location can have multiple weather conditions simultaneously.
     */
    @JsonProperty("weather")
    private Weather[] weather;

    /**
     * Wind information (speed and direction).
     * Contains wind-related measurements.
     */
    @JsonProperty("wind")
    private Wind wind;

    /**
     * Custom toString method for human-readable weather data display.
     * Formats the weather information in a concise, readable format.
     *
     * Safely handles null values to prevent NullPointerExceptions.
     * Extracts the first weather condition description if available.
     *
     * @return Formatted string like "Weather in London: 15.5°C, clear sky, Wind: 3.2 m/s"
     */
    @Override
    public String toString() {
        return String.format("Weather in %s: %.1f°C, %s, Wind: %.1f m/s",
                cityName,
                main != null ? main.getTemp() : 0.0,
                weather != null && weather.length > 0 ? weather[0].getDescription() : "N/A",
                wind != null ? wind.getSpeed() : 0.0);
    }

    /**
     * Nested class representing the main weather measurements.
     * Contains primary atmospheric and temperature data from the API's "main" section.
     *
     * Lombok @Data generates all standard methods for this nested class.
     * Jackson annotations map the JSON fields to Java properties.
     */
    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Main {

        /**
         * Current temperature in the specified units (Celsius when using units=metric).
         * This is the main temperature reading for the location.
         */
        @JsonProperty("temp")
        private double temp;

        /**
         * Humidity percentage (0-100%).
         * Represents the relative humidity in the air.
         */
        @JsonProperty("humidity")
        private int humidity;

        /**
         * Atmospheric pressure in hPa (hectopascals).
         * Sea level pressure measurement for weather analysis.
         */
        @JsonProperty("pressure")
        private double pressure;
    }

    /**
     * Nested class representing weather condition information.
     * Describes the current weather conditions (sunny, rainy, cloudy, etc.).
     *
     * The API returns an array because multiple weather conditions can occur simultaneously
     * (e.g., "light rain" and "partly cloudy").
     */
    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Weather {

        /**
         * Main weather category (e.g., "Rain", "Snow", "Clear", "Clouds").
         * Broad classification of the weather condition.
         */
        @JsonProperty("main")
        private String main;

        /**
         * Detailed weather condition description (e.g., "light rain", "clear sky").
         * More specific description than the main category.
         * This is typically what you'd display to users.
         */
        @JsonProperty("description")
        private String description;
    }

    /**
     * Nested class representing wind information.
     * Contains wind speed and direction measurements.
     *
     * Wind data is important for weather analysis and user safety information.
     */
    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Wind {

        /**
         * Wind speed in the specified units (m/s for metric, mph for imperial).
         * Represents the current wind velocity.
         */
        @JsonProperty("speed")
        private double speed;

        /**
         * Wind direction in degrees (0-360°).
         * Meteorological degrees where:
         * - 0° = North
         * - 90° = East
         * - 180° = South
         * - 270° = West
         */
        @JsonProperty("deg")
        private int deg;
    }
}