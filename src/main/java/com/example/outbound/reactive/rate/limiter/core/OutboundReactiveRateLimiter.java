package com.example.outbound.reactive.rate.limiter.core;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import reactor.core.publisher.Mono;

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