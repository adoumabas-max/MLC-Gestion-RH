package cm.mlc.rh.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** M12 — vérifie le calcul des majorations d'heures supplémentaires (décret 95/677). */
class TempsServiceTest {

    private final TempsService service = new TempsService();

    @Test
    void tauxHoraire_baseSur173_33h() {
        assertEquals(2019, service.tauxHoraire(350_000)); // 350 000 / 173,33
    }

    @Test
    void majoration_8hA20pct_et_4hA30pct() {
        // 350000/173,33 = 2019,27 ; 2019,27 * (8*1,20 + 4*1,30) = 2019,27 * 14,8 ≈ 29 885
        assertEquals(29_885, service.majoration(350_000, 8, 4, 0, 0));
    }

    @Test
    void majoration_nuitEtDimanche() {
        // 2019,27 * (2*1,50 + 1*1,40) = 2019,27 * 4,4 ≈ 8 885
        assertEquals(8_885, service.majoration(350_000, 0, 0, 2, 1));
    }

    @Test
    void aucuneHeureSup_zero() {
        assertEquals(0, service.majoration(350_000, 0, 0, 0, 0));
    }
}
