package cm.mlc.rh.web;

import cm.mlc.rh.domain.Bulletin;
import cm.mlc.rh.domain.Salarie;
import cm.mlc.rh.repository.BulletinRepository;
import cm.mlc.rh.repository.SalarieRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Collectors;

/** M07 CNPS/DIPE et M08 Fiscal/DSF — agrégations sur les bulletins. */
@Controller
public class DeclarationController {

    private final BulletinRepository bulletins;
    private final SalarieRepository salaries;

    public DeclarationController(BulletinRepository bulletins, SalarieRepository salaries) {
        this.bulletins = bulletins; this.salaries = salaries;
    }

    private long nz(Long v) { return v == null ? 0 : v; }

    /** Ligne nominative DIPE pour un salarié (cotisations CNPS). */
    public record DipeLigne(String matricule, String matCnps, String nom,
                            long assiette, long pvidSal, long pvidPat, long allocFam, long accident) {
        public long pvidTotal() { return pvidSal + pvidPat; }
        public long total() { return pvidSal + pvidPat + allocFam + accident; }
    }

    /** Ligne récapitulative DSF annuelle pour un salarié. */
    public record DsfLigne(String matricule, String nom, int mois,
                           long brut, long irpp, long cac, long cfc, long rav, long tdl, long net) {}

    @GetMapping("/dipe")
    public String dipe(@RequestParam(required = false) String periode, Model model) {
        var tous = bulletins.findAll();

        // Synthèse mensuelle (toutes périodes) : PVID + alloc. fam. + accident.
        Map<String, long[]> parPeriode = new TreeMap<>(Comparator.reverseOrder());
        for (Bulletin b : tous) {
            long[] t = parPeriode.computeIfAbsent(b.getPeriode(), k -> new long[4]);
            t[0]+=nz(b.getPvidSal()); t[1]+=nz(b.getPvidPat()); t[2]+=nz(b.getAllocFam()); t[3]+=nz(b.getAccident());
        }
        model.addAttribute("periodes", parPeriode);

        List<String> dispo = parPeriode.keySet().stream().toList();
        String per = (periode != null && !periode.isBlank()) ? periode
                   : (dispo.isEmpty() ? null : dispo.get(0));
        model.addAttribute("listePeriodes", dispo);
        model.addAttribute("periodeSel", per);

        // DIPE nominatif pour la période sélectionnée.
        List<DipeLigne> lignes = new ArrayList<>();
        if (per != null) {
            for (Bulletin b : tous) {
                if (!per.equals(b.getPeriode())) continue;
                Salarie s = salaries.findById(b.getSalarieId()).orElse(null);
                lignes.add(new DipeLigne(
                        s == null ? "?" : s.getMatricule(),
                        s == null ? "" : (s.getMatCnps() == null ? "" : s.getMatCnps()),
                        s == null ? "" : (s.getNom() + " " + (s.getPrenom() == null ? "" : s.getPrenom())).trim(),
                        nz(b.getSbt()), nz(b.getPvidSal()), nz(b.getPvidPat()), nz(b.getAllocFam()), nz(b.getAccident())));
            }
        }
        model.addAttribute("lignes", lignes);
        model.addAttribute("totAssiette", lignes.stream().mapToLong(DipeLigne::assiette).sum());
        model.addAttribute("totPvidSal", lignes.stream().mapToLong(DipeLigne::pvidSal).sum());
        model.addAttribute("totPvidPat", lignes.stream().mapToLong(DipeLigne::pvidPat).sum());
        model.addAttribute("totAllocFam", lignes.stream().mapToLong(DipeLigne::allocFam).sum());
        model.addAttribute("totAccident", lignes.stream().mapToLong(DipeLigne::accident).sum());
        model.addAttribute("totCnps", lignes.stream().mapToLong(DipeLigne::total).sum());
        return "dipe/list";
    }

    @GetMapping("/dsf")
    public String dsf(@RequestParam(required = false) String annee, Model model) {
        var tous = bulletins.findAll();

        List<String> annees = tous.stream()
                .map(b -> b.getPeriode() == null ? "" : b.getPeriode())
                .filter(p -> p.length() >= 4).map(p -> p.substring(0, 4))
                .distinct().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
        String an = (annee != null && !annee.isBlank()) ? annee
                  : (annees.isEmpty() ? String.valueOf(java.time.Year.now().getValue()) : annees.get(0));
        model.addAttribute("annees", annees);
        model.addAttribute("anneeSel", an);

        // Agrégation annuelle par salarié : [mois,brut,irpp,cac,cfc,rav,tdl,net].
        Map<Long, long[]> parSal = new LinkedHashMap<>();
        for (Bulletin b : tous) {
            if (b.getPeriode() == null || !b.getPeriode().startsWith(an)) continue;
            long[] a = parSal.computeIfAbsent(b.getSalarieId(), k -> new long[8]);
            a[0]++; a[1]+=nz(b.getSbt()); a[2]+=nz(b.getIrpp()); a[3]+=nz(b.getCac());
            a[4]+=nz(b.getCfcSal())+nz(b.getCfcPat()); a[5]+=nz(b.getRav()); a[6]+=nz(b.getTdl()); a[7]+=nz(b.getNetAPayer());
        }
        List<DsfLigne> lignes = new ArrayList<>();
        long[] tot = new long[8];
        for (var e : parSal.entrySet()) {
            Salarie s = salaries.findById(e.getKey()).orElse(null);
            long[] a = e.getValue();
            for (int i = 0; i < 8; i++) tot[i] += a[i];
            lignes.add(new DsfLigne(
                    s == null ? "?" : s.getMatricule(),
                    s == null ? "" : (s.getNom() + " " + (s.getPrenom() == null ? "" : s.getPrenom())).trim(),
                    (int) a[0], a[1], a[2], a[3], a[4], a[5], a[6], a[7]));
        }
        model.addAttribute("lignes", lignes);
        model.addAttribute("totBrut", tot[1]); model.addAttribute("totIrpp", tot[2]);
        model.addAttribute("totCac", tot[3]); model.addAttribute("totCfc", tot[4]);
        model.addAttribute("totRav", tot[5]); model.addAttribute("totTdl", tot[6]);
        model.addAttribute("totNet", tot[7]);
        return "dsf/list";
    }
}
