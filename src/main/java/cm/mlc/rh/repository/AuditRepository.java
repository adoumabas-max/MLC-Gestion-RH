package cm.mlc.rh.repository;
import cm.mlc.rh.domain.Audit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditRepository extends JpaRepository<Audit, Long> {
    List<Audit> findTop60ByOrderByHorodatageDesc();
}
