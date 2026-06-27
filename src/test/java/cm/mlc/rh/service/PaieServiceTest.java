package cm.mlc.rh.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test d'acceptation BLOQUANT du moteur de paie (cahier des charges, §6).
 * Le build Maven échoue si le cas de référence client n'est pas reproduit à l'unité près.
 */
class PaieServiceTest {

    private final PaieService paie = new PaieService(Parametres.defaut());

    @Test
    void casDeReference_350000_doitDonner_net302792_cout406700() {
        PaieResult r = paie.calculer(350_000);

        // Retenues salariales
        assertEquals(14_700, r.pvidSal(),  "PVID salarié");
        assertEquals(19_962, r.irpp(),     "IRPP");
        assertEquals(1_996,  r.cac(),      "CAC");
        assertEquals(3_500,  r.cfcSal(),   "CFC salarié");
        assertEquals(4_550,  r.rav(),      "RAV");
        assertEquals(2_500,  r.tdl(),      "TDL");
        assertEquals(47_208, r.totalRetenues(), "Total retenues");
        assertEquals(302_792, r.netAPayer(),    "NET A PAYER (cible client)");

        // Charges patronales
        assertEquals(14_700, r.pvidPat(),   "PVID employeur");
        assertEquals(24_500, r.allocFam(),  "Allocations familiales");
        assertEquals(8_750,  r.accident(),  "Accident du travail");
        assertEquals(5_250,  r.cfcPat(),    "CFC employeur");
        assertEquals(3_500,  r.fne(),       "FNE");
        assertEquals(56_700, r.totalPatronal(),  "Total patronal");
        assertEquals(406_700, r.coutEmployeur(), "COUT EMPLOYEUR (cible client)");
    }

    @Test
    void plafondCnps_appliqueAuDessusDe750000() {
        PaieResult r = paie.calculer(900_000);
        assertEquals(31_500, r.pvidSal(), "PVID plafonné à 4,2 % x 750 000");
    }

    @Test
    void smig_bloqueEnDessousDe60000() {
        assertFalse(paie.salaireValide(50_000));
        assertTrue(paie.salaireValide(60_000));
    }
}
