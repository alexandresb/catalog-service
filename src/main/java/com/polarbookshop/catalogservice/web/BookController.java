package com.polarbookshop.catalogservice.web;

import com.polarbookshop.catalogservice.domain.Book;
import com.polarbookshop.catalogservice.domain.BookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

//Définie un bean pouvant gérer les requêtes http -donc communiquer avec la DispatcherServlet
//hérite de @Component. La classe sera donc découverte lors du component scanning pour créer un bean et l'ajouter un contexte Spring
//déclare donc un contrôleur REST managé par Spring
@RestController
@RequestMapping("books") //URI de base du chemin vers la ressource
public class BookController {
    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }
    @GetMapping
    public Iterable<Book> get(){
        return bookService.viewBookList();
    }

    //méthode handdler gérant les requête GET pour le chemin relatif books/{isbn}
    @GetMapping("{isbn}")//la variable template est ajouté à l'URI de base déclarée sur la classe
    public Book getByIsbn(@PathVariable String isbn){//lie la variable template au paramètre de la méthode
        return bookService.viewBookDetails(isbn);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Book post(@Valid @RequestBody Book book){//lie/mappe le corps de la requête au paramètre de type Book
        return bookService.addBookToCatalog(book);
    }

    @DeleteMapping("{isbn}")
    @ResponseStatus(HttpStatus.NO_CONTENT)//code 204 si la délétion a réussi
    public void delete(@PathVariable String isbn){
        bookService.removeBookFromCatalog(isbn);
    }

    @PutMapping("{isbn}")
    public Book put(@PathVariable String isbn,@Valid @RequestBody Book book){
        return bookService.editBookDetails(isbn,book);
    }
}
