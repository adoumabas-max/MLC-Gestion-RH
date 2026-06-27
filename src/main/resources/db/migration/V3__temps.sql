-- M12 — Pointage des heures supplémentaires (livrable 4 — extension).
CREATE TABLE temps (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  salarie_id BIGINT NOT NULL,
  periode CHAR(7) NOT NULL,                 -- 'AAAA-MM'
  hs1 INT DEFAULT 0, hs2 INT DEFAULT 0,
  nuit INT DEFAULT 0, dim INT DEFAULT 0,
  heures INT DEFAULT 0,
  taux_horaire BIGINT, montant BIGINT,
  UNIQUE KEY uq_temps (salarie_id, periode),
  CONSTRAINT fk_temps_sal FOREIGN KEY (salarie_id) REFERENCES salarie(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
