package cm.mlc.rh.domain;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Getter @Setter @NoArgsConstructor
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"salarieId","periode"}))
public class Bulletin {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Long salarieId;
    private String periode;        // 'AAAA-MM'
    private Long sbt, pvidSal, irpp, cac, cfcSal, rav, tdl, totalRetenues, netAPayer;
    private Long pvidPat, allocFam, accident, cfcPat, fne, totalPatronal, coutEmployeur;
    private LocalDateTime valideLe;
}
