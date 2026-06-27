package cm.mlc.rh.domain;
import jakarta.persistence.*;
import lombok.*;

/** M12 — Pointage des heures supplémentaires d'un salarié pour une période (AAAA-MM). */
@Entity @Getter @Setter @NoArgsConstructor
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"salarieId","periode"}))
public class Temps {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Long salarieId;
    private String periode;        // 'AAAA-MM'
    private Integer hs1;           // heures sup. +20% (8 premières h/sem)
    private Integer hs2;           // heures sup. +30% (au-delà)
    private Integer nuit;          // heures de nuit +50%
    private Integer dim;           // dimanche / jour férié +40%
    private Integer heures;        // total heures sup.
    private Long tauxHoraire;      // salaireBase / 173,33
    private Long montant;          // majoration à reporter en paie (composante SBT)
}
