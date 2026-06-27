package cm.mlc.rh.web;

import cm.mlc.rh.domain.Salarie;
import cm.mlc.rh.repository.SalarieRepository;
import cm.mlc.rh.service.AuditService;
import cm.mlc.rh.service.PaieService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/salaries")
public class SalarieController {

    private final SalarieRepository repo;
    private final AuditService audit;
    private final PaieService paie;

    public SalarieController(SalarieRepository repo, AuditService audit, PaieService paie) {
        this.repo = repo; this.audit = audit; this.paie = paie;
    }

    @GetMapping
    public String liste(Model model) {
        model.addAttribute("salaries", repo.findAll());
        return "salaries/list";
    }

    @GetMapping("/nouveau")
    public String nouveau(Model model) {
        model.addAttribute("salarie", new Salarie());
        model.addAttribute("smig", paie.smig());
        return "salaries/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DRH','PAIE')")
    public String enregistrer(@Valid @ModelAttribute("salarie") Salarie salarie,
                              BindingResult br, Model model, Principal principal) {
        // Contrôle SMIG bloquant (Art. SMIG)
        if (salarie.getSalaireBase() != null && !paie.salaireValide(salarie.getSalaireBase())) {
            br.rejectValue("salaireBase", "smig", "Salaire inférieur au SMIG (" + paie.smig() + " FCFA).");
        }
        if (br.hasErrors()) { model.addAttribute("smig", paie.smig()); return "salaries/form"; }
        if (salarie.getSociete() == null) salarie.setSociete("MLC");
        repo.save(salarie);
        audit.tracer(principal == null ? "system" : principal.getName(),
                "Création/MAJ salarié", salarie.getMatricule(), salarie.getSociete());
        return "redirect:/salaries";
    }
}
