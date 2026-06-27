package cm.mlc.rh.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Mappe la page de connexion personnalisée déclarée dans {@link SecurityConfig}
 * ({@code formLogin().loginPage("/login")}) sur le template {@code login.html}.
 * Sans cela, {@code GET /login} ne correspond à aucun handler et renvoie 404.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("login");
    }
}
