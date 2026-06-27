package cm.mlc.rh.domain;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity @Getter @Setter @NoArgsConstructor
public class Conge {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Long salarieId;
    private String type;           // Congé annuel / Maternité / Absence (Art. 84/89 CT)
    private LocalDate debut;
    private Integer jours;
    private String statut;
}
