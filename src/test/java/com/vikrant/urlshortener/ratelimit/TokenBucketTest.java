package com.vikrant.urlshortener.ratelimit;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class TokenBucketTest {

    private static final long ONE_SECOND = 1_000_000_000L;

    @Test
    void shouldStartWithFullCapacity() {
        TokenBucket bucket = new TokenBucket(5, 5.0);

        for (int i = 0; i < 5; i++) {
            assertTrue(bucket.tryConsume());
        }

        assertFalse(bucket.tryConsume());
    }

    @Test
    void shouldRejectRequestWhenBucketIsEmpty() {
        TokenBucket bucket = new TokenBucket(2, 1.0);

        assertTrue(bucket.tryConsume());
        assertTrue(bucket.tryConsume());
        assertFalse(bucket.tryConsume());
    }

    @Test
    void shouldRefillTokensOverTime() {
        AtomicLong clock = new AtomicLong(0);

        TokenBucket bucket =
                new TokenBucket(5, 1.0, clock::get);

        for (int i = 0; i < 5; i++) {
            assertTrue(bucket.tryConsume());
        }

        assertFalse(bucket.tryConsume());

        // One second adds one token at a rate of 1 token/second.
        clock.addAndGet(ONE_SECOND);

        assertTrue(bucket.tryConsume());
        assertFalse(bucket.tryConsume());
    }

    @Test
    void shouldNotExceedBucketCapacity() {
        AtomicLong clock = new AtomicLong(0);

        TokenBucket bucket =
                new TokenBucket(3, 1.0, clock::get);

        // Leave the bucket full, then advance time.
        clock.addAndGet(10 * ONE_SECOND);

        // Capacity remains three, even after a long idle period.
        assertTrue(bucket.tryConsume());
        assertTrue(bucket.tryConsume());
        assertTrue(bucket.tryConsume());
        assertFalse(bucket.tryConsume());
    }

    @Test
    void shouldRefillFractionally() {
        AtomicLong clock = new AtomicLong(0);

        TokenBucket bucket =
                new TokenBucket(1, 2.0, clock::get);

        assertTrue(bucket.tryConsume());
        assertFalse(bucket.tryConsume());

        // At 2 tokens/second, half a second restores one token.
        clock.addAndGet(ONE_SECOND / 2);

        assertTrue(bucket.tryConsume());
        assertFalse(bucket.tryConsume());
    }
}
