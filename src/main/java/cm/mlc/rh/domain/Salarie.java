package cm.mlc.rh.domain;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Entity @Getter @Setter @NoArgsConstructor
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"societe","matricule"}))
public class Salarie {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @NotBlank private String societe;
    @NotBlank private String matricule;
    private String matCnps;
    @NotBlank private String nom;
    private String prenom;
    private LocalDate naissance;
    private String sexe;
    private String nationalite;
    private String situation;
    private int enfants;
    private String poste;
    private String service;
    private String categorie;
    private LocalDate embauche;
    @NotNull @Min(60000)            // SMIG : contrôle bloquant (Art. SMIG)
    private Long salaireBase;
    private String statut = "Actif"; // Actif / Sorti
    private String tel;
}
