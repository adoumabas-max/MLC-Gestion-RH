package cm.mlc.rh.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Traduit les exceptions de la couche API REST (/api/**) en réponses JSON cohérentes,
 * au lieu de laisser remonter des 500 opaques (ex. {@code orElseThrow()} sur une ressource absente).
 */
@RestControllerAdvice(basePackages = "cm.mlc.rh.api")
public class ApiExceptionHandler {

    private record Erreur(int statut, String erreur) {}

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Erreur> introuvable(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new Erreur(404, "Ressource introuvable"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Erreur> interdit(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new Erreur(403, "Accès refusé"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Erreur> invalide(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + " : " + e.getDefaultMessage())
                .collect(Collectors.joining(" ; "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new Erreur(400, detail.isBlank() ? "Requête invalide" : detail));
    }
}
