package com.magsav.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
@Import(WebIntegrationTest.TestDBConfig.class)
@org.springframework.test.context.TestPropertySource(
    properties = "spring.main.allow-bean-definition-overriding=true")
class ProductUploadNegativeTest {

  @Autowired MockMvc mvc;

  @Test
  @DisplayName("Upload sans CSRF -> 403 Forbidden")
  @WithMockUser(roles = {"ADMIN"})
  void uploadWithoutCsrfForbidden() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile(
            "file",
            "test.png",
            MediaType.IMAGE_PNG_VALUE,
            new byte[] {
              (byte) 0x89,
              (byte) 0x50,
              (byte) 0x4E,
              (byte) 0x47,
              (byte) 0x0D,
              (byte) 0x0A,
              (byte) 0x1A,
              (byte) 0x0A
            });
    mvc.perform(multipart("/product/SEC-1/photo").file(file)).andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Upload vide -> redirect avec erreur")
  @WithMockUser(roles = {"ADMIN"})
  void uploadEmptyFile() throws Exception {
    MockMultipartFile empty =
        new MockMultipartFile("file", "empty.png", MediaType.IMAGE_PNG_VALUE, new byte[] {});
    mvc.perform(multipart("/product/SEC-2/photo").file(empty).with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("/**error=*"));
  }

  @Test
  @DisplayName("Upload trop gros -> redirect avec erreur")
  @WithMockUser(roles = {"ADMIN"})
  void uploadTooLarge() throws Exception {
    byte[] big = new byte[6 * 1024 * 1024]; // 6MB
    big[0] = (byte) 0x89;
    big[1] = (byte) 0x50;
    big[2] = (byte) 0x4E;
    big[3] = (byte) 0x47;
    big[4] = (byte) 0x0D;
    big[5] = (byte) 0x0A;
    big[6] = (byte) 0x1A;
    big[7] = (byte) 0x0A; // PNG header
    MockMultipartFile file =
        new MockMultipartFile("file", "big.png", MediaType.IMAGE_PNG_VALUE, big);
    mvc.perform(multipart("/product/SEC-3/photo").file(file).with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("/**error=*"));
  }

  @Test
  @DisplayName("Upload type non autorisé -> redirect avec erreur")
  @WithMockUser(roles = {"ADMIN"})
  void uploadInvalidType() throws Exception {
    // GIF header: 47 49 46 38 39 61
    byte[] gif = new byte[] {0x47, 0x49, 0x46, 0x38, 0x39, 0x61, 0x00, 0x00};
    MockMultipartFile file = new MockMultipartFile("file", "bad.gif", "image/gif", gif);
    mvc.perform(multipart("/product/SEC-4/photo").file(file).with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("/**error=*"));
  }

  @Test
  @DisplayName("Upload avec rôle USER (non admin) -> 403 Forbidden")
  @WithMockUser(roles = {"USER"})
  void uploadAsUserForbidden() throws Exception {
    byte[] png1x1 =
        new byte[] {
          (byte) 0x89,
          (byte) 0x50,
          (byte) 0x4E,
          (byte) 0x47,
          (byte) 0x0D,
          (byte) 0x0A,
          (byte) 0x1A,
          (byte) 0x0A
        };
    MockMultipartFile file =
        new MockMultipartFile("file", "u.png", MediaType.IMAGE_PNG_VALUE, png1x1);
    mvc.perform(multipart("/product/SEC-5/photo").file(file).with(csrf()))
        .andExpect(status().isForbidden());
  }
}
