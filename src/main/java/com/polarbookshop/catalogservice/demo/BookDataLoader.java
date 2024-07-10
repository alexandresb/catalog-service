package com.polarbookshop.catalogservice.demo;

import com.polarbookshop.catalogservice.domain.Book;
import com.polarbookshop.catalogservice.domain.BookRepository;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("testdata") //spécifie que le bean appartient au profil testdata - bean chargé que si ce profil est activé
//alternative à l'utilisation des profils comme feature flag
//@ConditionalOnProperty(name="polar.testdata.enabled", havingValue = "true")
public class BookDataLoader {
    private final BookRepository bookRepository;

    public BookDataLoader(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    //listener déclenché une fois l'application prête (phase de démarrage terminée)
    //lorsque le démarrage est terminée le container envoie un event ApplicationReadyEvent
    // déclenchant les callbacks définis pour réagir à cet event
    @EventListener(ApplicationReadyEvent.class)
    public void loadBookTestData() {
        bookRepository.deleteAll();//pour partir d'une table vierge pour notamment éviter la violation de  la contrainte d'unicité pour l'ISBN

        //utilisation de la factory method of pour créer un Record Book
        var book1 = Book.of("1234567891", "Northern Lights", "Lyra Silverstar", 9.90);
        var book2 = Book.of("1234567892", "Polar Journey", "Iorek Polarson", 12.90);
        //les 2 opérations de sauvegarde se font au sein de la même transaction
        bookRepository.saveAll(List.of(book1, book2));

    }
}
