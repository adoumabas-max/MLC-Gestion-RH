package cm.mlc.rh.domain;
import jakarta.persistence.*;
import lombok.*;

@Entity @Getter @Setter @NoArgsConstructor
public class Poste {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private String societe;
    private String intitule;
    private String service;
    private String categorie;
}
