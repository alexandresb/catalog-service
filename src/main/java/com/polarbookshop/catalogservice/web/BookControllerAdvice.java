package com.polarbookshop.catalogservice.web;

import com.polarbookshop.catalogservice.domain.BookAlreadyExistsException;
import com.polarbookshop.catalogservice.domain.BookNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

//advice centralisant la gestion des exceptions au niveau du contrôleur REST BookController
@RestControllerAdvice //déclaration d'un Advice lié à contrôleur Spring
public class BookControllerAdvice {
    @ExceptionHandler(BookNotFoundException.class)// déclare une méthode interceptant les exception BNFE
    @ResponseStatus(HttpStatus.NOT_FOUND) //définit le code de statut retourné par la réponse (404)
    String bookNotFoundHandler(BookNotFoundException ex){
        return ex.getMessage();// msg = "The book with isbn ... was not found."
    }

    @ExceptionHandler(BookAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY) //statut 422
    String bookAlreadyExistsHandler(BookAlreadyExistsException ex){
        return ex.getMessage();
    }

    //gère les erreurs de validation de Book
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // statut 400
    Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex){
        var errors = new HashMap<String, String>();
        //récupération de la liste de violation de contrainte dans l'exception
        //BindingResult est une interface Spring de l'API de validation'
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName,errorMessage);
        });
        return errors;
    }

}
