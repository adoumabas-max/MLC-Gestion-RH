package cm.mlc.rh.web;

import cm.mlc.rh.domain.Conge;
import cm.mlc.rh.repository.*;
import cm.mlc.rh.service.AuditService;
import cm.mlc.rh.service.CongeService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/** W3 — Congés (Art. 89 acquisition 1,5 j/mois ; Art. 84 maternité 14 semaines). */
@Controller
@RequestMapping("/conges")
public class CongeController {

    private final CongeRepository conges;
    private final SalarieRepository salaries;
    private final CongeService service;
    private final AuditService audit;

    public CongeController(CongeRepository conges, SalarieRepository salaries,
                           CongeService service, AuditService audit) {
        this.conges = conges; this.salaries = salaries; this.service = service; this.audit = audit;
    }

    @GetMapping
    public String liste(Model model) {
        model.addAttribute("conges", conges.findAll());
        model.addAttribute("salaries", salaries.findBySocieteAndStatut("MLC", "Actif"));
        model.addAttribute("solde", null);
        return "conges/wizard";
    }

    @GetMapping("/solde")
    @ResponseBody
    public int solde(@RequestParam Long salarieId) { return service.solde(salarieId); }

    @PostMapping
    @PreAuthorize("hasAnyRole('DRH','MANAGER')")
    public String valider(@RequestParam Long salarieId, @RequestParam String type,
                          @RequestParam String debut, @RequestParam int jours, Principal principal) {
        Conge c = new Conge();
        c.setSalarieId(salarieId); c.setType(type);
        c.setDebut(java.time.LocalDate.parse(debut)); c.setJours(jours); c.setStatut("Validé");
        conges.save(c);
        audit.tracer(principal == null ? "system" : principal.getName(),
                "Congé validé (W3)", type + " — " + jours + " j", "MLC");
        return "redirect:/conges";
    }
}
