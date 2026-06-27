package cm.mlc.rh.domain;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity @Getter @Setter @NoArgsConstructor
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"cle","dateEffet"}))
public class Parametre {           // M15 : source unique de vérité, versionnée
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private String cle;
    private String valeur;
    private LocalDate dateEffet;
}
