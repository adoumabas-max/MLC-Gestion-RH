package cm.mlc.rh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

/**
 * Moteur de paie déterministe (cahier des charges MLC-RH-CDC-001, §5).
 * Cas de référence validé : base 350 000 -> net 302 792 / coût employeur 406 700.
 * Tout écart constitue une non-conformité bloquante (voir PaieServiceTest).
 */
@Service
public class PaieService {

    /**
     * Source des paramètres légaux, résolue à chaque calcul. En contexte Spring elle pointe
     * sur {@link ParametreService#courant()} : toute mise à jour M15 est ainsi prise en compte
     * immédiatement par le moteur, sans recréer le service.
     */
    private final Supplier<Parametres> source;

    /** Constructeur Spring : suit dynamiquement les paramètres en vigueur (M15). */
    @Autowired
    public PaieService(ParametreService parametres) { this.source = parametres::courant; }

    /** Constructeur de test / autonome : paramètres figés. */
    public PaieService(Parametres parametres) { this.source = () -> parametres; }

    /** Construit le service avec les paramètres par défaut. */
    public PaieService() { this(Parametres.defaut()); }

    public PaieResult calculer(long salaireBase) { return calculer(salaireBase, 0, 0, 0); }

    public PaieResult calculer(long salaireBase, long primesImposables,
                               long primesNonImposables, long majorationsHS) {
        Parametres p = source.get();
        long sbt = salaireBase + primesImposables + majorationsHS;   // salaire brut taxable

        // --- Retenues salariales ---
        long pvidSal  = Math.round(p.pvidSal * Math.min(sbt, p.plafondCnps));     // Art. CNPS
        long baseIRPP = Math.max(0, Math.round(sbt * p.partFraisPro) - pvidSal - p.abattForfaitIRPP);
        long irpp     = baremeProgressif(p, baseIRPP);                            // barème IRPP
        long cac      = Math.round(p.cac * irpp);
        long cfcSal   = Math.round(p.cfcSal * sbt);
        long rav      = Parametres.bareme(p.baremeRav, sbt);
        long tdl      = Parametres.bareme(p.baremeTdl, sbt);
        long totalRetenues = pvidSal + irpp + cac + cfcSal + rav + tdl;
        long netAPayer     = sbt + primesNonImposables - totalRetenues;

        // --- Charges patronales ---
        long pvidPat = Math.round(p.pvidPat * Math.min(sbt, p.plafondCnps));
        long pf      = Math.round(p.allocFam * sbt);
        long at      = Math.round(p.accidentTravail * sbt);
        long cfcPat  = Math.round(p.cfcPat * sbt);
        long fne     = Math.round(p.fne * sbt);
        long totalPatronal = pvidPat + pf + at + cfcPat + fne;
        long coutEmployeur = sbt + totalPatronal;

        return new PaieResult(sbt, pvidSal, irpp, cac, cfcSal, rav, tdl,
                totalRetenues, netAPayer, pvidPat, pf, at, cfcPat, fne,
                totalPatronal, coutEmployeur);
    }

    /** SMIG : contrôle bloquant (Art. — salaire minimum interprofessionnel garanti). */
    public boolean salaireValide(long salaireBase) { return salaireBase >= source.get().smig; }

    public long smig() { return source.get().smig; }

    private long baremeProgressif(Parametres p, long base) {
        double irpp = 0; long prev = 0;
        for (Parametres.Tranche t : p.baremeIRPP) {
            if (base > prev) { irpp += (Math.min(base, t.plafond()) - prev) * t.taux(); prev = t.plafond(); }
            else break;
        }
        return Math.round(irpp);
    }
}
