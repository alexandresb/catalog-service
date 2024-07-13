package com.polarbookshop.catalogservice.domain;

import org.springframework.stereotype.Service;

@Service //Annotation stéréotype déclarant la classe comme bean managé. Documente comme composant de logique métier
public class BookService {
    private final BookRepository bookRepository;

    //injection par constructeur
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Iterable<Book> viewBookList() {
        return bookRepository.findAll();
    }

    public Book viewBookDetails(String isbn) {
        return bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new BookNotFoundException(isbn));//expression lambda implémentant un supplier (prog fonctionnelle)
    }

    public Book addBookToCatalog(Book book) {
        if (bookRepository.existsByIsbn(book.isbn())) {
            throw new BookAlreadyExistsException(book.isbn());
        }
        return bookRepository.save(book);
    }

    public void removeBookFromCatalog(String isbn) {
        bookRepository.deleteByIsbn(isbn);
    }

    public Book editBookDetails(String isbn, Book book) {
        return bookRepository.findByIsbn(isbn)
                .map(existingBook -> {//existingBook = le livre optionnel retourné par findByIsbn
                    var bookToUpdate = new Book(
                            existingBook.id(),
                            existingBook.isbn(),
                            book.title(),
                            book.author(),
                            book.price(),
                            book.publisher(),
                            existingBook.createdDate(),
                            existingBook.lastModifiedDate(),
                            existingBook.version());
                    return bookRepository.save(bookToUpdate);
                }).orElseGet(()->addBookToCatalog(book));
                //orElseGet retourne le livre mis à jour embarqué dans Optional s'il existe sinon retourne
                //le nouveau livre sauvegardé
    }

}
