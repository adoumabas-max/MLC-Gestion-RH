package cm.mlc.rh.web;

import cm.mlc.rh.domain.Bulletin;
import cm.mlc.rh.repository.*;
import cm.mlc.rh.service.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;

/** Wizard de paie W2 (simplifié) : sélection salarié -> calcul -> validation/bulletin. */
@Controller
@RequestMapping("/paie")
public class PaieController {

    private final SalarieRepository salaries;
    private final BulletinRepository bulletins;
    private final PaieService paie;
    private final AuditService audit;
    private final TempsRepository temps;

    public PaieController(SalarieRepository salaries, BulletinRepository bulletins,
                          PaieService paie, AuditService audit, TempsRepository temps) {
        this.salaries = salaries; this.bulletins = bulletins; this.paie = paie;
        this.audit = audit; this.temps = temps;
    }

    /** Majoration HS pointée en M12 pour ce salarié et cette période (0 si aucun pointage). */
    private long majorationHS(Long salarieId, String periode) {
        return temps.findBySalarieIdAndPeriode(salarieId, periode)
                .map(t -> t.getMontant() == null ? 0L : t.getMontant()).orElse(0L);
    }

    @GetMapping
    public String liste(Model model) {
        model.addAttribute("bulletins", bulletins.findAll());
        model.addAttribute("salaries", salaries.findBySocieteAndStatut("MLC", "Actif"));
        return "paie/wizard";
    }

    @PostMapping("/calculer")
    public String calculer(@RequestParam Long salarieId, @RequestParam String periode,
                           @RequestParam(defaultValue = "0") long primesImposables,
                           @RequestParam(defaultValue = "0") long primesNonImposables,
                           @RequestParam(defaultValue = "0") long majorationsHS, Model model) {
        var s = salaries.findById(salarieId).orElseThrow();
        // Report automatique de la majoration HS pointée en M12 si le champ est laissé à 0.
        if (majorationsHS == 0) majorationsHS = majorationHS(salarieId, periode);
        PaieResult r = paie.calculer(s.getSalaireBase(), primesImposables, primesNonImposables, majorationsHS);
        model.addAttribute("salarie", s);
        model.addAttribute("majorationsHS", majorationsHS);
        model.addAttribute("r", r);
        model.addAttribute("periode", periode);
        model.addAttribute("salaries", salaries.findBySocieteAndStatut("MLC", "Actif"));
        model.addAttribute("bulletins", bulletins.findAll());
        return "paie/wizard";
    }

    @PostMapping("/valider")
    @PreAuthorize("hasAnyRole('DRH','PAIE')")
    public String valider(@RequestParam Long salarieId, @RequestParam String periode, Principal principal) {
        var s = salaries.findById(salarieId).orElseThrow();
        // Le bulletin validé intègre la majoration HS pointée en M12 pour la période.
        PaieResult r = paie.calculer(s.getSalaireBase(), 0, 0, majorationHS(salarieId, periode));
        Bulletin b = new Bulletin();
        b.setSalarieId(salarieId); b.setPeriode(periode);
        b.setSbt(r.sbt()); b.setPvidSal(r.pvidSal()); b.setIrpp(r.irpp()); b.setCac(r.cac());
        b.setCfcSal(r.cfcSal()); b.setRav(r.rav()); b.setTdl(r.tdl());
        b.setTotalRetenues(r.totalRetenues()); b.setNetAPayer(r.netAPayer());
        b.setPvidPat(r.pvidPat()); b.setAllocFam(r.allocFam()); b.setAccident(r.accident());
        b.setCfcPat(r.cfcPat()); b.setFne(r.fne()); b.setTotalPatronal(r.totalPatronal());
        b.setCoutEmployeur(r.coutEmployeur()); b.setValideLe(LocalDateTime.now());
        bulletins.save(b);
        audit.tracer(principal == null ? "system" : principal.getName(),
                "Paie validée (W2)", "Période " + periode + " — " + s.getMatricule(), s.getSociete());
        return "redirect:/paie";
    }
}
