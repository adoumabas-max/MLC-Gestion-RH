package cm.mlc.rh.web;

import cm.mlc.rh.domain.*;
import cm.mlc.rh.repository.*;
import cm.mlc.rh.report.JasperReportService;
import cm.mlc.rh.service.PaieResult;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** M10 — Documents & attestations : génération PDF (JasperReports). */
@Controller
public class DocumentController {

    private final SalarieRepository salaries;
    private final ContratRepository contrats;
    private final BulletinRepository bulletins;
    private final JasperReportService jasper;

    public DocumentController(SalarieRepository salaries, ContratRepository contrats,
                              BulletinRepository bulletins, JasperReportService jasper) {
        this.salaries = salaries; this.contrats = contrats; this.bulletins = bulletins; this.jasper = jasper;
    }

    @GetMapping("/documents")
    public String page(Model model) {
        model.addAttribute("salaries", salaries.findAll());
        return "documents/index";
    }

    @GetMapping("/documents/{id}/certificat.pdf")
    public ResponseEntity<byte[]> certificat(@PathVariable Long id) throws Exception {
        Salarie s = salaries.findById(id).orElseThrow();
        return pdf(jasper.certificatPdf(s), "certificat-" + s.getMatricule() + ".pdf");
    }

    @GetMapping("/documents/{id}/contrat.pdf")
    public ResponseEntity<byte[]> contrat(@PathVariable Long id) throws Exception {
        Salarie s = salaries.findById(id).orElseThrow();
        List<Contrat> cs = contrats.findBySalarieId(id);
        Contrat c = cs.isEmpty() ? null : cs.get(0);
        String type = c == null ? "CDI" : c.getType();
        String debut = c == null || c.getDebut() == null ? "" : c.getDebut().toString();
        Integer essai = c == null ? 0 : c.getEssaiMois();
        long sal = s.getSalaireBase() == null ? 0 : s.getSalaireBase();
        return pdf(jasper.contratPdf(s, type, debut, essai, sal), "contrat-" + s.getMatricule() + ".pdf");
    }

    @GetMapping("/paie/bulletin/{id}.pdf")
    public ResponseEntity<byte[]> bulletin(@PathVariable Long id) throws Exception {
        Bulletin b = bulletins.findById(id).orElseThrow();
        Salarie s = salaries.findById(b.getSalarieId()).orElseThrow();
        PaieResult r = new PaieResult(nz(b.getSbt()), nz(b.getPvidSal()), nz(b.getIrpp()), nz(b.getCac()),
                nz(b.getCfcSal()), nz(b.getRav()), nz(b.getTdl()), nz(b.getTotalRetenues()), nz(b.getNetAPayer()),
                nz(b.getPvidPat()), nz(b.getAllocFam()), nz(b.getAccident()), nz(b.getCfcPat()), nz(b.getFne()),
                nz(b.getTotalPatronal()), nz(b.getCoutEmployeur()));
        return pdf(jasper.bulletinPdf(s, r, b.getPeriode()), "bulletin-" + s.getMatricule() + "-" + b.getPeriode() + ".pdf");
    }

    private long nz(Long v) { return v == null ? 0 : v; }

    private ResponseEntity<byte[]> pdf(byte[] data, String nom) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_PDF);
        h.setContentDispositionFormData("inline", nom);
        return new ResponseEntity<>(data, h, HttpStatus.OK);
    }
}
