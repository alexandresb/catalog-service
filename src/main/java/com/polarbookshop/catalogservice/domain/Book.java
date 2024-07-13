package com.polarbookshop.catalogservice.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;

import java.time.Instant;

public record Book(
        @Id
        Long id,
        @NotBlank(message = "the BOOK ISBN must be defined.")//msg si contrainte violée
        @Pattern(
                regexp = "^([0-9]{10}|[0-9]{13})$", //^ = début de la chaine - $ fin de la chaine
                 message = "The ISBN format must be valid.")
         String isbn, //idenfiant
         @NotBlank(message = "The book title must be defined.")
         String title,
         @NotBlank(message = "The book author must be defined.")
         String author,
         @NotNull(message = "The book price must be defined")
         @Positive(message = "The book price must be greater than zero.")
         Double price,
         String publisher,
        //lorsqu'un event d'insertion est émis, cette annotation permet de capturer la (méta) donnée correspondant de l'event pour insérer la valeur en base
         @CreatedDate
         Instant createdDate,
         @LastModifiedDate
         Instant lastModifiedDate,
         @Version
         int version
) {
    // factory method facilitant la construction d'une NOUVELLE entité Book de type Record
    public static Book of(final String isbn, final String title, final String author, final Double price,final String publisher) {
     return new Book(null,isbn,title,author,price,publisher,null,null,0);
    }
}
