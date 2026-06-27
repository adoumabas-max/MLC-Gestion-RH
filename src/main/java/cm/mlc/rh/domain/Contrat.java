package cm.mlc.rh.domain;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity @Getter @Setter @NoArgsConstructor
public class Contrat {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Long salarieId;
    private String type;            // CDD / CDI  (Art. 25/28 CT)
    private LocalDate debut;
    private LocalDate fin;
    private Integer essaiMois;      // Art. 34 CT
    private Long salaire;
    private String statut = "En cours"; // En cours / Clôturé
}
