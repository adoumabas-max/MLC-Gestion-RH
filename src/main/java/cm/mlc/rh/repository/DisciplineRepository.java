package cm.mlc.rh.repository;
import cm.mlc.rh.domain.Discipline;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DisciplineRepository extends JpaRepository<Discipline, Long> {
}
