package cm.mlc.rh.security;

import cm.mlc.rh.repository.UtilisateurRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UtilisateurRepository repo;
    public CustomUserDetailsService(UtilisateurRepository repo) { this.repo = repo; }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        var u = repo.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur inconnu : " + login));
        return User.withUsername(u.getLogin())
                .password(u.getMotDePasse())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole())))
                .build();
    }
}
