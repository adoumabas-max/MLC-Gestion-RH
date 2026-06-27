package cm.mlc.rh.repository;
import cm.mlc.rh.domain.Salarie;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SalarieRepository extends JpaRepository<Salarie, Long> {
    List<Salarie> findBySociete(String societe);
    List<Salarie> findBySocieteAndStatut(String societe, String statut);
}
