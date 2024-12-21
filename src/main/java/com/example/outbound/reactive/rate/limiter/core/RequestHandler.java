package com.example.outbound.reactive.rate.limiter.core;

import reactor.core.publisher.Mono;

/**
 * A functional interface representing a handler for processing a data object in a reactive manner.
 * 
 * @param <T> The type of the data object to be processed.
 */
@FunctionalInterface
public interface RequestHandler<T> {

    /**
     * Processes the given data object and returns a reactive {@link Mono<Void>} indicating
     * the completion of the processing.
     *
     * @param dataObject The data object to process. This could represent a request payload
     *                   or any other relevant data.
     * @return A {@link Mono<Void>} that completes when the processing is finished. If the
     *         operation encounters an error, the Mono will propagate the error.
     */
    Mono<Void> handleRequest(T dataObject);
}
