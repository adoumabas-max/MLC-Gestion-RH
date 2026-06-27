package cm.mlc.rh.web;

import cm.mlc.rh.domain.Bulletin;
import cm.mlc.rh.domain.Salarie;
import cm.mlc.rh.report.JasperReportService;
import cm.mlc.rh.repository.BulletinRepository;
import cm.mlc.rh.repository.SalarieRepository;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Collectors;

/** M07 CNPS/DIPE et M08 Fiscal/DSF — agrégations sur les bulletins + éditions PDF. */
@Controller
public class DeclarationController {

    private final BulletinRepository bulletins;
    private final SalarieRepository salaries;
    private final JasperReportService jasper;

    public DeclarationController(BulletinRepository bulletins, SalarieRepository salaries,
                                 JasperReportService jasper) {
        this.bulletins = bulletins; this.salaries = salaries; this.jasper = jasper;
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

    // ---- Calculs réutilisés par les vues et par les éditions PDF ----

    private String nomComplet(Salarie s) {
        return s == null ? "" : (s.getNom() + " " + (s.getPrenom() == null ? "" : s.getPrenom())).trim();
    }

    private List<String> periodesDisponibles(List<Bulletin> tous) {
        return tous.stream().map(Bulletin::getPeriode).filter(Objects::nonNull)
                .distinct().sorted(Comparator.reverseOrder()).toList();
    }

    private List<DipeLigne> calculDipe(String periode, List<Bulletin> tous) {
        List<DipeLigne> lignes = new ArrayList<>();
        if (periode == null) return lignes;
        for (Bulletin b : tous) {
            if (!periode.equals(b.getPeriode())) continue;
            Salarie s = salaries.findById(b.getSalarieId()).orElse(null);
            lignes.add(new DipeLigne(
                    s == null ? "?" : s.getMatricule(),
                    s == null || s.getMatCnps() == null ? "" : s.getMatCnps(),
                    nomComplet(s),
                    nz(b.getSbt()), nz(b.getPvidSal()), nz(b.getPvidPat()), nz(b.getAllocFam()), nz(b.getAccident())));
        }
        return lignes;
    }

    private List<String> anneesDisponibles(List<Bulletin> tous) {
        return tous.stream().map(b -> b.getPeriode() == null ? "" : b.getPeriode())
                .filter(p -> p.length() >= 4).map(p -> p.substring(0, 4))
                .distinct().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
    }

    private List<DsfLigne> calculDsf(String annee, List<Bulletin> tous) {
        Map<Long, long[]> parSal = new LinkedHashMap<>();   // [mois,brut,irpp,cac,cfc,rav,tdl,net]
        for (Bulletin b : tous) {
            if (b.getPeriode() == null || !b.getPeriode().startsWith(annee)) continue;
            long[] a = parSal.computeIfAbsent(b.getSalarieId(), k -> new long[8]);
            a[0]++; a[1]+=nz(b.getSbt()); a[2]+=nz(b.getIrpp()); a[3]+=nz(b.getCac());
            a[4]+=nz(b.getCfcSal())+nz(b.getCfcPat()); a[5]+=nz(b.getRav()); a[6]+=nz(b.getTdl()); a[7]+=nz(b.getNetAPayer());
        }
        List<DsfLigne> lignes = new ArrayList<>();
        for (var e : parSal.entrySet()) {
            Salarie s = salaries.findById(e.getKey()).orElse(null);
            long[] a = e.getValue();
            lignes.add(new DsfLigne(s == null ? "?" : s.getMatricule(), nomComplet(s),
                    (int) a[0], a[1], a[2], a[3], a[4], a[5], a[6], a[7]));
        }
        return lignes;
    }

    // ---- Écrans HTML ----

    @GetMapping("/dipe")
    public String dipe(@RequestParam(required = false) String periode, Model model) {
        var tous = bulletins.findAll();
        Map<String, long[]> parPeriode = new TreeMap<>(Comparator.reverseOrder());
        for (Bulletin b : tous) {
            long[] t = parPeriode.computeIfAbsent(b.getPeriode(), k -> new long[4]);
            t[0]+=nz(b.getPvidSal()); t[1]+=nz(b.getPvidPat()); t[2]+=nz(b.getAllocFam()); t[3]+=nz(b.getAccident());
        }
        model.addAttribute("periodes", parPeriode);

        List<String> dispo = periodesDisponibles(tous);
        String per = (periode != null && !periode.isBlank()) ? periode : (dispo.isEmpty() ? null : dispo.get(0));
        model.addAttribute("listePeriodes", dispo);
        model.addAttribute("periodeSel", per);

        List<DipeLigne> lignes = calculDipe(per, tous);
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
        List<String> annees = anneesDisponibles(tous);
        String an = (annee != null && !annee.isBlank()) ? annee
                  : (annees.isEmpty() ? String.valueOf(java.time.Year.now().getValue()) : annees.get(0));
        model.addAttribute("annees", annees);
        model.addAttribute("anneeSel", an);

        List<DsfLigne> lignes = calculDsf(an, tous);
        model.addAttribute("lignes", lignes);
        model.addAttribute("totBrut", lignes.stream().mapToLong(DsfLigne::brut).sum());
        model.addAttribute("totIrpp", lignes.stream().mapToLong(DsfLigne::irpp).sum());
        model.addAttribute("totCac", lignes.stream().mapToLong(DsfLigne::cac).sum());
        model.addAttribute("totCfc", lignes.stream().mapToLong(DsfLigne::cfc).sum());
        model.addAttribute("totRav", lignes.stream().mapToLong(DsfLigne::rav).sum());
        model.addAttribute("totTdl", lignes.stream().mapToLong(DsfLigne::tdl).sum());
        model.addAttribute("totNet", lignes.stream().mapToLong(DsfLigne::net).sum());
        return "dsf/list";
    }

    // ---- Éditions PDF (JasperReports) ----

    @GetMapping("/dipe/{periode}.pdf")
    public ResponseEntity<byte[]> dipePdf(@PathVariable String periode) throws Exception {
        var tous = bulletins.findAll();
        List<DipeLigne> lignes = calculDipe(periode, tous);
        Collection<Map<String, ?>> rows = lignes.stream().map(l -> Map.<String, Object>of(
                "matricule", l.matricule(), "matCnps", l.matCnps(), "nom", l.nom(),
                "assiette", l.assiette(), "pvidTotal", l.pvidTotal(), "allocFam", l.allocFam(),
                "accident", l.accident(), "total", l.total())).collect(Collectors.toList());
        Map<String, Object> tot = Map.of(
                "totAssiette", lignes.stream().mapToLong(DipeLigne::assiette).sum(),
                "totPvid", lignes.stream().mapToLong(DipeLigne::pvidTotal).sum(),
                "totAllocFam", lignes.stream().mapToLong(DipeLigne::allocFam).sum(),
                "totAccident", lignes.stream().mapToLong(DipeLigne::accident).sum(),
                "totCnps", lignes.stream().mapToLong(DipeLigne::total).sum());
        return pdf(jasper.dipePdf(periode, rows, tot), "dipe-" + periode + ".pdf");
    }

    @GetMapping("/dsf/{annee}.pdf")
    public ResponseEntity<byte[]> dsfPdf(@PathVariable String annee) throws Exception {
        var tous = bulletins.findAll();
        List<DsfLigne> lignes = calculDsf(annee, tous);
        Collection<Map<String, ?>> rows = lignes.stream().map(l -> Map.<String, Object>of(
                "matricule", l.matricule(), "nom", l.nom(), "brut", l.brut(), "irpp", l.irpp(),
                "cac", l.cac(), "cfc", l.cfc(), "rav", l.rav(), "tdl", l.tdl(), "net", l.net()))
                .collect(Collectors.toList());
        Map<String, Object> tot = Map.of(
                "totBrut", lignes.stream().mapToLong(DsfLigne::brut).sum(),
                "totIrpp", lignes.stream().mapToLong(DsfLigne::irpp).sum(),
                "totCac", lignes.stream().mapToLong(DsfLigne::cac).sum(),
                "totCfc", lignes.stream().mapToLong(DsfLigne::cfc).sum(),
                "totRav", lignes.stream().mapToLong(DsfLigne::rav).sum(),
                "totTdl", lignes.stream().mapToLong(DsfLigne::tdl).sum(),
                "totNet", lignes.stream().mapToLong(DsfLigne::net).sum());
        return pdf(jasper.dsfPdf(annee, rows, tot), "dsf-" + annee + ".pdf");
    }

    private ResponseEntity<byte[]> pdf(byte[] data, String nom) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_PDF);
        h.setContentDispositionFormData("inline", nom);
        return new ResponseEntity<>(data, h, HttpStatus.OK);
    }
}
