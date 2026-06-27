package cm.mlc.rh.domain;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity @Getter @Setter @NoArgsConstructor
public class Discipline {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Long salarieId;
    private LocalDate dateFaits;
    @Column(columnDefinition = "TEXT") private String motif;
    @Column(columnDefinition = "TEXT") private String reponse;
    private String sanction;       // mise à pied <= 8 jours (Art. 29 CT)
}
