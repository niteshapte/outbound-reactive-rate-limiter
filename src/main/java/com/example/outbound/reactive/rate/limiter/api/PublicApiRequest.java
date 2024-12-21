package com.example.outbound.reactive.rate.limiter.api;

public class PublicApiRequest {

	private String url;
	
    public PublicApiRequest(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}