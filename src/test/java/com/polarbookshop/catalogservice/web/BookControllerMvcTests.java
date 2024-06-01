package com.polarbookshop.catalogservice.web;

import com.polarbookshop.catalogservice.domain.BookNotFoundException;
import com.polarbookshop.catalogservice.domain.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//chargement d'un mock de container de servlet et initialisant un contexte d'application web contenant uniquement le bean BookController
@WebMvcTest(BookController.class)
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
}
