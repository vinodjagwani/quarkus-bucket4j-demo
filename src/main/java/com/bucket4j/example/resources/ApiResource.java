package com.bucket4j.example.resources;

import com.bucket4j.example.services.RateLimiterService;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/api")
public class ApiResource {

    private final RateLimiterService limiter;

    public ApiResource(final RateLimiterService limiter) {
        this.limiter = limiter;
    }

    @GET
    @Path("/rate-limit")
    public Response rateLimit(@HeaderParam("X-API-Key") String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("API key required").build();
        }

        boolean allowed = limiter.tryConsume(apiKey);
        long tokensLeft = limiter.availableTokens(apiKey);
        String plan = limiter.resolvePlan(apiKey);

        if (!allowed) {
            return Response.status(429)
                    .entity("Too many requests for plan: " + plan + ", try later!")
                    .build();
        }

        return Response.ok("Plan: " + plan + ", Attempts left: " + tokensLeft).build();
    }
}
