package cm.mlc.rh.service;

import cm.mlc.rh.domain.Salarie;
import cm.mlc.rh.repository.CongeRepository;
import cm.mlc.rh.repository.SalarieRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Gestion des congés — acquisition 1,5 jour ouvrable par mois (Art. 89 CT). */
@Service
public class CongeService {

    private final SalarieRepository salaries;
    private final CongeRepository conges;

    public CongeService(SalarieRepository salaries, CongeRepository conges) {
        this.salaries = salaries; this.conges = conges;
    }

    /** Solde de congés acquis (jours), net des congés déjà validés. */
    public int solde(Long salarieId) {
        Salarie s = salaries.findById(salarieId).orElse(null);
        if (s == null || s.getEmbauche() == null) return 0;
        long mois = ChronoUnit.MONTHS.between(s.getEmbauche(), LocalDate.now());
        int acquis = (int) Math.floor(mois * 1.5);            // Art. 89 CT
        int pris = conges.sommeJours(salarieId, "Validé");    // somme déléguée à la BD
        return Math.max(0, acquis - pris);
    }
}
