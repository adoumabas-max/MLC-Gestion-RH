package cm.mlc.rh.web;

import cm.mlc.rh.repository.*;
import cm.mlc.rh.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class DashboardController {

    private final SalarieRepository salaries;
    private final BulletinRepository bulletins;
    private final PaieService paie;

    public DashboardController(SalarieRepository salaries, BulletinRepository bulletins, PaieService paie) {
        this.salaries = salaries; this.bulletins = bulletins; this.paie = paie;
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        var actifs = salaries.findAll().stream().filter(s -> "Actif".equals(s.getStatut())).toList();
        long masse = actifs.stream().mapToLong(s -> s.getSalaireBase() == null ? 0 : s.getSalaireBase()).sum();
        long cout = actifs.stream().mapToLong(s -> paie.calculer(s.getSalaireBase()).coutEmployeur()).sum();
        // Auto-test paie (cas de référence)
        PaieResult ref = paie.calculer(350_000);
        boolean conforme = ref.netAPayer() == 302_792 && ref.coutEmployeur() == 406_700;

        model.addAttribute("nbActifs", actifs.size());
        model.addAttribute("masse", masse);
        model.addAttribute("cout", cout);
        model.addAttribute("nbBulletins", bulletins.count());
        model.addAttribute("conforme", conforme);
        model.addAttribute("refNet", ref.netAPayer());
        model.addAttribute("refCout", ref.coutEmployeur());
        return "dashboard";
    }
}
