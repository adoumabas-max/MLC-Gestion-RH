package cm.mlc.rh.web;

import cm.mlc.rh.domain.Bulletin;
import cm.mlc.rh.repository.BulletinRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;
import java.util.stream.Collectors;

/** M07 CNPS/DIPE et M08 Fiscal/DSF — agrégations sur les bulletins. */
@Controller
public class DeclarationController {

    private final BulletinRepository bulletins;
    public DeclarationController(BulletinRepository bulletins) { this.bulletins = bulletins; }

    @GetMapping("/dipe")
    public String dipe(Model model) {
        Map<String, long[]> parPeriode = new TreeMap<>();
        for (Bulletin b : bulletins.findAll()) {
            long[] t = parPeriode.computeIfAbsent(b.getPeriode(), k -> new long[4]);
            t[0]+=nz(b.getPvidSal()); t[1]+=nz(b.getPvidPat()); t[2]+=nz(b.getAllocFam()); t[3]+=nz(b.getAccident());
        }
        model.addAttribute("periodes", parPeriode);
        return "dipe/list";
    }

    @GetMapping("/dsf")
    public String dsf(Model model) {
        // Récapitulatif annuel des retenues fiscales par bulletin
        model.addAttribute("bulletins", bulletins.findAll());
        return "dsf/list";
    }

    private long nz(Long v) { return v == null ? 0 : v; }
}
