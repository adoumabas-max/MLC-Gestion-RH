package cm.mlc.rh.web;

import cm.mlc.rh.domain.Depart;
import cm.mlc.rh.domain.Salarie;
import cm.mlc.rh.repository.*;
import cm.mlc.rh.service.AuditService;
import cm.mlc.rh.service.DepartService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;

/** W5 — Départ / licenciement (préavis arrêté 016/MTPS, indemnité Art. 37). */
@Controller
@RequestMapping("/departs")
public class DepartController {

    private final DepartRepository departs;
    private final SalarieRepository salaries;
    private final ContratRepository contrats;
    private final DepartService service;
    private final AuditService audit;

    public DepartController(DepartRepository departs, SalarieRepository salaries,
                            ContratRepository contrats, DepartService service, AuditService audit) {
        this.departs = departs; this.salaries = salaries; this.contrats = contrats;
        this.service = service; this.audit = audit;
    }

    @GetMapping
    public String liste(Model model) {
        model.addAttribute("departs", departs.findAll());
        model.addAttribute("salaries", salaries.findBySocieteAndStatut("MLC", "Actif"));
        return "departs/wizard";
    }

    @PostMapping("/calculer")
    public String calculer(@RequestParam Long salarieId, @RequestParam String motif, Model model) {
        Salarie s = salaries.findById(salarieId).orElseThrow();
        model.addAttribute("droits", service.calculer(s, motif));
        model.addAttribute("salarie", s);
        model.addAttribute("motif", motif);
        model.addAttribute("departs", departs.findAll());
        model.addAttribute("salaries", salaries.findBySocieteAndStatut("MLC", "Actif"));
        return "departs/wizard";
    }

    @PostMapping("/valider")
    @PreAuthorize("hasRole('DRH')")
    public String valider(@RequestParam Long salarieId, @RequestParam String motif,
                          @RequestParam String dateDepart, Principal principal) {
        Salarie s = salaries.findById(salarieId).orElseThrow();
        var dr = service.calculer(s, motif);
        Depart d = new Depart();
        d.setSalarieId(salarieId); d.setDateDepart(LocalDate.parse(dateDepart));
        d.setMotif(motif); d.setIndemnite(dr.total());
        departs.save(d);
        s.setStatut("Sorti"); salaries.save(s);
        contrats.findBySalarieId(salarieId).forEach(c -> { c.setStatut("Clôturé"); contrats.save(c); });
        audit.tracer(principal == null ? "system" : principal.getName(),
                "Départ validé (W5)", motif + " — " + dr.total() + " FCFA", s.getSociete());
        return "redirect:/departs";
    }
}
