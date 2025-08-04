package com.polarbookshop.catalogservice.web;

import com.polarbookshop.catalogservice.config.SecurityConfig;
import com.polarbookshop.catalogservice.domain.BookNotFoundException;
import com.polarbookshop.catalogservice.domain.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//chargement d'un mock de container de servlet et initialisant un contexte d'application web contenant uniquement le bean BookController
@WebMvcTest(BookController.class)
@Import(SecurityConfig.class) // config de la protection des EP exposés par le contrôleur.
public class BookControllerMvcTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean // ajout du mock de BookService dans le contexte d'application web Spring
    private BookService bookService;

    @Test
    void whenGetBookNotExistingThenShouldReturnNotFound() throws Exception {
        String isbn = "7894561230";
        //définition du comportement pour le bean BookService lorsqu'on demande un livre qui n'existe pas
        given(bookService.viewBookDetails(isbn))
                .willThrow(BookNotFoundException.class);

        //lorsqu'on émet une requête pour obtenir un livre qui n'existe pas, on doit recevoir le code 404
        mockMvc.perform(get("/books/"+isbn))
                .andExpect(status().isNotFound());
    }
    //test que l'employé est autorisé à supprimer un livre
    @Test
    void whenDeleteBookWithEmployeeRoleThenShouldReturn204() throws Exception {
        var isbn = "789456123";
        //requête de délétion modifiée, intégrant un JWT access-token "mocké" associé au rôle employee
        mockMvc.perform(delete("/books/"+isbn)
                    .with(SecurityMockMvcRequestPostProcessors.jwt() //ajout d'un token
                        .authorities(new SimpleGrantedAuthority("ROLE_employee"))))//assignation de l'autorité ROLE_employee
                .andExpect(status().isNoContent());
    }
    //test qu'un client n'est pas autorisé à supprimer un livre
    @Test
    void whenDeleteBookWithCustomerRoleThenShouldReturn403() throws Exception {
        var isbn = "789456123";
        mockMvc.perform(delete("/books/"+isbn)
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                    .authorities(new SimpleGrantedAuthority("ROLE_customer"))))
                .andExpect(status().isForbidden());
    }
    //test du réponde 401 (non authorisé) si l'utilisateur n'est pas authentifié
    @Test
    void whenDeleteBookNotAuthenticatedThenShouldReturn401() throws Exception {
        var isbn = "789456123";
        mockMvc.perform(delete("/books/"+isbn))
                .andExpect(status().isUnauthorized());
    }
}
