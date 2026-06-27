-- Données d'amorçage (livrable 2 — seed).
INSERT INTO societe(code, raison, pays) VALUES
 ('MLC','Mercure Logistics SARL','Cameroun'),
 ('MLC RCA','MLC Centrafrique','Centrafrique'),
 ('TRANSEXPRESS','TransExpress SARL','Cameroun');

INSERT INTO poste(societe,intitule,service,categorie) VALUES
 ('MLC','Déclarant en douane','Exploitation','V'),
 ('MLC','Chauffeur corridor','Transport','III'),
 ('MLC','Responsable des opérations','Direction','IX');

INSERT INTO salarie(societe,matricule,mat_cnps,nom,prenom,naissance,sexe,nationalite,situation,enfants,poste,service,categorie,embauche,salaire_base,statut,tel)
 VALUES ('MLC','MLC-001','CM-100200','NGUE','Paul','1990-04-12','M','Camerounaise','Marié',2,'Déclarant en douane','Exploitation','V','2025-02-01',350000,'Actif','690000000');

INSERT INTO contrat(salarie_id,type,debut,essai_mois,salaire,statut) VALUES (1,'CDI','2025-02-01',3,350000,'En cours');

-- Paramètres légaux (M15) — versionnés
INSERT INTO parametre(cle,valeur,date_effet) VALUES
 ('smig','60000','2026-01-01'),('plafond_cnps','750000','2026-01-01'),
 ('pvid','0.042','2026-01-01'),('cac','0.10','2026-01-01');

-- Comptes par rôle (mots de passe BCrypt). Identifiants par défaut : drh/drh123, paie/paie123, audit/audit123
INSERT INTO utilisateur(login,mot_de_passe,role,societe) VALUES
 ('drh','$2b$10$mnD4HkhJK7RV7XqiahYmpeDNDd8tHtCqRGVpMpwnEGwmoyO7IdhZa','DRH','MLC'),
 ('paie','$2b$10$HRqkmwjEDBd0S1V4uZFjnOFkpiETFRfuYfF9DJm3qCn3jmrhpilkG','PAIE','MLC'),
 ('audit','$2b$10$q9dh0dy7c3hYUA/ToCUu8.jYIZyFF5NesvJxDaxJepYxmjz65BnS2','AUDITEUR','MLC');
