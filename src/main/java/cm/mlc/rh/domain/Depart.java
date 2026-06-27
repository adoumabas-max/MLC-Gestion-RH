package cm.mlc.rh.domain;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity @Getter @Setter @NoArgsConstructor
public class Depart {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Long salarieId;
    private LocalDate dateDepart;
    private String motif;
    private Long indemnite;        // Art. 37 CT
}
