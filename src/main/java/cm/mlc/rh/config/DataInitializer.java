package cm.mlc.rh.config;

import cm.mlc.rh.service.ParametreService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Amorçage applicatif léger (les données métier sont chargées par Flyway : V2__seed.sql). */
@Component
public class DataInitializer implements CommandLineRunner {
    private final ParametreService parametres;
    public DataInitializer(ParametreService parametres) { this.parametres = parametres; }

    @Override
    public void run(String... args) {
        // Paramètres légaux par défaut déjà calibrés (M15). Point d'extension : charge depuis 'parametre'.
        parametres.courant();
    }
}
