package cm.mlc.rh.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Tests d'intégration de la chaîne web (session) : redirection login et en-têtes de sécurité. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WebSecurityTest {

    @Autowired MockMvc mvc;

    @Test
    void dashboard_nonAuthentifie_redirigeVersLogin() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(roles = "DRH")
    void dashboard_authentifie_afficheLaVue() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attributeExists("conforme"));
    }

    @Test
    @WithMockUser(roles = "DRH")
    void enTetesDeSecurite_sontPresents() throws Exception {
        // HSTS n'est émis que sur requête HTTPS (request.isSecure()).
        mvc.perform(get("/").secure(true))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "SAMEORIGIN"))
                .andExpect(header().string("Strict-Transport-Security", containsString("max-age=31536000")));
    }

    @Test
    void pageLogin_estPublique() throws Exception {
        mvc.perform(get("/login"))
                .andExpect(status().isOk());
    }
}
