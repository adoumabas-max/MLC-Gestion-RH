package cm.mlc.rh.api;

import cm.mlc.rh.domain.Salarie;
import cm.mlc.rh.repository.SalarieRepository;
import cm.mlc.rh.service.PaieService;
import cm.mlc.rh.service.PaieResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** API REST sécurisée JWT — exemples salariés + calcul de paie. */
@RestController
@RequestMapping("/api/v1")
public class SalarieApiController {

    private final SalarieRepository repo;
    private final PaieService paie;

    public SalarieApiController(SalarieRepository repo, PaieService paie) { this.repo = repo; this.paie = paie; }

    @GetMapping("/salaries")
    @PreAuthorize("hasAnyRole('DRH','PAIE','AUDITEUR')")
    public List<Salarie> liste() { return repo.findAll(); }

    @GetMapping("/salaries/{id}/paie")
    @PreAuthorize("hasAnyRole('DRH','PAIE','AUDITEUR')")
    public PaieResult paie(@PathVariable Long id) {
        var s = repo.findById(id).orElseThrow();
        return paie.calculer(s.getSalaireBase());
    }
}
