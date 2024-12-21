package com.example.outbound.reactive.rate.limiter;

import com.example.outbound.reactive.rate.limiter.api.PublicApiHandler;
import com.example.outbound.reactive.rate.limiter.api.PublicApiProcessor;
import com.example.outbound.reactive.rate.limiter.core.OutboundReactiveRateLimiter;

public class Main {

	public static void main(String[] args) {
		OutboundReactiveRateLimiter rateLimiterService = new OutboundReactiveRateLimiter();
        PublicApiHandler publicApiHandler = new PublicApiHandler();

        // Create the PaymentProcessor instance
        PublicApiProcessor paymentProcessor = new PublicApiProcessor(rateLimiterService, publicApiHandler);

        // Start processing
        System.out.println("Starting the API request processing...");
        paymentProcessor.run();
	}
}