package cm.mlc.rh.web;

import cm.mlc.rh.domain.Bulletin;
import cm.mlc.rh.domain.Salarie;
import cm.mlc.rh.repository.BulletinRepository;
import cm.mlc.rh.repository.SalarieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Rendu réel des écrans M12 (temps), M07 (DIPE nominatif) et M08 (DSF annuelle). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DeclarationWebTest {

    @Autowired MockMvc mvc;
    @Autowired SalarieRepository salaries;
    @Autowired BulletinRepository bulletins;

    @BeforeEach
    void seed() {
        bulletins.deleteAll();
        salaries.deleteAll();
        Salarie s = new Salarie();
        s.setSociete("MLC"); s.setMatricule("MLC-001"); s.setNom("NGUE"); s.setPrenom("Paul");
        s.setMatCnps("CM-100200"); s.setSalaireBase(350_000L); s.setEmbauche(LocalDate.of(2025, 2, 1));
        salaries.save(s);
        Bulletin b = new Bulletin();
        b.setSalarieId(s.getId()); b.setPeriode("2026-06");
        b.setSbt(350_000L); b.setPvidSal(14_700L); b.setPvidPat(14_700L); b.setAllocFam(24_500L);
        b.setAccident(8_750L); b.setIrpp(19_962L); b.setCac(1_996L); b.setCfcSal(3_500L); b.setCfcPat(5_250L);
        b.setRav(4_550L); b.setTdl(2_500L); b.setNetAPayer(302_792L); b.setCoutEmployeur(406_700L);
        bulletins.save(b);
    }

    @Test
    @WithMockUser(roles = "DRH")
    void temps_saffiche() throws Exception {
        mvc.perform(get("/temps")).andExpect(status().isOk())
                .andExpect(view().name("temps/list"))
                .andExpect(content().string(containsString("Temps de travail")));
    }

    @Test
    @WithMockUser(roles = "DRH")
    void dipe_nominatif_affiche_leSalarie() throws Exception {
        mvc.perform(get("/dipe")).andExpect(status().isOk())
                .andExpect(view().name("dipe/list"))
                .andExpect(content().string(containsString("DIPE nominatif")))
                .andExpect(content().string(containsString("CM-100200")));
    }

    @Test
    @WithMockUser(roles = "DRH")
    void dsf_annuelle_affiche_lExercice() throws Exception {
        mvc.perform(get("/dsf").param("annee", "2026")).andExpect(status().isOk())
                .andExpect(view().name("dsf/list"))
                .andExpect(content().string(containsString("MLC-001")));
    }
}
