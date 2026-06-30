package cm.mlc.rh.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * M16 — Caisse / Encaissement.
 *
 * Refonte moderne (Thymeleaf + Spring Boot) de l'écran « Pièce de Caisse »
 * de l'ancien logiciel EasyGC. Saisie d'un encaissement et journal des
 * règlements. Données conservées en mémoire (démonstration de l'affichage).
 */
@Controller
@RequestMapping("/encaissement")
public class EncaissementController {

    /** Ligne du journal des règlements (caisse). */
    public record Reglement(
            int numero, String date, String heure, String motif, String affectation,
            String dossier, long debit, long credit, String codutil, String piece,
            String source, String transaction, String tiers) {}

    private final List<Reglement> journal = new CopyOnWriteArrayList<>(List.of(
            new Reglement(1, "30/06/2026", "11:34", "ETABLISSEMENT BARC RCA CAMIO LTTR176BA (ESSENCE SOPRAF)", "SOPRAF", "IM0015/26", 184_000, 0, "Mht", "7835", "Caisse Prévision SOPRAF", "BARC", "Mercure Logistics Cameroun"),
            new Reglement(2, "30/06/2026", "11:31", "ETABLISSEMENT BARC RCA CAMIO LTTR176BA (ESSENCE SOPRAF)", "SOPRAF", "IM0025/26", 184_000, 0, "Mht", "7834", "Caisse Prévision SOPRAF", "BARC", "Mercure Logistics Cameroun"),
            new Reglement(3, "30/06/2026", "11:30", "ETABLISSEMENT LVI CAMION LTTR176BA", "SOPRAF", "IM0015/26", 42_000, 0, "Mht", "7833", "Caisse Prévision SOPRAF", "LVI", "Mercure Logistics Cameroun"),
            new Reglement(4, "30/06/2026", "11:29", "ETABLISSEMENT LVI CAMION LTTR176BA", "SOPRAF", "IM0025/26", 42_000, 0, "Mht", "7832", "Caisse Prévision SOPRAF", "LVI", "Mercure Logistics Cameroun"),
            new Reglement(5, "30/06/2026", "11:24", "ACHAT DE: 02 SUPPORT RADIO", "MLC", "", 45_000, 0, "Mht", "7831", "Caisse Prévision SOPRAF", "Dépenses Diverses", "Mercure Logistics Cameroun"),
            new Reglement(6, "27/06/2026", "11:55", "A SUPP SVP", "CCCG", "EX0050/26", 0, 0, "Mht", "", "Caisse Prévision SOPRAF", "Avance Facture", "YOUSSOUFA"),
            new Reglement(7, "12/06/2026", "10:56", "PREVISION ADOUM POUR STL", "MLC-CAM", "", 2_000_000, 0, "Mht", "", "Caisse Prévision SOPRAF", "Dépenses Diverses", "Mercure Logistics Cameroun"),
            new Reglement(8, "22/04/2026", "10:32", "PREVISION SUR SOLDE IDRI", "MLC-CAM", "", 1_400_000, 0, "Mht", "", "Caisse Prévision SOPRAF", "Dépenses Diverses", "Mercure Logistics Cameroun")
    ));
    private final AtomicInteger sequence = new AtomicInteger(8);
    private final AtomicInteger piece = new AtomicInteger(7835);

    private static final List<String> CREDITS = List.of("Caisse Prévision SOPRAF", "Caisse Centrale", "Banque Afriland", "Banque UBA");
    private static final List<String> TRANSACTIONS = List.of("BARC", "LVI", "Dépenses Diverses", "Avance Facture", "Encaissement Client");
    private static final List<String> AFFECTATIONS = List.of("SOPRAF", "MLC", "MLC-CAM", "CCCG");
    private static final List<String> LIGNES = List.of("01 Transit", "02 Consignation", "03 Logistique", "04 Frais Généraux");

    @GetMapping
    public String page(Model model) {
        peupler(model);
        return "encaissement/index";
    }

    @PostMapping
    public String enregistrer(
            @RequestParam(defaultValue = "") String credit,
            @RequestParam(defaultValue = "") String transaction,
            @RequestParam(defaultValue = "") String dossier,
            @RequestParam(defaultValue = "") String tiers,
            @RequestParam(defaultValue = "0") long montant,
            @RequestParam(defaultValue = "") String motif,
            @RequestParam(defaultValue = "") String affectation,
            @RequestParam(defaultValue = "Mht") String codutil,
            RedirectAttributes ra) {

        int num = sequence.incrementAndGet();
        int piec = piece.incrementAndGet();
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String heure = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        journal.add(0, new Reglement(num, date, heure,
                motif.isBlank() ? "(sans motif)" : motif,
                affectation, dossier, montant, 0, codutil,
                String.valueOf(piec),
                credit.isBlank() ? "Caisse Prévision SOPRAF" : credit,
                transaction.isBlank() ? "Dépenses Diverses" : transaction,
                tiers.isBlank() ? "Mercure Logistics Cameroun" : tiers));

        ra.addFlashAttribute("ok", "Pièce N°" + piec + " enregistrée (" + String.format("%,d", montant).replace(',', ' ') + " F).");
        return "redirect:/encaissement";
    }

    private void peupler(Model model) {
        List<Reglement> liste = new ArrayList<>(journal);
        long totalDebit = liste.stream().mapToLong(Reglement::debit).sum();
        long totalCredit = liste.stream().mapToLong(Reglement::credit).sum();

        model.addAttribute("reglements", liste);
        model.addAttribute("totalDebit", totalDebit);
        model.addAttribute("totalCredit", totalCredit);
        model.addAttribute("solde", totalDebit - totalCredit);
        model.addAttribute("nbPieces", liste.size());
        model.addAttribute("prochainePiece", piece.get() + 1);
        model.addAttribute("dateJour", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        model.addAttribute("credits", CREDITS);
        model.addAttribute("transactions", TRANSACTIONS);
        model.addAttribute("affectations", AFFECTATIONS);
        model.addAttribute("lignes", LIGNES);
    }
}
