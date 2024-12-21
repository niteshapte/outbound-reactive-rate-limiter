# Outbound Reactive Rate Limiter
Outbound API rate limiting with Project Reactor in Java. The implementation ensures that the number of outgoing API requests adheres to a configurable **Transactions Per Second (TPS)** limit, preventing API throttling or overuse while processing large datasets efficiently.

## Features
1. **Dynamic Rate Limiting**: Ensures outgoing requests do not exceed the configured TPS (Transactions Per Second) limit.
2. **Reactive Programming**: Built entirely with Project Reactor to enable efficient, non-blocking, and asynchronous operations.
3. **Functional Interface Support**: Allows extensibility by passing different handlers using a functional programming approach.
4. **Thread-Safe Implementation**: Uses AtomicInteger and AtomicLong to manage concurrent request limits safely.
5. **Highly Configurable**: Supports customization for buffer sizes, delays, thread counts, and TPS limits to adapt to various use cases.

## Overview
1. **Rate Limiter**:
    - Tracks the number of API requests per second.
    - Delays further requests if the TPS limit is exceeded.
2. **API Processor**:
    - Accepts batches of API requests for processing.
    - Uses parallelism for efficient request handling.
    - Applies rate-limiting logic before dispatching each request.
3. **Reactive Flow**:
    - Processes data streams using Project Reactor.
    - Enables delay-based buffering and backpressure handling.

## How It Works
* **Configuration**: Define TPS, buffer size, and other properties in the environment or application configuration.
* **Data Processing**: Feed the data source (e.g., a list of API requests) into the PaymentProcessor.
* **Rate-Limiting Logic**:
    - Track requests per second using AtomicInteger and AtomicLong.
    - Enforce delays when the TPS limit is reached.
* **API Handling**: Process individual API calls using the PublicApiHandler.

## Key Technologies
* **Project Reactor**: For reactive, non-blocking data processing.
* **Java Concurrency**: To manage thread-safe operations for rate limiting.
* **Functional Programming**: For extensible and reusable request handlers.

## Example Use Case
This project is ideal for scenarios where you need to process large datasets while adhering to API rate limits. For example:
  - Billing systems interacting with a third-party payment gateway.
  - IoT systems sending telemetry data to an external server.
  - Data pipelines that call public APIs for processing.

## OutboundReactiveRateLimiter
The ```OutboundReactiveRateLimiter``` class encapsulates the core logic for rate-limiting outbound requests. It ensures compliance with a Transactions Per Second (TPS) threshold, protecting APIs from being overwhelmed while maintaining efficient request processing. Here's a detailed explanation of its implementation:
```
/**
 * A utility class that provides a rate-limiting mechanism for outbound reactive operations.
 * It ensures that requests do not exceed a specified Transactions Per Second (TPS) limit.
 */
public class OutboundReactiveRateLimiter {

    /**
     * Processes data objects with a rate limit applied to ensure that outgoing requests
     * adhere to a specified Transactions Per Second (TPS) threshold.
     *
     * @param dataObject    The data object to process. This could represent a request payload
     *                      or any data relevant to the operation.
     * @param requestCount  A thread-safe counter tracking the number of requests made in the
     *                      current time window.
     * @param lastResetTime A thread-safe tracker for the last time the request count was reset.
     *                      Used to enforce rate-limiting time windows.
     * @param maxTps        The maximum number of requests allowed per second.
     * @param handler       A {@link RequestHandler} functional interface used to process the
     *                      data object.
     * @param <T>           The type of the data object being processed.
     * @return A {@link Mono<Void>} that completes when the processing of the data object is done.
     *         If the rate limit is exceeded, the Mono will delay execution before completing.
     */
    public <T> Mono<Void> handleWithRateLimit(T dataObject, AtomicInteger requestCount, AtomicLong lastResetTime, int maxTps, RequestHandler<T> handler) {
        return Mono.defer(() -> {
            long currentTime = System.currentTimeMillis();

            // Check if more than a second has passed since last reset
            if (currentTime - lastResetTime.get() >= 1000) {
                // Reset the request count and update the last reset time atomically
                lastResetTime.set(currentTime);
                requestCount.set(0);
            }

            // Increment the request count and check the rate limit
            int currentRequestCount = requestCount.incrementAndGet();
            if (currentRequestCount > maxTps) {
                // Calculate remaining time to wait
                long sleepTime = 1000 - (currentTime - lastResetTime.get());
                sleepTime = Math.max(sleepTime, 0);

                // Log and delay the request until the rate limit window resets
                System.out.println("Rate limit exceeded, sleeping for " + sleepTime + " ms");

                return Mono.delay(Duration.ofMillis(sleepTime)).then(handler.handleRequest(dataObject));
            }

            // Proceed immediately if the rate limit is not exceeded
            return handler.handleRequest(dataObject);
        });
    }
}

```
#### Explanation
1. Purpose:
    - The class is designed to regulate outgoing request rates to conform to a defined TPS limit.
2. Parameters:
    - ```dataObject```: Represents the payload or context for the operation, allowing flexibility to handle diverse use cases.
    - ```chargeType```: Indicates the type of request, useful for logging or categorization.
    - ```requestCount```: Tracks how many requests have been made in the current second, ensuring TPS compliance.
    - ```lastResetTime```: Monitors the last time the rate counter was reset to establish rate-limiting windows.
    - ```maxTps```: The upper limit of allowed transactions per second.
    - ```handler```: A functional interface for custom request handling, making the solution adaptable to various processing requirements.
3. Implementation:
    - Time Window Management:
          - Resets the counter if a second has passed since the last reset.
    - Rate Limiting:
        - Increments the request count and compares it to the maxTps.
        - If exceeded, calculates the remaining time in the window and delays further processing.
    - Flexibility:
        - Accepts a ```RequestHandler``` functional interface, enabling modular and reusable request processing logic.
4. Reactive Pattern:
    - Utilizes Mono.defer to ensure the rate-limiting logic is evaluated lazily.
    - Combines delay and request handling into a seamless reactive pipeline.

#### Output Example
- Normal Flow:
  ```
  Started API request processing at 2024-12-21T10:00:00
  Response from https://jsonplaceholder.typicode.com/photos/1: {response_body}
  Response from https://jsonplaceholder.typicode.com/photos/2: {response_body}

  API request processing ended at 2024-12-21T10:10:00. Total execution time: 10 minutes.
  ```
- Rate Limit Hit:
  ```
  Rate limit exceeded, sleeping for 100 ms
  Rate limit exceeded, sleeping for 200 ms
  ```

## Getting Started
### Prerequisites
  - Java 17 or higher (compatible with Java 21).
  - Build tool: Maven.

### Running the Project
1. Clone the repository:
   ```
   git clone https://github.com/niteshapte/outbound-reactive-rate-limiter.git
   ```
2. Navigate to the project directory:
   ```
   cd outbound-reactive-rate-limiter
   ```
3. Compile and run the application:
   ```
   mvn clean package;
   java -jar target/rate-limited-api-processor.jar
   ```

