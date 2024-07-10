package com.polarbookshop.catalogservice.domain;

import com.polarbookshop.catalogservice.config.DataConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

//@DataJdbcTest chaque méthode de test est incluse dans une transaction se terminant par un rollback
@DataJdbcTest//chargement des entités et repos dans le contexte +configuration du JdbcTemplate pour effectuer les tests
@Import(DataConfig.class) //importation de la classe activant l'audit JDBC
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)//désactivation de la configuration d'une DB embarquée car on utilise tc
@ActiveProfiles("integration")//activation du profil pour les tests pour charger l'url de la basepostgres chargée par tc
public class BookRepositoryJdbcTests {
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private JdbcAggregateTemplate jdbcAggregateTemplate;

    @Test
    void findBookByIsbnWhenExisting(){
        var bookIsbn="1234561237";
        var book = Book.of(bookIsbn,"Title","Author",12.90);
        //s'exécute dans la transaction démarquant la méthode @Test
        jdbcAggregateTemplate.insert(book);//pour "configurer / charger" les données sans utiliser le repository qui est testé
        Optional<Book> actualBook = bookRepository.findByIsbn(bookIsbn);

        assertThat(actualBook).isPresent();
        assertThat(actualBook.get().isbn()).isEqualTo(book.isbn());

    }

    @Test
    void findBookByIsbnWhenNotExisting(){
        //note la lecture par le repository ne vérfie pas la validation des champs (ex :taille ISBN)
        var bookIsbn="1234561235";
        Optional<Book> actualBook = bookRepository.findByIsbn(bookIsbn);
        assertThat(actualBook).isEmpty();
    }

    @Test
    void findAllBooks(){
        var book1 = Book.of("1234561235","Title","Author",12.90);
        var book2 = Book.of("1234561236","another Title","Author",12.90);
        //insertion de données de test dans la base containerisée postgres managée par tc
        jdbcAggregateTemplate.insert(book1);
        jdbcAggregateTemplate.insert(book2);

        Iterable<Book> actualBooks = bookRepository.findAll();
        //vérification que les livres retrouvés correspondent bien aux livres persistés
        assertThat(StreamSupport.stream(actualBooks.spliterator(), true)
                //filter retourne une stream contenant les livres validant le prédiquat d'égalité de l'ISBN
                .filter(book->book.isbn().equals(book1.isbn()) || book.isbn().equals(book2.isbn()))
                .collect(Collectors.toList())).hasSize(2);
    }

    @Test
    void existsByIsbnWhenExisting(){
        var bookIsbn="1234561237";
        var book = Book.of(bookIsbn,"Title","Author",12.90);
        jdbcAggregateTemplate.insert(book);

        boolean exists = bookRepository.existsByIsbn(bookIsbn);
        assertThat(exists).isTrue();
    }

    @Test
    void existsByIsbnWhenNotExisting(){
        var bookIsbn="1234561235";
        boolean exists = bookRepository.existsByIsbn(bookIsbn);
        assertThat(exists).isFalse();
    }

    @Test
    void deleteByIsbn(){
        var bookIsbn="1234561237";
        var book = Book.of(bookIsbn,"Title","Author",12.90);
        var persistedBook = jdbcAggregateTemplate.insert(book);//persistedBook record contenant l'id du livre suite à son insertion en base

        bookRepository.deleteByIsbn(bookIsbn);

        assertThat(jdbcAggregateTemplate.findById(persistedBook.id(),Book.class)).isNull();


    }


}
