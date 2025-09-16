package app.sistemaclientesrv.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração de segurança para usar apenas a funcionalidade de criptografia de senhas.
 * Desabilita autenticação automática do Spring Security mantendo apenas o BCrypt.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Bean para criptografia de senhas usando BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configuração para desabilitar toda a segurança automática do Spring Security.
     * Permite acesso livre a todos os endpoints.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Desabilita CSRF
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()) // Permite acesso a todos os endpoints
                .httpBasic(basic -> basic.disable()) // Desabilita autenticação HTTP básica
                .formLogin(form -> form.disable()) // Desabilita formulário de login
                .logout(logout -> logout.disable()); // Desabilita logout automático

        return http.build();
    }
}