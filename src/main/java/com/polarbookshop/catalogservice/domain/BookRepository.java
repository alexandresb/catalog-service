package com.polarbookshop.catalogservice.domain;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

//note : les méthodes de modification définies dans CrudRepository sont par défaut @Transactional
public interface BookRepository extends CrudRepository<Book, Long> {

    //méthodes de lecture utilisant les conventions de nommage Spring Data pour générer les requêtes sous-jacentes
    Optional<Book> findByIsbn(String isbn);
    boolean existsByIsbn(String isbn);

    @Modifying //indique une op de modif d'enregistrement
    @Query("delete from Book where isbn=:isbn")//on pourrait utiliser les conv de nommage de méthodes custom
    @Transactional
    void deleteByIsbn(String isbn);
}
