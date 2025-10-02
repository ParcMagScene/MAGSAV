package com.magsav.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@SpringBootTest
@AutoConfigureMockMvc
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
@Import(WebIntegrationTest.TestDBConfig.class)
@org.springframework.test.context.TestPropertySource(
    properties = "spring.main.allow-bean-definition-overriding=true")
class WebSecurityHeadersTest {

  @Autowired MockMvc mvc;

  @Test
  @DisplayName("Headers X-Content-Type-Options et X-Frame-Options présents en HTTP")
  void defaultSecurityHeadersOnHttp() throws Exception {
    mvc.perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(header().string("X-Content-Type-Options", "nosniff"))
        .andExpect(header().string("X-Frame-Options", "DENY"));
  }

  @Test
  @DisplayName("HSTS envoyé uniquement en HTTPS")
  void hstsOnlyOnHttps() throws Exception {
    // Requête HTTPS simulée
    RequestPostProcessor https =
        new RequestPostProcessor() {
          @Override
          public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
            request.setSecure(true);
            request.addHeader("X-Forwarded-Proto", "https");
            return request;
          }
        };

    var httpsResult = mvc.perform(get("/").with(https)).andExpect(status().isOk()).andReturn();

    String hsts = httpsResult.getResponse().getHeader("Strict-Transport-Security");
    assertThat(hsts).as("HSTS doit être présent en HTTPS").isNotNull();

    // Requête HTTP (non secure)
    var httpResult = mvc.perform(get("/")).andExpect(status().isOk()).andReturn();
    String hstsHttp = httpResult.getResponse().getHeader("Strict-Transport-Security");
    assertThat(hstsHttp).as("HSTS ne doit pas être présent en HTTP").isNull();
  }
}
