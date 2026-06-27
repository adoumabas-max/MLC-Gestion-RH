package cm.mlc.rh.repository;
import cm.mlc.rh.domain.Conge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CongeRepository extends JpaRepository<Conge, Long> {

    /** Somme des jours de congés validés pour un salarié (0 si aucun). */
    @Query("select coalesce(sum(c.jours), 0) from Conge c " +
           "where c.salarieId = :salarieId and c.statut = :statut")
    int sommeJours(@Param("salarieId") Long salarieId, @Param("statut") String statut);
}
