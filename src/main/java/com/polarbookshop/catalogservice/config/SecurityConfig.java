package com.polarbookshop.catalogservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration(proxyBeanMethods = false)
//@EnableWebSecurity
public class SecurityConfig {

    //définition du bean configurant les filtres de sécurité
    @Bean
    SecurityFilterChain securityFilterChain (HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(authorize->authorize
                                                        .requestMatchers(HttpMethod.GET, "/","/books/**")
                                                        .permitAll().anyRequest()
                                                                        .hasRole("employee"))//seuls les employés peuvent accéder aux APIs protégées
                .oauth2ResourceServer(oauth2-> oauth2.jwt(Customizer.withDefaults()))//support de l'authentification JWT
                .sessionManagement(sessionManagement-> sessionManagement
                                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS))//chaque requête doit inclure l'access token pour rester stateless
                .csrf(AbstractHttpConfigurer::disable)//désactivation de la protection csrf car pas de comm entre un navigateur et catalog-service
                .build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        //convertisseur en grant authorities
        var jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_"); //ajout du préfixe ROLE à la valeur de chaque autoritée extraite
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("roles"); //extraction des valeurs de la claim roles pour créer les objets GrantAuthority

        //le personnalise seulement l'extraction des rôles dans le mécanisme de conversion du token JWT en types Spring Security
        var jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }
}
