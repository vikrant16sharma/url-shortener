package com.vikrant.urlshortener.ratelimit;

import java.util.Objects;
import java.util.function.LongSupplier;

public class TokenBucket {

    private final int capacity;
    private final double refillTokensPerNano    ;
    private final LongSupplier nanoTime;

    private double tokens;
    private long lastRefillTime;

    public TokenBucket(int capacity, double refillTokensPerSecond) {
        this(capacity, refillTokensPerSecond, System::nanoTime);
    }

    // Package-private constructor makes time-based tests deterministic.
    TokenBucket(
            int capacity,
            double refillTokensPerSecond,
            LongSupplier nanoTime
    ) {
        if (capacity <= 0) {
            throw new IllegalArgumentException(
                    "Capacity must be greater than zero"
            );
        }

        if (!Double.isFinite(refillTokensPerSecond)
                || refillTokensPerSecond <= 0) {
            throw new IllegalArgumentException(
                    "Refill rate must be greater than zero"
            );
        }

        this.capacity = capacity;
        this.refillTokensPerNano =
                refillTokensPerSecond / 1_000_000_000.0;
        this.nanoTime = Objects.requireNonNull(nanoTime);

        this.tokens = capacity;
        this.lastRefillTime = nanoTime.getAsLong();
    }

    public synchronized boolean tryConsume() {
        refill();

        if (tokens < 1.0) {
            return false;
        }

        tokens -= 1.0;
        return true;
    }

    private void refill() {
        long currentTime = nanoTime.getAsLong();
        long elapsedNanos = currentTime - lastRefillTime;

        if (elapsedNanos <= 0) {
            return;
        }

        tokens = Math.min(
                capacity,
                tokens + elapsedNanos * refillTokensPerNano
        );

        lastRefillTime = currentTime;
    }
}
