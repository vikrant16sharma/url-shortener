
package com.vikrant.urlshortener.ratelimit;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class RateLimitService {

    private static final int CAPACITY = 5;
    private static final double REFILL_TOKENS_PER_SECOND = 5.0 / 60.0;

    private final ConcurrentMap<Long, TokenBucket> userBuckets =
            new ConcurrentHashMap<>();

    public boolean isAllowed(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "User ID cannot be null"
            );
        }

        TokenBucket bucket = userBuckets.computeIfAbsent(
                userId,
                id -> new TokenBucket(
                        CAPACITY,
                        REFILL_TOKENS_PER_SECOND
                )
        );

        return bucket.tryConsume();
    }
}
