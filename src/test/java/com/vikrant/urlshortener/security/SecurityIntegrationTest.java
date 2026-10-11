package com.vikrant.urlshortener.security;

import com.vikrant.urlshortener.config.JwtProperties;
import com.vikrant.urlshortener.repository.UserRepository;

import org.junit.jupiter.api.Test;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.vikrant.urlshortener.Services.JwtService;
import com.vikrant.urlshortener.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User createTestUser() {

        User user = new User();
        String email = "jwt-test-" + UUID.randomUUID() + "@example.com";
        user.setEmail(email);
        user.setPasswordHash(
                passwordEncoder.encode("password123")
        );

        return userRepository.save(user);
    }
    @Test
    void protectedEndpointWithoutJwtShouldReturn401()
            throws Exception {

        mockMvc.perform(
                        post("/api/urls")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                                "url": "https://google.com"
                            }
                        """)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointWithInvalidJwtShouldReturn401()
            throws Exception {

        mockMvc.perform(
                        post("/api/urls")
                                .header(
                                        "Authorization",
                                        "Bearer invalid.jwt.token"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                        {
                            "url": "https://google.com"
                        }
                    """)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerShouldBeAccessibleWithoutJwt()
            throws Exception {

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                        {
                            "email": "security-test@example.com",
                            "password": "password123"
                        }
                    """)
                )
                .andExpect(status().isCreated());
    }
    @Test
    void protectedEndpointWithValidJwtShouldNotReturn401()
            throws Exception {

        User user = createTestUser();

        String token = jwtService.generateToken(
                user.getId(),
                user.getEmail()
        );

        mockMvc.perform(
                        post("/api/urls")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                        {
                            "url": "https://google.com"
                        }
                    """)
                )
                .andExpect(status().isCreated());
    }

    @Test
    void protectedEndpointWithWrongSignatureShouldReturn401()
            throws Exception {

        mockMvc.perform(
                        post("/api/urls")
                                .header(
                                        "Authorization",
                                        "Bearer abc.def.ghi"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                        {   
                            "url": "https://google.com"
                        }
                    """)
                )
                .andExpect(status().isUnauthorized());
    }
}