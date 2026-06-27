package cm.mlc.rh.service;

import cm.mlc.rh.domain.Salarie;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Calcul des droits de départ : indemnité de licenciement (Art. 37 CT), préavis, congés non pris. */
@Service
public class DepartService {

    private final CongeService congeService;
    public DepartService(CongeService congeService) { this.congeService = congeService; }

    /** Tranches de l'indemnité de licenciement (Art. 37) : % du salaire mensuel moyen par année. */
    private static final long[][] TRANCHES = { {1,5}, {6,10}, {11,15}, {16,99} };
    private static final double[] TAUX = { 0.20, 0.25, 0.30, 0.35 };

    public record Droits(double anciennete, long smm, long indemnite, int preavisMois,
                         long indPreavis, int congesNonPris, long allocConges, long total) {}

    public Droits calculer(Salarie s, String motif) {
        double anc = s.getEmbauche() == null ? 0
                : ChronoUnit.DAYS.between(s.getEmbauche(), LocalDate.now()) / 365.25;
        long smm = s.getSalaireBase() == null ? 0 : s.getSalaireBase();   // salaire mensuel moyen ~ base

        long indemnite = 0;
        boolean fauteLourde = motif != null && motif.contains("Faute lourde");
        if (!fauteLourde) {
            for (int i = 0; i < TRANCHES.length; i++) {
                double anneesTranche = Math.max(0, Math.min(anc, TRANCHES[i][1]) - (TRANCHES[i][0] - 1));
                if (anneesTranche > 0) indemnite += Math.round(anneesTranche * TAUX[i] * smm);
            }
        }
        int congesNonPris = congeService.solde(s.getId());
        long allocConges = Math.round(congesNonPris / 30.0 * smm);
        int preavisMois = anc < 1 ? 1 : (anc < 5 ? 1 : 2);     // arrêté 016/MTPS (simplifié)
        long indPreavis = fauteLourde ? 0 : (long) preavisMois * smm;
        long total = indemnite + allocConges + indPreavis;
        return new Droits(anc, smm, indemnite, preavisMois, indPreavis, congesNonPris, allocConges, total);
    }
}
