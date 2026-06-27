package cm.mlc.rh.service;

import org.springframework.stereotype.Service;

/**
 * M12 — Temps de travail. Durée légale 40 h/semaine (Art. 80 CT) ≈ 173,33 h/mois.
 * Majorations heures supplémentaires (décret n° 95/677) : 8 premières +20 %, suivantes +30 %,
 * nuit +50 %, dimanche / jour férié +40 %.
 * Le moteur est aligné sur la variante mono-fichier (HTML).
 */
@Service
public class TempsService {

    public static final double HEURES_MOIS = 173.33;

    public long tauxHoraire(long salaireBase) {
        return Math.round(salaireBase / HEURES_MOIS);
    }

    /** Majoration totale (en FCFA entiers) à reporter dans le salaire brut taxable. */
    public long majoration(long salaireBase, int hs1, int hs2, int nuit, int dim) {
        double th = salaireBase / HEURES_MOIS;
        return Math.round(th * (hs1 * 1.20 + hs2 * 1.30 + nuit * 1.50 + dim * 1.40));
    }
}
