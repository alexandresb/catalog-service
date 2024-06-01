package com.polarbookshop.catalogservice;

import com.polarbookshop.catalogservice.domain.Book;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

//TI utilisant un container de servlet écoutant sur un port aléatoire.
//spécifie le chargement d'un container de servlet associé avec un contexte d'application configuré via la classe amorce @SpringBootApplication
@SpringBootTest (
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class CatalogServiceApplicationTests {

	//injection basée sur les champs
	@Autowired
	private WebTestClient webTestClient;

	@Test
	void whenPostRequestThenBookCreated(){
		var expectedBook  = new Book("1231231231", "Title", "Author", 9.90 );
		webTestClient.post().uri("/books")
				.bodyValue(expectedBook)
				.exchange() //envoi de la requête
				.expectStatus().isCreated()
				.expectBody(Book.class).value(actualBook -> {
					assertThat(actualBook).isNotNull();
					assertThat(actualBook.isbn()).isEqualTo(expectedBook.isbn());
				} );

	}

}
