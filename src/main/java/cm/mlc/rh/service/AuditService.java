package cm.mlc.rh.service;

import cm.mlc.rh.domain.Audit;
import cm.mlc.rh.repository.AuditRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

/** Journal d'audit horodaté (exigence ISO 9001 - 7.5 informations documentées). */
@Service
public class AuditService {
    private final AuditRepository repo;
    public AuditService(AuditRepository repo) { this.repo = repo; }

    public void tracer(String acteur, String action, String detail, String societe) {
        Audit a = new Audit();
        a.setHorodatage(LocalDateTime.now());
        a.setActeur(acteur); a.setAction(action); a.setDetail(detail); a.setSociete(societe);
        repo.save(a);
    }
}
