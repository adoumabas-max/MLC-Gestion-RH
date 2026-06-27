package cm.mlc.rh.service;

import org.springframework.stereotype.Service;

/**
 * Fournit les paramètres légaux (M15). Par défaut, valeurs calibrées sur le cas de référence.
 * Extension possible : charger/surcharger depuis la table 'parametre' (versionnée par date d'effet).
 */
@Service
public class ParametreService {
    private Parametres courant = Parametres.defaut();
    public Parametres courant() { return courant; }
    public void remplacer(Parametres p) { this.courant = p; }
}
