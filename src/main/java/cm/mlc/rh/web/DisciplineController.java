package cm.mlc.rh.web;

import cm.mlc.rh.domain.Discipline;
import cm.mlc.rh.repository.*;
import cm.mlc.rh.service.AuditService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;

/** W4 — Discipline (Art. 29 CT : règlement intérieur ; mise à pied ≤ 8 jours). */
@Controller
@RequestMapping("/discipline")
public class DisciplineController {

    private final DisciplineRepository disciplines;
    private final SalarieRepository salaries;
    private final AuditService audit;

    public DisciplineController(DisciplineRepository disciplines, SalarieRepository salaries, AuditService audit) {
        this.disciplines = disciplines; this.salaries = salaries; this.audit = audit;
    }

    @GetMapping
    public String liste(Model model) {
        model.addAttribute("disciplines", disciplines.findAll());
        model.addAttribute("salaries", salaries.findBySocieteAndStatut("MLC", "Actif"));
        return "discipline/wizard";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DRH','MANAGER')")
    public String enregistrer(@RequestParam Long salarieId, @RequestParam String dateFaits,
                              @RequestParam String motif, @RequestParam(required = false) String reponse,
                              @RequestParam String sanction, Principal principal) {
        Discipline d = new Discipline();
        d.setSalarieId(salarieId); d.setDateFaits(LocalDate.parse(dateFaits));
        d.setMotif(motif); d.setReponse(reponse); d.setSanction(sanction);
        disciplines.save(d);
        audit.tracer(principal == null ? "system" : principal.getName(),
                "Sanction disciplinaire (W4)", sanction, "MLC");
        return "redirect:/discipline";
    }
}
