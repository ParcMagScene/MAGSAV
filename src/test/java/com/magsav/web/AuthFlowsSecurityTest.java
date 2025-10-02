package com.magsav.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
@Import(WebIntegrationTest.TestDBConfig.class)
@org.springframework.test.context.TestPropertySource(
    properties = "spring.main.allow-bean-definition-overriding=true")
class AuthFlowsSecurityTest {

  @Autowired MockMvc mvc;

  @Test
  @DisplayName("POST /logout avec CSRF -> 3xx vers /")
  @WithMockUser(roles = {"USER"})
  void logoutWithCsrf() throws Exception {
    mvc.perform(post("/logout").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"));
  }

  @Test
  @DisplayName("POST /login sans CSRF -> 403")
  void loginWithoutCsrfForbidden() throws Exception {
    mvc.perform(post("/login").param("username", "user").param("password", "password"))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("POST /login avec CSRF -> 3xx vers /")
  void loginWithCsrfOk() throws Exception {
    mvc.perform(post("/login").with(csrf()).param("username", "user").param("password", "password"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"));
  }

  @Test
  @DisplayName("GET /intervention/1 anonyme -> redirection /login")
  void interventionAnonymousRedirects() throws Exception {
    mvc.perform(get("/intervention/1"))
        .andExpect(status().is3xxRedirection())
        .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/login")));
  }
}
