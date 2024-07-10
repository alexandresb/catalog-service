package com.polarbookshop.catalogservice;

import com.polarbookshop.catalogservice.domain.Book;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

//TI utilisant un container de servlet écoutant sur un port aléatoire.
//spécifie le chargement d'un container de servlet associé avec un contexte d'application configuré via la classe amorce @SpringBootApplication
@SpringBootTest (
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("integration")
class CatalogServiceApplicationTests {

	//injection basée sur les champs
	@Autowired
	private WebTestClient webTestClient;

	/*
	à la différence des tests de la couche repository avec des classes annotés @DataJdbcTest, dans ces tests d'intégration
	 sur l'ensemble de la chaine d'invocation, les transactions ne sont pas annulées en fin de méthode de test.
	 Ici les méthodes de tests ne sont pas exécutées dans le contexte d'une transaction.
	 Les données en bases  sont supprimés lors de la suppresion du container une fois tous les tests executés.
	 */

	@Test
	void whenPostRequestThenBookCreated(){
		var expectedBook  = Book.of("1231231231", "Title", "Author", 9.90 );
		webTestClient.post().uri("/books")
				.bodyValue(expectedBook)
				.exchange() //envoi de la requête
				.expectStatus().isCreated()
				.expectBody(Book.class).value(actualBook -> {
					assertThat(actualBook).isNotNull();
					assertThat(actualBook.isbn()).isEqualTo(expectedBook.isbn());
				} );

	}

	//test de la récup d'un livre via son Isbn
	@Test
	void whenGetRequestThenBookReturned(){
		var bookIsbn = "1231231232";
		var bookToCreate = Book.of(bookIsbn, "Title", "Author", 9.90);
		var expecteBook = webTestClient
				.post()
				.uri("/books")
				.bodyValue(bookToCreate)
				.exchange()
				.expectStatus().isCreated()
				.expectBody(Book.class).value(actualBook -> assertThat(actualBook).isNotNull())
				.returnResult().getResponseBody();

		webTestClient
				.get()
				.uri("/books/" + bookIsbn)
				.exchange()
				.expectStatus().is2xxSuccessful()
				.expectBody(Book.class).value(actualBook -> {
					assertThat(actualBook).isNotNull();
					assertThat(actualBook.isbn()).isEqualTo(bookIsbn);
				});
	}

}
