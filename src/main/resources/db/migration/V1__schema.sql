-- MLC-Gestion-RH — schéma MySQL (livrable 2). InnoDB / utf8mb4.
CREATE TABLE societe (
  code   VARCHAR(20) PRIMARY KEY,
  raison VARCHAR(120) NOT NULL,
  pays   VARCHAR(40)  NOT NULL DEFAULT 'Cameroun'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE poste (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  societe VARCHAR(20), intitule VARCHAR(120), service VARCHAR(120), categorie VARCHAR(20)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE salarie (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  societe VARCHAR(20) NOT NULL, matricule VARCHAR(30) NOT NULL, mat_cnps VARCHAR(40),
  nom VARCHAR(80) NOT NULL, prenom VARCHAR(80), naissance DATE, sexe VARCHAR(1),
  nationalite VARCHAR(60), situation VARCHAR(40), enfants INT DEFAULT 0,
  poste VARCHAR(120), service VARCHAR(120), categorie VARCHAR(20),
  embauche DATE, salaire_base BIGINT NOT NULL, statut VARCHAR(20) DEFAULT 'Actif', tel VARCHAR(30),
  UNIQUE KEY uq_mat (societe, matricule),
  CONSTRAINT fk_sal_soc FOREIGN KEY (societe) REFERENCES societe(code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE contrat (
  id BIGINT AUTO_INCREMENT PRIMARY KEY, salarie_id BIGINT NOT NULL,
  type VARCHAR(5) NOT NULL, debut DATE, fin DATE, essai_mois INT, salaire BIGINT,
  statut VARCHAR(20) DEFAULT 'En cours',
  CONSTRAINT fk_ctr_sal FOREIGN KEY (salarie_id) REFERENCES salarie(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE bulletin (
  id BIGINT AUTO_INCREMENT PRIMARY KEY, salarie_id BIGINT NOT NULL, periode VARCHAR(7) NOT NULL,
  sbt BIGINT, pvid_sal BIGINT, irpp BIGINT, cac BIGINT, cfc_sal BIGINT, rav BIGINT, tdl BIGINT,
  total_retenues BIGINT, net_a_payer BIGINT, pvid_pat BIGINT, alloc_fam BIGINT, accident BIGINT,
  cfc_pat BIGINT, fne BIGINT, total_patronal BIGINT, cout_employeur BIGINT, valide_le DATETIME,
  UNIQUE KEY uq_bul (salarie_id, periode),
  CONSTRAINT fk_bul_sal FOREIGN KEY (salarie_id) REFERENCES salarie(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE conge (id BIGINT AUTO_INCREMENT PRIMARY KEY, salarie_id BIGINT NOT NULL,
  type VARCHAR(40), debut DATE, jours INT, statut VARCHAR(20),
  CONSTRAINT fk_cg_sal FOREIGN KEY (salarie_id) REFERENCES salarie(id)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE discipline (id BIGINT AUTO_INCREMENT PRIMARY KEY, salarie_id BIGINT NOT NULL,
  date_faits DATE, motif TEXT, reponse TEXT, sanction VARCHAR(60),
  CONSTRAINT fk_dis_sal FOREIGN KEY (salarie_id) REFERENCES salarie(id)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE depart (id BIGINT AUTO_INCREMENT PRIMARY KEY, salarie_id BIGINT NOT NULL,
  date_depart DATE, motif VARCHAR(80), indemnite BIGINT,
  CONSTRAINT fk_dep_sal FOREIGN KEY (salarie_id) REFERENCES salarie(id)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE parametre (id BIGINT AUTO_INCREMENT PRIMARY KEY, cle VARCHAR(60) NOT NULL,
  valeur VARCHAR(255) NOT NULL, date_effet DATE NOT NULL,
  UNIQUE KEY uq_param (cle, date_effet)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE utilisateur (id BIGINT AUTO_INCREMENT PRIMARY KEY, login VARCHAR(60) UNIQUE,
  mot_de_passe VARCHAR(120), role VARCHAR(20), societe VARCHAR(20)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE audit (id BIGINT AUTO_INCREMENT PRIMARY KEY, horodatage DATETIME,
  acteur VARCHAR(60), action VARCHAR(120), detail TEXT, societe VARCHAR(20)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
