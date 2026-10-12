
package com.vikrant.urlshortener.ratelimit;

import com.vikrant.urlshortener.security.AuthenticatedUserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final AuthenticatedUserService authenticatedUserService;

    public RateLimitFilter(
            RateLimitService rateLimitService,
            AuthenticatedUserService authenticatedUserService
    ) {
        this.rateLimitService = rateLimitService;
        this.authenticatedUserService = authenticatedUserService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !HttpMethod.POST.matches(request.getMethod())
                || !"/api/urls".equals(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        // Leave unauthenticated requests to Spring Security.
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            filterChain.doFilter(request, response);
            return;
        }

        Long userId = authenticatedUserService.getCurrentUser().getId();

        if (!rateLimitService.isAllowed(userId)) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(
                    """
                    {
                      "status": 429,
                      "error": "Too Many Requests",
                      "message": "Rate limit exceeded. Please try again later."
                    }
                    """
            );
            return;
        }

        filterChain.doFilter(request, response);
    }
}
