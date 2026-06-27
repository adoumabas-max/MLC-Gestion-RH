package cm.mlc.rh.repository;
import cm.mlc.rh.domain.Contrat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ContratRepository extends JpaRepository<Contrat, Long> {
    List<Contrat> findBySalarieId(Long salarieId);
}
