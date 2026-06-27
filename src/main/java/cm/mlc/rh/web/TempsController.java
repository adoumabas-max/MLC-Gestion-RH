package cm.mlc.rh.web;

import cm.mlc.rh.domain.Salarie;
import cm.mlc.rh.domain.Temps;
import cm.mlc.rh.repository.SalarieRepository;
import cm.mlc.rh.repository.TempsRepository;
import cm.mlc.rh.service.AuditService;
import cm.mlc.rh.service.TempsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/** M12 — Temps de travail : saisie des heures supplémentaires majorées (reportées en paie). */
@Controller
@RequestMapping("/temps")
public class TempsController {

    private final TempsRepository temps;
    private final SalarieRepository salaries;
    private final TempsService service;
    private final AuditService audit;

    public TempsController(TempsRepository temps, SalarieRepository salaries,
                           TempsService service, AuditService audit) {
        this.temps = temps; this.salaries = salaries; this.service = service; this.audit = audit;
    }

    /** Ligne d'affichage : pointage enrichi du matricule et du nom du salarié. */
    public record Ligne(String matricule, String nom, String periode,
                        long tauxHoraire, int heures, long montant) {}

    @GetMapping
    public String liste(Model model) {
        var lignes = temps.findAll().stream().map(t -> {
            var s = salaries.findById(t.getSalarieId()).orElse(null);
            String mat = s == null ? "?" : s.getMatricule();
            String nom = s == null ? "" : (s.getNom() + " " + (s.getPrenom() == null ? "" : s.getPrenom()));
            return new Ligne(mat, nom.trim(), t.getPeriode(),
                    t.getTauxHoraire() == null ? 0 : t.getTauxHoraire(),
                    t.getHeures() == null ? 0 : t.getHeures(),
                    t.getMontant() == null ? 0 : t.getMontant());
        }).toList();
        model.addAttribute("pointages", lignes);
        model.addAttribute("salaries", salaries.findBySocieteAndStatut("MLC", "Actif"));
        return "temps/list";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DRH','PAIE','MANAGER')")
    public String enregistrer(@RequestParam Long salarieId, @RequestParam String periode,
                              @RequestParam(defaultValue = "0") int hs1,
                              @RequestParam(defaultValue = "0") int hs2,
                              @RequestParam(defaultValue = "0") int nuit,
                              @RequestParam(defaultValue = "0") int dim, Principal principal) {
        Salarie s = salaries.findById(salarieId).orElseThrow();
        long base = s.getSalaireBase() == null ? 0 : s.getSalaireBase();
        // Un seul pointage par salarié + période (remplacement si réédition).
        Temps t = temps.findBySalarieIdAndPeriode(salarieId, periode).orElseGet(Temps::new);
        t.setSalarieId(salarieId); t.setPeriode(periode);
        t.setHs1(hs1); t.setHs2(hs2); t.setNuit(nuit); t.setDim(dim);
        t.setHeures(hs1 + hs2 + nuit + dim);
        t.setTauxHoraire(service.tauxHoraire(base));
        t.setMontant(service.majoration(base, hs1, hs2, nuit, dim));
        temps.save(t);
        audit.tracer(principal == null ? "system" : principal.getName(),
                "Pointage (M12)", periode + " — " + s.getMatricule() + " — " + t.getHeures()
                        + " h sup. — " + t.getMontant(), s.getSociete());
        return "redirect:/temps";
    }
}
