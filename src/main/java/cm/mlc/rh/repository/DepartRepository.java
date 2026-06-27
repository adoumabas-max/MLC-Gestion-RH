package cm.mlc.rh.repository;
import cm.mlc.rh.domain.Depart;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DepartRepository extends JpaRepository<Depart, Long> {
}
