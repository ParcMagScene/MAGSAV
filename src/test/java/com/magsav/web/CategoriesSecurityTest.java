package com.magsav.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
class CategoriesSecurityTest {

  @Autowired MockMvc mvc;

  @Test
  @DisplayName("POST /categories avec ADMIN + CSRF -> 3xx")
  @WithMockUser(roles = {"ADMIN"})
  void adminCanCreateCategory() throws Exception {
    mvc.perform(post("/categories").with(csrf()).param("name", "SecTestCat"))
        .andExpect(status().is3xxRedirection());
  }

  @Test
  @DisplayName("POST /categories/{id}/delete par USER -> 403")
  @WithMockUser(roles = {"USER"})
  void userCannotDeleteCategory() throws Exception {
    mvc.perform(post("/categories/999/delete").with(csrf())).andExpect(status().isForbidden());
  }
}
