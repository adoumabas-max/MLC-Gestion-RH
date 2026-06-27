package cm.mlc.rh.domain;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Getter @Setter @NoArgsConstructor
public class Audit {               // journal d'audit horodaté (ISO 9001 - 7.5)
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private LocalDateTime horodatage;
    private String acteur;
    private String action;
    @Column(columnDefinition = "TEXT") private String detail;
    private String societe;
}
