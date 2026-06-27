package cm.mlc.rh.web;

import cm.mlc.rh.domain.Poste;
import cm.mlc.rh.repository.PosteRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/postes")
public class PosteController {
    private final PosteRepository repo;
    public PosteController(PosteRepository repo) { this.repo = repo; }

    @GetMapping
    public String liste(Model model) { model.addAttribute("postes", repo.findAll()); return "postes/list"; }

    @PostMapping
    @PreAuthorize("hasAnyRole('DRH','MANAGER')")
    public String ajouter(@RequestParam String intitule, @RequestParam(required=false) String service,
                          @RequestParam(required=false) String categorie) {
        Poste p = new Poste(); p.setSociete("MLC"); p.setIntitule(intitule);
        p.setService(service); p.setCategorie(categorie); repo.save(p);
        return "redirect:/postes";
    }

    @PostMapping("/{id}/supprimer")
    @PreAuthorize("hasRole('DRH')")
    public String supprimer(@PathVariable Long id) { repo.deleteById(id); return "redirect:/postes"; }
}
