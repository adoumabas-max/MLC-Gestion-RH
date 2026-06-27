package cm.mlc.rh.repository;
import cm.mlc.rh.domain.Bulletin;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BulletinRepository extends JpaRepository<Bulletin, Long> {
    List<Bulletin> findBySalarieIdOrderByPeriodeDesc(Long salarieId);
}
