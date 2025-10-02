package com.magsav.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import(WebIntegrationTest.TestDBConfig.class)
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
@org.springframework.test.context.TestPropertySource(
    properties = "spring.main.allow-bean-definition-overriding=true")
class SecurityIntegrationTest {

  @Autowired MockMvc mvc;

  @Test
  @DisplayName("GET protégé anonyme -> redirection login")
  void anonymousRedirectsToLogin() throws Exception {
    mvc.perform(get("/clients"))
        .andExpect(status().is3xxRedirection())
        .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/login")));
  }

  @Test
  @DisplayName("POST sans CSRF -> 403")
  @WithMockUser(roles = "ADMIN")
  void postWithoutCsrfForbidden() throws Exception {
    mvc.perform(post("/dossier/1/statut").param("nouveauStatut", "en_cours"))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("POST avec CSRF -> redirection")
  @WithMockUser(roles = "ADMIN")
  void postWithCsrfOk() throws Exception {
    mvc.perform(post("/dossier/1/statut").with(csrf()).param("nouveauStatut", "en_cours"))
        .andExpect(status().is3xxRedirection());
  }

  @Test
  @DisplayName("POST /categories nécessite ADMIN")
  @WithMockUser(roles = "USER")
  void categoriesPostRequiresAdmin() throws Exception {
    mvc.perform(
            post("/categories")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "Test"))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("POST /logout sans CSRF -> 403")
  @WithMockUser(roles = "USER")
  void logoutRequiresCsrf() throws Exception {
    mvc.perform(post("/logout")).andExpect(status().isForbidden());
  }
}
