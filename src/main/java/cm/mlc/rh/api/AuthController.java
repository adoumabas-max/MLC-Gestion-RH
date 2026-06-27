package cm.mlc.rh.api;

import cm.mlc.rh.repository.UtilisateurRepository;
import cm.mlc.rh.security.JwtTokenProvider;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** Authentification API : émet un JWT à partir d'un couple login / mot de passe. */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UtilisateurRepository repo;
    private final PasswordEncoder encoder;
    private final JwtTokenProvider provider;

    public AuthController(UtilisateurRepository repo, PasswordEncoder encoder, JwtTokenProvider provider) {
        this.repo = repo; this.encoder = encoder; this.provider = provider;
    }

    public record LoginRequest(
            @NotBlank(message = "login requis") String login,
            @NotBlank(message = "mot de passe requis") String motDePasse) {}

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        var u = repo.findByLogin(req.login()).orElse(null);
        if (u == null || !encoder.matches(req.motDePasse(), u.getMotDePasse())) {
            return ResponseEntity.status(401).body(Map.of("erreur", "Identifiants invalides"));
        }
        String token = provider.generer(u.getLogin(), u.getRole());
        return ResponseEntity.ok(Map.of("token", token, "role", u.getRole(), "type", "Bearer"));
    }
}
