package com.magsav.web;

import com.magsav.db.DB;
import com.magsav.model.DossierSAV;
import com.magsav.model.Client;
import com.magsav.repo.ClientRepository;
import com.magsav.repo.DossierSAVRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class WebIntegrationTest {

    @TestConfiguration
    static class TestDBConfig {
        @Bean
        @Primary
        DataSource testDataSource() throws Exception {
            Path dbFile = Path.of("build", "test-magsav.db");
            Files.deleteIfExists(dbFile);
            var ds = DB.init("jdbc:sqlite:" + dbFile.toString());
            DB.migrate(ds);
            return ds;
        }
    }

    @Autowired
    MockMvc mvc;

    @Autowired
    DataSource dataSource;

    private DossierSAVRepository dossierRepo;
    private ClientRepository clientRepo;

    @BeforeEach
    void setup() throws Exception {
        if (dossierRepo == null) dossierRepo = new DossierSAVRepository(dataSource);
        if (clientRepo == null) clientRepo = new ClientRepository(dataSource);
        try (Connection c = dataSource.getConnection()) {
            c.createStatement().execute("DELETE FROM dossiers_sav");
            c.createStatement().execute("DELETE FROM clients");
            c.createStatement().execute("DELETE FROM fournisseurs");
        }
    }

    @Test
    @DisplayName("Page index vide -> message aucun dossier")
    void indexPageEmpty() throws Exception {
        mvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Aucun dossier trouvé")));
    }

    @Test
    @DisplayName("Affichage d'un dossier + détail + changement de statut + QR + étiquette")
    void dossierLifecycle() throws Exception {
        DossierSAV d = DossierSAV.nouveau("iPhone 14", "SN-XYZ-123", "Jean Dupont", "Écran cassé", "Tech A");
        d = dossierRepo.save(d);
        Long id = d.id();
        assertThat(id).isNotNull();

        mvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Jean Dupont")));

        mvc.perform(get("/dossier/" + id))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Dossier SAV #" + id)))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Écran cassé")));

        mvc.perform(post("/dossier/" + id + "/statut")
                .param("nouveauStatut", "en_cours"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/dossier/" + id));

        mvc.perform(get("/dossier/" + id))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("en_cours")));

        mvc.perform(get("/dossier/" + id + "/qr"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("image/png")))
            .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("inline")))
            .andExpect(result -> {
                byte[] data = result.getResponse().getContentAsByteArray();
                if (data.length == 0) throw new AssertionError("Le QR retourné est vide");
            });

        mvc.perform(get("/dossier/" + id + "/etiquette"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("application/pdf")))
            .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("inline")));
    }

    @Test
    @DisplayName("Listing clients vide puis avec un client")
    void clientsListing() throws Exception {
        mvc.perform(get("/clients"))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Aucun client")));

        clientRepo.upsertByEmail(new Client(null, "Dupont", "Jean", "jean@example.com", "0102030405", "Paris"));

        mvc.perform(get("/clients"))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("jean@example.com")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Dupont")));

        mvc.perform(get("/clients").param("q", "jean"))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("jean@example.com")));
    }
}
