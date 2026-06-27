package cm.mlc.rh.repository;
import cm.mlc.rh.domain.Temps;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TempsRepository extends JpaRepository<Temps, Long> {
    Optional<Temps> findBySalarieIdAndPeriode(Long salarieId, String periode);
}
