package com.example.outbound.reactive.rate.limiter.api;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import com.example.outbound.reactive.rate.limiter.core.OutboundReactiveRateLimiter;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

/**
 * A processor that manages the execution of outbound API requests with rate limiting and reactive processing.
 * This class leverages the {@link OutboundReactiveRateLimiter} to enforce a Transactions Per Second (TPS)
 * limit on API calls.
 */
public class PublicApiProcessor {

    private final OutboundReactiveRateLimiter rateLimiter;
    private final PublicApiHandler publicApiHandler;

    /**
     * Constructs a new {@code PublicApiProcessor} with the specified rate limiter and API handler.
     *
     * @param rateLimiterService The {@link OutboundReactiveRateLimiter} to manage rate-limiting.
     * @param publicApiHandler   The {@link PublicApiHandler} to handle individual API requests.
     */
    public PublicApiProcessor(OutboundReactiveRateLimiter rateLimiterService, PublicApiHandler publicApiHandler) {
        this.rateLimiter = rateLimiterService;
        this.publicApiHandler = publicApiHandler;
    }

    /**
     * Starts the processing of API requests in a reactive and rate-limited manner.
     * 
     * The method:
     * - Creates a flux of API requests with dynamically generated URLs and query parameters.
     * - Buffers, delays, and processes these requests in parallel with rate-limiting.
     * - Logs the start and end times for the operation, including the total execution time.
     * - Ensures main thread synchronization using a {@link CountDownLatch}.
     */
    public void run() {
        CountDownLatch latch = new CountDownLatch(1); // Ensure main thread waits

        LocalDateTime startDateTime = LocalDateTime.now();
        System.out.println("Started API request processing at " + startDateTime);

        // Get it from configuration 
        int bufferSize = 10;
        int threadCount = 2;
        int delayInMs = 100;
        int maxTps = 20;

        AtomicInteger requestCount = new AtomicInteger(0);
        AtomicLong lastResetTime = new AtomicLong(System.currentTimeMillis());

        try {
            // Generate API requests
            Flux<PublicApiRequest> requestFlux = Flux.fromStream(
                IntStream.range(1, 5000).mapToObj(i -> {
                    String url = "https://jsonplaceholder.typicode.com/photos/" + i;
                    return new PublicApiRequest(url);
                })
            );

            requestFlux
                .buffer(bufferSize) // Group requests into batches
                .delayElements(Duration.ofMillis(delayInMs)) // Add delay between batches
                .flatMap(batch -> Flux.fromIterable(batch) // Process each batch
                    .parallel(threadCount) // Enable parallel processing
                    .runOn(Schedulers.boundedElastic()) // Use bounded elastic scheduler
                    .flatMap(request ->
                        rateLimiter.handleWithRateLimit(
                            request,
                            requestCount,
                            lastResetTime,
                            maxTps,
                            publicApiHandler::handleRequest // Delegate handling to PublicApiHandler
                        )
                    )
                )
                .doOnComplete(() -> {
                    // Log end time and execution duration
                    LocalDateTime endDateTime = LocalDateTime.now();
                    Duration totalTime = Duration.between(startDateTime, endDateTime);
                    System.out.println("API request processing ended at " + endDateTime + ". Total execution time: " + totalTime.toMinutes() + " minutes.");
                    latch.countDown(); // Release latch when done
                })
                .doOnError(error -> {
                    // Handle errors during processing
                    System.err.println("Error occurred: " + error.getMessage());
                    latch.countDown(); // Release latch in case of error
                })
                .subscribe();

            latch.await(); // Wait for stream to complete
        } catch (Throwable e) {
            // Handle unexpected errors
            System.err.println("Unexpected error occurred: " + e.getMessage());
        }
    }
}