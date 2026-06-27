# MLC-Gestion-RH — application RH de Mercure Logistics

Mercure Logistics SARL — Transit & Logistique, Douala.
Conforme ISO 9001:2015 et au Code du travail camerounais (Loi n° 92/007), cahier des
charges **MLC-RH-CDC-001 v1.0**.

Le projet réunit les **deux variantes** prévues par les skills, partageant la **même
spécification métier** et surtout le **même moteur de paie** (cas de référence bloquant
identique : base 350 000 → **net 302 792** / **coût employeur 406 700**) :

| Variante | Techno | Fichier / dossier | Pour qui |
|----------|--------|-------------------|----------|
| **A — Mono-fichier HTML** | HTML/CSS/JS natif, hors-ligne, `localStorage` | [`MLC-Gestion-RH.html`](MLC-Gestion-RH.html) | mise en main immédiate, poste isolé, démo, clé USB |
| **B — Entreprise** | Spring Boot 3 · MySQL 8 · Thymeleaf · Security (session + JWT) · JasperReports | `pom.xml` + `src/` | déploiement serveur, multi-postes, PDF, API REST |

## Variante A — application mono-fichier (autonome)

Ouvrir **`MLC-Gestion-RH.html`** dans un navigateur (double-clic) — aucune installation,
aucun serveur, aucun appel réseau. Ou servir localement :
```bash
python3 -m http.server 8000   # puis http://localhost:8000/MLC-Gestion-RH.html
```
- 15 modules (M01–M15), 5 wizards (W1 embauche, W2 paie, W3 congés, W4 discipline, W5 départ),
  multi-sociétés (MLC / MLC RCA / TRANSEXPRESS).
- **M15 → « Rejouer l'auto-test »** vérifie le cas de référence (✅ net 302 792 / coût 406 700).
- Éditions imprimables (`@media print`) : bulletin, contrat, certificat, attestation.
- Sauvegarde par **export / import JSON** ; persistance `localStorage` (repli mémoire).

## Variante B — application d'entreprise (Spring Boot)

Spring Boot 3 · MySQL 8 · Thymeleaf · Spring Security (session + JWT) · JasperReports.

### Démarrage

1. **Base de données**
   ```sql
   CREATE DATABASE mlc_rh CHARACTER SET utf8mb4;
   CREATE USER 'mlc_rh'@'%' IDENTIFIED BY 'mlc_rh';
   GRANT ALL ON mlc_rh.* TO 'mlc_rh'@'%';
   ```
   Le schéma et les données de démonstration sont créés automatiquement par **Flyway**
   (V1__schema.sql, V2__seed.sql) au premier démarrage.

2. **Build + tests** (le test d'acceptation de paie DOIT passer)
   ```bash
   mvn clean package
   ```
   `PaieServiceTest` vérifie le cas de référence : base 350 000 → **net 302 792** /
   **coût employeur 406 700**. Le build échoue si l'écart n'est pas nul.

3. **Lancer**
   ```bash
   java -jar target/mlc-gestion-rh-1.0.0.jar      # ou : mvn spring-boot:run
   ```
   Ouvrir http://localhost:8080 — comptes : **drh/drh123**, paie/paie123, audit/audit123.

## API REST (JWT)
```bash
# 1) Obtenir un jeton
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" -d '{"login":"drh","motDePasse":"drh123"}'
# 2) Appeler une ressource protégée
curl http://localhost:8080/api/v1/salaries -H "Authorization: Bearer <TOKEN>"
```

## Couverture des livrables
- **2** Schéma MySQL (Flyway) · **3** Squelette Maven · **4** Entités + services + moteur de
  paie (+ test JUnit d'acceptation) · **5** Vues Thymeleaf + wizards **W2 paie, W3 congés,
  W4 discipline, W5 départ** · **6** Sécurité session + JWT (rôles DRH/PAIE/MANAGER/SALARIE/
  AUDITEUR, @PreAuthorize) · **7** JasperReports : **bulletin, contrat, certificat, DIPE** ·
  **9** Documentation ISO (docs/iso).
- Modules : M01 salariés, M03 paie, M04 congés, M05 discipline, M06 départs, M07 CNPS/DIPE,
  M08 Fiscal/DSF, M09 tableau de bord, M10 documents PDF, M11 postes, M14 administration
  (journal d'audit), M15 paramétrage (auto-test paie).
- Pistes d'extension : M12 temps de travail et M13 avances (entité + écrans), surcharge des
  paramètres M15 depuis la table `parametre`, application Android (livrable 8).

## Améliorations récentes
- **Correction câblage M15** : `PaieService` résout désormais les paramètres légaux via
  `ParametreService.courant()` à chaque calcul. Auparavant le bean Spring figeait les
  paramètres par défaut au démarrage et ignorait toute mise à jour M15.
- **Performance congés** : `CongeService.solde()` agrège les jours côté base
  (`CongeRepository.sommeJours`) au lieu de charger toute la table en mémoire.
- **Sécurité** : en-têtes HTTP de défense en profondeur (anti-clickjacking, anti-sniffing,
  HSTS, Referrer-Policy) ; échec au démarrage si le secret JWT fait moins de 256 bits ;
  validation `@Valid` du login API.
- **Robustesse API** : `ApiExceptionHandler` traduit les erreurs (`404/403/400`) en JSON
  cohérent au lieu de 500 opaques.
- **Page de login réparée** : `GET /login` est désormais mappé sur `login.html`
  (`WebConfig`) — la page personnalisée renvoyait auparavant 404.
- **API 401 vs 403** : une requête API non authentifiée renvoie `401 Unauthorized`
  (`AuthenticationEntryPoint`) au lieu du `403` par défaut.
- **Profil `prod`** (`application-prod.yml`) : aucun secret par défaut — le démarrage
  échoue si `JWT_SECRET` / `DB_PASSWORD` ne sont pas fournis.
- **Tests & CI** : tests de non-régression du moteur de paie (suivi M15), tests
  d'intégration des contrôleurs API et web (sécurité, JWT, validation, en-têtes,
  gestion d'erreurs) et workflow GitHub Actions (`mvn -B clean verify` sur JDK 17).
