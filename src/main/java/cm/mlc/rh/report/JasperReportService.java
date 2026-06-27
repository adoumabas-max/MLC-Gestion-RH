package cm.mlc.rh.report;

import cm.mlc.rh.domain.Salarie;
import cm.mlc.rh.service.PaieResult;
import net.sf.jasperreports.engine.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/** Génération des éditions PDF via JasperReports (.jrxml dans resources/reports). */
@Service
public class JasperReportService {

    private byte[] generer(String modele, Map<String, Object> params) throws JRException {
        try (InputStream is = new ClassPathResource("reports/" + modele).getInputStream()) {
            JasperReport report = JasperCompileManager.compileReport(is);
            JasperPrint print = JasperFillManager.fillReport(report, params, new JREmptyDataSource());
            return JasperExportManager.exportReportToPdf(print);
        } catch (JRException e) { throw e; }
        catch (Exception e) { throw new JRException(e); }
    }

    public byte[] bulletinPdf(Salarie s, PaieResult r, String periode) throws JRException {
        Map<String, Object> p = base(s);
        p.put("periode", periode);
        p.put("sbt", r.sbt()); p.put("totalRetenues", r.totalRetenues());
        p.put("netAPayer", r.netAPayer()); p.put("coutEmployeur", r.coutEmployeur());
        return generer("bulletin.jrxml", p);
    }

    public byte[] contratPdf(Salarie s, String type, String debut, Integer essai, long salaire) throws JRException {
        Map<String, Object> p = base(s);
        p.put("type", type); p.put("debut", debut);
        p.put("essai", essai == null ? 0 : essai); p.put("salaire", salaire);
        return generer("contrat.jrxml", p);
    }

    public byte[] certificatPdf(Salarie s) throws JRException {
        Map<String, Object> p = base(s);
        p.put("dateJour", LocalDate.now().toString());
        p.put("embauche", s.getEmbauche() == null ? "" : s.getEmbauche().toString());
        return generer("certificat.jrxml", p);
    }

    public byte[] dipePdf(String periode, long pvidSal, long pvidPat, long allocFam, long accident) throws JRException {
        Map<String, Object> p = new HashMap<>();
        p.put("societe", "MERCURE LOGISTICS SARL"); p.put("periode", periode);
        p.put("pvidSal", pvidSal); p.put("pvidPat", pvidPat);
        p.put("allocFam", allocFam); p.put("accident", accident);
        p.put("total", pvidSal + pvidPat + allocFam + accident);
        return generer("dipe.jrxml", p);
    }

    private Map<String, Object> base(Salarie s) {
        Map<String, Object> p = new HashMap<>();
        p.put("societe", "MERCURE LOGISTICS SARL");
        p.put("salarie", s.getNom() + " " + (s.getPrenom() == null ? "" : s.getPrenom()));
        p.put("matricule", s.getMatricule());
        p.put("poste", s.getPoste() == null ? "" : s.getPoste());
        return p;
    }
}
