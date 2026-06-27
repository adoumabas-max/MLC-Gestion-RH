package cm.mlc.rh.domain;
import jakarta.persistence.*;
import lombok.*;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Societe {
    @Id private String code;            // 'MLC', 'MLC RCA', 'TRANSEXPRESS'
    private String raison;
    private String pays = "Cameroun";
}
