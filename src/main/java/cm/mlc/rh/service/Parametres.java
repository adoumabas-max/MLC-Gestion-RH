package cm.mlc.rh.service;

import java.util.List;

/**
 * Paramètres légaux du moteur de paie (module M15 — source unique de vérité).
 * Valeurs indicatives, éditables (révision annuelle selon la Loi de finances).
 * Conformes au Code du travail camerounais (Loi n° 92/007) et à la fiscalité en vigueur.
 */
public class Parametres {

    public double pvidSal = 0.042, pvidPat = 0.042;   // CNPS PVID (4,2 % / 4,2 %)
    public double allocFam = 0.07;                     // allocations familiales (employeur)
    public double accidentTravail = 0.025;             // accident du travail (employeur)
    public double cfcSal = 0.01, cfcPat = 0.015;       // Crédit Foncier (1 % / 1,5 %)
    public double fne = 0.01;                           // Fonds National de l'Emploi
    public double cac = 0.10;                            // centimes additionnels communaux (10 % IRPP)
    public double partFraisPro = 0.70;                  // on conserve 70 % du brut (abattement 30 %)
    public long plafondCnps = 750_000;                  // plafond assiette PVID
    public long abattForfaitIRPP = 41_667;              // 500 000 / 12
    public long smig = 60_000;                          // SMIG mensuel (contrôle bloquant)

    public List<Tranche> baremeIRPP;
    public List<Palier> baremeRav, baremeTdl;

    /** Tranche du barème IRPP mensuel progressif. */
    public record Tranche(long plafond, double taux) {}
    /** Palier d'un barème par tranche de salaire (RAV, TDL). */
    public record Palier(long max, long valeur) {}

    /** Renvoie la valeur du palier dont la borne supérieure couvre x. */
    public static long bareme(List<Palier> tab, long x) {
        for (Palier p : tab) if (x <= p.max()) return p.valeur();
        return 0;
    }

    /** Paramètres par défaut (calibrés sur le cas de référence client). */
    public static Parametres defaut() {
        Parametres p = new Parametres();
        // Barème IRPP mensuel (Art. barème annuel / 12)
        p.baremeIRPP = List.of(
            new Tranche(166_667, 0.10),
            new Tranche(250_000, 0.15),
            new Tranche(416_667, 0.25),
            new Tranche(Long.MAX_VALUE, 0.35));
        // RAV — redevance audiovisuelle (mensuel)
        p.baremeRav = List.of(
            new Palier(50_000, 0), new Palier(100_000, 750), new Palier(200_000, 1_950),
            new Palier(300_000, 3_250), new Palier(400_000, 4_550), new Palier(500_000, 5_850),
            new Palier(600_000, 7_150), new Palier(700_000, 8_450), new Palier(800_000, 9_750),
            new Palier(900_000, 11_050), new Palier(1_000_000, 12_350), new Palier(Long.MAX_VALUE, 13_000));
        // TDL — taxe de développement local (mensuel, calibré sur le cas de référence)
        p.baremeTdl = List.of(
            new Palier(100_000, 500), new Palier(200_000, 1_000), new Palier(300_000, 1_500),
            new Palier(400_000, 2_500), new Palier(500_000, 3_000), new Palier(700_000, 4_000),
            new Palier(Long.MAX_VALUE, 5_000));
        return p;
    }
}
