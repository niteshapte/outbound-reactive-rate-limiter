package com.example.outbound.reactive.rate.limiter.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import reactor.core.publisher.Mono;

/**
 * Handles outbound HTTP requests to public APIs using a non-blocking reactive paradigm.
 * This class leverages Java's {@link HttpClient} for making HTTP requests.
 */
public class PublicApiHandler {

    private final HttpClient httpClient;

    /**
     * Default constructor that initializes the {@link HttpClient} instance
     * used for sending HTTP requests.
     */
    public PublicApiHandler() {
        this.httpClient = HttpClient.newHttpClient();
    }

    /**
     * Processes a {@link PublicApiRequest} by constructing and sending an HTTP GET request.
     * The method ensures that the request is built dynamically with query parameters,
     * and logs the response or errors as they occur.
     *
     * @param request The {@link PublicApiRequest} object containing the URL and query parameters
     *                required for the API request.
     * @return A {@link Mono<Void>} that completes when the request is successfully processed.
     *         If an error occurs, it is logged and propagated as part of the reactive stream.
     */
    public Mono<Void> handleRequest(PublicApiRequest request) {
        return Mono.fromCallable(() -> {
        	URI uri = new URI(request.getUrl());
            HttpRequest httpRequest = HttpRequest.newBuilder().uri(uri).GET().build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("Response from " + request.getUrl() + ": " + response.body());
            return response.body();
        })
        .doOnError(error -> System.err.println("Error while making request to " + request.getUrl() + ": " + error.getMessage()))
        .then();
    }
}
