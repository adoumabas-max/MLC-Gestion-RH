package cm.mlc.rh.web;

import cm.mlc.rh.repository.AuditRepository;
import cm.mlc.rh.service.ParametreService;
import cm.mlc.rh.service.PaieResult;
import cm.mlc.rh.service.PaieService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    private final AuditRepository audit;
    private final ParametreService parametres;
    private final PaieService paie;

    public AdminController(AuditRepository audit, ParametreService parametres, PaieService paie) {
        this.audit = audit; this.parametres = parametres; this.paie = paie;
    }

    @GetMapping("/admin")
    public String admin(Model model) {
        model.addAttribute("journal", audit.findTop60ByOrderByHorodatageDesc());
        return "admin/index";
    }

    @GetMapping("/params")
    public String params(Model model) {
        var p = parametres.courant();
        PaieResult r = paie.calculer(350_000);
        model.addAttribute("p", p);
        model.addAttribute("conforme", r.netAPayer() == 302_792 && r.coutEmployeur() == 406_700);
        model.addAttribute("net", r.netAPayer());
        model.addAttribute("cout", r.coutEmployeur());
        return "params/index";
    }
}
