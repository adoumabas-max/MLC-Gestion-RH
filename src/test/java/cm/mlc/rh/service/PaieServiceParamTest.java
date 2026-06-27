package cm.mlc.rh.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Vérifie le câblage M15 : le moteur de paie construit via {@link ParametreService}
 * doit refléter toute mise à jour des paramètres en vigueur (non-régression du bug
 * où PaieService figeait les paramètres par défaut au démarrage).
 */
class PaieServiceParamTest {

    @Test
    void miseAJourParametres_estPriseEnComptePar_leMoteur() {
        ParametreService params = new ParametreService();
        PaieService paie = new PaieService(params);

        // Référence avec les paramètres par défaut.
        assertEquals(302_792, paie.calculer(350_000).netAPayer());

        // On bascule sur un jeu de paramètres sans aucune retenue.
        Parametres zero = Parametres.defaut();
        zero.pvidSal = 0; zero.cfcSal = 0; zero.cac = 0; zero.partFraisPro = 0;
        zero.baremeRav = java.util.List.of(new Parametres.Palier(Long.MAX_VALUE, 0));
        zero.baremeTdl = java.util.List.of(new Parametres.Palier(Long.MAX_VALUE, 0));
        params.remplacer(zero);

        // Sans retenues, le net doit égaler le brut : la mise à jour est bien suivie.
        assertEquals(350_000, paie.calculer(350_000).netAPayer(),
                "le moteur doit utiliser les paramètres courants de ParametreService");
    }

    @Test
    void smig_suit_lesParametresCourants() {
        ParametreService params = new ParametreService();
        PaieService paie = new PaieService(params);
        assertFalse(paie.salaireValide(50_000));

        Parametres bas = Parametres.defaut();
        bas.smig = 40_000;
        params.remplacer(bas);
        assertTrue(paie.salaireValide(50_000), "le SMIG doit suivre ParametreService");
    }
}
