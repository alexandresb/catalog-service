package com.polarbookshop.catalogservice.domain;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

//DDD style repo
@Repository //bean managé -spécialisation de @Component permettant d'indiquer le rôle de la classe annotée (documentation)
public class InMemoryBookRepository implements BookRepository{
    private static final Map<String, Book> books= new ConcurrentHashMap<>();
    @Override
    public Iterable<Book> findAll() {
        return books.values();//Collection est une spécialisation d'Iterable
    }

    @Override
    public Optional<Book> findByIsbn(String isbn) {
        return existsByIsbn(isbn) ? Optional.of(books.get(isbn)) : Optional.empty();
    }

    @Override
    public boolean existsByIsbn(String isbn) {
        return books.get(isbn)!=null;
    }

    @Override
    public Book save(Book book) {
        books.put(book.isbn(),book);
        return book;
    }

    @Override
    public void deleteByIsbn(String isbn) {
        books.remove(isbn);
    }
}
