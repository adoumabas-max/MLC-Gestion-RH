package cm.mlc.rh.api;

import cm.mlc.rh.domain.Salarie;
import cm.mlc.rh.domain.Utilisateur;
import cm.mlc.rh.repository.SalarieRepository;
import cm.mlc.rh.repository.UtilisateurRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration de la couche API REST /api/v1 : authentification JWT,
 * autorisation par rôle, validation des entrées et traduction des erreurs (ApiExceptionHandler).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiSecurityTest {

    @Autowired MockMvc mvc;
    @Autowired UtilisateurRepository utilisateurs;
    @Autowired SalarieRepository salaries;
    @Autowired PasswordEncoder encoder;
    @Autowired ObjectMapper json;

    @BeforeEach
    void seed() {
        utilisateurs.deleteAll();
        salaries.deleteAll();

        Utilisateur drh = new Utilisateur();
        drh.setLogin("drh"); drh.setMotDePasse(encoder.encode("drh123"));
        drh.setRole("DRH"); drh.setSociete("MLC");
        utilisateurs.save(drh);

        Salarie s = new Salarie();
        s.setSociete("MLC"); s.setMatricule("MLC-001"); s.setNom("NGUE");
        s.setSalaireBase(350_000L); s.setEmbauche(LocalDate.of(2025, 2, 1));
        salaries.save(s);
    }

    private String jeton(String login, String mdp) throws Exception {
        String corps = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"" + login + "\",\"motDePasse\":\"" + mdp + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode node = json.readTree(corps);
        return node.get("token").asText();
    }

    @Test
    void login_valide_renvoieUnJeton() throws Exception {
        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"drh\",\"motDePasse\":\"drh123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("DRH"));
    }

    @Test
    void login_mauvaisMotDePasse_renvoie401() throws Exception {
        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"drh\",\"motDePasse\":\"faux\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_champManquant_renvoie400() throws Exception {
        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"drh\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statut").value(400));
    }

    @Test
    void salaries_sansJeton_renvoie401() throws Exception {
        mvc.perform(get("/api/v1/salaries"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void salaries_avecJetonDrh_renvoieLaListe() throws Exception {
        mvc.perform(get("/api/v1/salaries")
                        .header("Authorization", "Bearer " + jeton("drh", "drh123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].matricule").value("MLC-001"));
    }

    @Test
    void paie_salarieInconnu_renvoie404Json() throws Exception {
        mvc.perform(get("/api/v1/salaries/999999/paie")
                        .header("Authorization", "Bearer " + jeton("drh", "drh123")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statut").value(404));
    }
}
