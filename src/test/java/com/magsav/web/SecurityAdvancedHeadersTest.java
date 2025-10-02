package com.magsav.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.boot.autoconfigure.EnableAutoConfiguration(
    exclude = DataSourceAutoConfiguration.class)
@Import(WebIntegrationTest.TestDBConfig.class)
@TestPropertySource(properties = "spring.main.allow-bean-definition-overriding=true")
class SecurityAdvancedHeadersTest {

  @Autowired MockMvc mockMvc;

  @Test
  void enTetesAvancesPresentSurIndex() throws Exception {
    mockMvc
        .perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(
            header()
                .string(
                    "Content-Security-Policy",
                    org.hamcrest.Matchers.containsString("default-src 'self'")))
        .andExpect(header().string("Referrer-Policy", "strict-origin-when-cross-origin"))
        .andExpect(
            header()
                .string("Permissions-Policy", org.hamcrest.Matchers.containsString("camera=()")));
  }
}
