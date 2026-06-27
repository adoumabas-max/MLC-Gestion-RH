package cm.mlc.rh.service;

/** Résultat détaillé d'un calcul de paie (toutes les rubriques, en FCFA entiers). */
public record PaieResult(
        long sbt,
        long pvidSal, long irpp, long cac, long cfcSal, long rav, long tdl,
        long totalRetenues, long netAPayer,
        long pvidPat, long allocFam, long accident, long cfcPat, long fne,
        long totalPatronal, long coutEmployeur) {}
