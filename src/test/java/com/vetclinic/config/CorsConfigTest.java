package com.vetclinic.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CorsConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void corsConfig_AllowsOrigin() throws Exception {
        // Act & Assert
        mockMvc.perform(options("/api/**")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    @Test
    void corsConfig_AllowsCredentials() throws Exception {
        // Act & Assert
        mockMvc.perform(options("/api/**")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(header().exists("Access-Control-Allow-Credentials"));
    }

    @Test
    void corsConfig_AllowsMethods() throws Exception {
        // Act & Assert
        mockMvc.perform(options("/api/**")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(header().exists("Access-Control-Allow-Methods"));
    }
}
