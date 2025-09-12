# Weather Kafka Application

A Spring Boot application that fetches weather data from OpenWeather API and publishes it to Apache Kafka for pub/sub messaging.

## Features

- 🌤️ Fetches weather data from OpenWeather API
- 📤 Publishes weather data to Kafka topics
- 📥 Consumes and processes weather data from Kafka
- 🔄 RESTful API endpoints for manual triggering
- 🐳 Docker Compose setup for local Kafka cluster
- 🔧 Uses Spring Boot, Lombok, and Spring Kafka

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker and Docker Compose
- OpenWeather API key (free at https://openweathermap.org/api)

## Setup Instructions

### 1. Clone the Repository
```bash
git clone <repository-url>
cd weather-kafka-app
```

### 2. Get OpenWeather API Key
1. Sign up at https://openweathermap.org/api
2. Get your free API key
3. Update `application.yml` with your API key:
```yaml
openweather:
  api:
    key: YOUR_ACTUAL_API_KEY_HERE
```

### 3. Start Kafka Infrastructure
```bash
# Start Kafka cluster (Zookeeper + Kafka + Kafka UI)
docker-compose up -d

# Check if services are running
docker-compose ps
```

### 4. Build and Run the Application
```bash
# Build the application
mvn clean package

# Run the application
mvn spring-boot:run
```

## Usage

### REST API Endpoints

1. **Get Weather Data** (fetches and publishes to Kafka):
```bash
curl http://localhost:8080/api/weather/London
```

2. **Fetch and Publish** (alternative endpoint):
```bash
curl -X POST "http://localhost:8080/api/weather/fetch-and-publish?city=Berlin"
```

### Kafka Topics

- **Topic Name**: `weather-data`
- **Producer**: Publishes weather data when API is called
- **Consumer**: Automatically processes published weather data

### Monitor Kafka

Access Kafka UI at: http://localhost:8090
- View topics and messages
- Monitor consumer groups
- Check partition details

## Project Structure

```
src/main/java/com/example/
├── WeatherKafkaApplication.java     # Main Spring Boot application
├── config/
│   └── AppConfig.java              # Configuration beans
├── controller/
│   └── WeatherController.java      # REST API endpoints
├── kafka/
│   ├── WeatherProducer.java        # Kafka message producer
│   └── WeatherConsumer.java        # Kafka message consumer
├── model/
│   └── WeatherData.java           # Weather data model with Lombok
└── service/
    └── WeatherService.java         # OpenWeather API client
```

## Configuration

Key configuration properties in `application.yml`:

```yaml
# OpenWeather API
openweather.api.key: YOUR_API_KEY
openweather.api.url: https://api.openweathermap.org/data/2.5/weather

# Kafka
spring.kafka.bootstrap-servers: localhost:9092
kafka.topic.weather: weather-data
```