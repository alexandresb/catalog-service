package com.polarbookshop.catalogservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration //source de configurations
/* activation de l'audit des données lors de leur insertion, modification ou suppression d'enregistrement en base
Lors d'une op de persistance, modif ou suppr un callback est invoqué au sein de Spring Security pour injecter les métadonnées dans les champs annotés de Book (@CreatedDate, etc.).
Ces métadonnées seront intégrées dans un nouvel objet de type record, car immutable (cas d'insert ou update) puis la requête SQL correspondante sera générée.

*/
@EnableJdbcAuditing
public class DataConfig {

    //bean contenant le nom de l'utilisateur authentifié
    @Bean
    AuditorAware<String> auditorAware(){
        return ()-> Optional
                //extraction du SecurityContext correspondant à l'utilisateur authentifié de l'objet SecurityContextHolder
                .ofNullable(SecurityContextHolder.getContext())
                //obtention de l'objet Authentication stocké dans SecurityContext
                .map(SecurityContext::getAuthentication)
                //on filtre que les utilisateurs authentifiés. ça devrait être toujours le cas vu la protection des EP
                .filter(Authentication::isAuthenticated)
                //capture / obtention du nom de l'utilisateur authentifié
                .map(Authentication::getName);
    }
}
