package com.bucket4j.example.services;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class RateLimiterService {

    private final ConcurrentMap<String, Bucket> cache = new ConcurrentHashMap<>();

    // ---- Config from application.properties ----
    @ConfigProperty(name = "rate-limiter.free.capacity")
    int freeCapacity;

    @ConfigProperty(name = "rate-limiter.free.refill-tokens")
    int freeRefillTokens;

    @ConfigProperty(name = "rate-limiter.free.refill-period-seconds")
    int freeRefillPeriod;

    @ConfigProperty(name = "rate-limiter.premium.capacity")
    int premiumCapacity;

    @ConfigProperty(name = "rate-limiter.premium.refill-tokens")
    int premiumRefillTokens;

    @ConfigProperty(name = "rate-limiter.premium.refill-period-seconds")
    int premiumRefillPeriod;

    @ConfigProperty(name = "rate-limiter.enterprise.capacity")
    int enterpriseCapacity;

    @ConfigProperty(name = "rate-limiter.enterprise.refill-tokens")
    int enterpriseRefillTokens;

    @ConfigProperty(name = "rate-limiter.enterprise.refill-period-seconds")
    int enterpriseRefillPeriod;

    // ---- helpers ----
    private static Bucket newBucket(final int capacity, final int refillTokens, final int refillPeriodSeconds) {
        final Bandwidth limit = Bandwidth.classic(
                capacity,
                Refill.greedy(refillTokens, Duration.ofSeconds(refillPeriodSeconds))
        );
        return Bucket.builder().addLimit(limit).build();
    }

    // ---- plans ----
    private Bucket newFreeBucket() {
        return newBucket(freeCapacity, freeRefillTokens, freeRefillPeriod);
    }

    private Bucket newPremiumBucket() {
        return newBucket(premiumCapacity, premiumRefillTokens, premiumRefillPeriod);
    }

    private Bucket newEnterpriseBucket() {
        return newBucket(enterpriseCapacity, enterpriseRefillTokens, enterpriseRefillPeriod);
    }

    // ---- routing ----
    private Bucket resolveBucket(final String apiKey) {
        return cache.computeIfAbsent(apiKey, key -> {
            final String plan = resolvePlan(key);
            return switch (plan) {
                case "enterprise" -> newEnterpriseBucket();
                case "premium" -> newPremiumBucket();
                default -> newFreeBucket();
            };
        });
    }

    // ---- API ----
    public boolean tryConsume(final String apiKey) {
        return resolveBucket(apiKey).tryConsume(1);
    }

    public long availableTokens(final String apiKey) {
        return resolveBucket(apiKey).getAvailableTokens();
    }


    public String resolvePlan(final String apiKey) {
        if (apiKey != null && apiKey.startsWith("enterprise-")) {
            return "enterprise";
        }
        if (apiKey != null && apiKey.startsWith("premium-")) {
            return "premium";
        }
        return "free";
    }
}
