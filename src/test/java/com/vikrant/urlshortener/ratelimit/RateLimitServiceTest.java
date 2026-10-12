
package com.vikrant.urlshortener.ratelimit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitServiceTest {

    @Test
    void shouldAllowFiveRequestsForOneUser() {
        RateLimitService service = new RateLimitService();

        for (int i = 0; i < 5; i++) {
            assertTrue(service.isAllowed(101L));
        }

        assertFalse(service.isAllowed(101L));
    }

    @Test
    void shouldKeepUsersRateLimitsIndependent() {
        RateLimitService service = new RateLimitService();

        for (int i = 0; i < 5; i++) {
            assertTrue(service.isAllowed(101L));
        }

        assertFalse(service.isAllowed(101L));

        // A different user has a separate bucket.
        assertTrue(service.isAllowed(202L));
    }

    @Test
    void shouldRejectNullUserId() {
        RateLimitService service = new RateLimitService();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.isAllowed(null)
        );
    }
}
