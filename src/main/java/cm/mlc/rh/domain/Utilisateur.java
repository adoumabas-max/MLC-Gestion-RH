package cm.mlc.rh.domain;
import jakarta.persistence.*;
import lombok.*;

@Entity @Getter @Setter @NoArgsConstructor
public class Utilisateur {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(unique = true) private String login;
    private String motDePasse;     // BCrypt
    private String role;           // DRH / PAIE / MANAGER / SALARIE / AUDITEUR
    private String societe;
}
