package com.polarbookshop.catalogservice.domain;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class BookValidationTests {
    private static Validator validator;

    @BeforeAll
    static void setUp(){
        //obtention d'une instance Validator pour tester la validation des contraintes appliquées sur Book
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {//try with non présent dans le livre
            validator = factory.getValidator();
        }
    }

    @Test
    void whenAllFieldsCorrectThenValidationSucceeds(){
        //var : type local déduit depuis l'instance Book assignée
        var book = Book.of("1234567890","Title","Author", 9.90,"Polarsophia");
        Set<ConstraintViolation<Book>> violations = validator.validate(book);
        assertThat(violations).isEmpty();//aucune contrainte n'est violée.

    }

    //IJ peut proposer la complétion du nom de la méthode - globalement cette méthode a été implémenté via le sys d'autocomplétion
    @Test
    void whenIsbnDefinedButIncorrectThenValidationFails(){
        var book = Book.of("a234567890","Title","Author",9.90,"Polarsophia");
        Set<ConstraintViolation<Book>> violations = validator.validate(book);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("The ISBN format must be valid.");
    }
}
