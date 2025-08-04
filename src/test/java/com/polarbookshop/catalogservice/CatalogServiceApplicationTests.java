package com.polarbookshop.catalogservice;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.polarbookshop.catalogservice.domain.Book;
import static org.assertj.core.api.Assertions.assertThat;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

//TI utilisant un container de servlet écoutant sur un port aléatoire.
//spécifie le chargement d'un container de servlet associé avec un contexte d'application configuré via la classe amorce @SpringBootApplication
@SpringBootTest (
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("integration") //profil contenant l'URI de la base containerisé PostgreSQL
@Testcontainers //gestion auto du cycle de vie du container
@AutoConfigureWebTestClient(timeout = "36000") // pour éviter les timeout aléatoires durant les tests
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

	//définition du container Keycloak
	@Container
	private static final KeycloakContainer keycloakContainer = new KeycloakContainer("quay.io/keycloak/keycloak:26.1.3")
			.withRealmImportFile("test-realm-config.json");

	//redéfiniton de l'URI de test de Keycloak à laquelle accède catalog-service
	@DynamicPropertySource
	static void dynamicProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
				() -> keycloakContainer.getAuthServerUrl()+"/realms/PolarBookshop");
	}

	//définition d'un record contenant l'access token
	// annoté pour indiquer àJackson comment obtenir /désérialiser l'access token depuis la réponse JSON
	private record KeycloakToken(String accessToken) {
		@JsonCreator
		private KeycloakToken(@JsonProperty("access_token") final String accessToken) {
			this.accessToken = accessToken;
		}
	}

	//méthode privée pour obtenir depuis le client web les tokens de tests via password grant flow
	private static KeycloakToken authenticateWith(String username, String password, WebClient webClient) {
		return webClient
				.post()
				.body(BodyInserters.fromFormData("grant_type", "password")
						.with("client_id","polar-test")
						.with("username", username)
						.with("password", password)
				)
				.retrieve()
				.bodyToMono(KeycloakToken.class)
				.block();//on bloque le client reactif jusqu'à obtention du token -> mode impératif
	}

	private static KeycloakToken bjornTokens;
	private static KeycloakToken isabelleTokens;

	//génération d'access token avant l'exécution des tests
	@BeforeAll
	static void generateAccessTokens() {
		//construction d'une requête vers le EP Keycloak d'obtention d'un access token un client web (pas de test)
		WebClient webClient =WebClient.builder()
				.baseUrl(keycloakContainer.getAuthServerUrl()+"/realms/PolarBookshop/protocol/openid-connect/token")//adresse d'obtention d'un token
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.build();

		// envoi des requêtes pour générer les access tokens
		isabelleTokens = authenticateWith("isabelle", "password", webClient);
		bjornTokens = authenticateWith("bjorn", "password", webClient);
	}

	@Test
	void whenPostRequestThenBookCreated(){
		var expectedBook  = Book.of("1231231231", "Title", "Author", 9.90, "Polarsophia" );
		webTestClient.post().uri("/books")
				//passage de l'access token d'isabelle autorisée à créer un livre
				.headers(headers -> headers.setBearerAuth(isabelleTokens.accessToken()))
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
		var bookToCreate = Book.of(bookIsbn, "Title", "Author", 9.90, "Polarsophia");
		var expectedBook = webTestClient
				.post()
				.uri("/books")
				//passage de l'access token d'isabelle autorisée à créer un livre
				.headers(headers -> headers.setBearerAuth(isabelleTokens.accessToken()))
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

	@Test
	void whenPutRequestThenBookUpdated(){
		var bookIsbn = "1231231233";
		var bookToCreate = Book.of(bookIsbn, "Title", "Author", 9.90, "Polarsophia");
		var createdBook = webTestClient
				.post()
				.uri("/books")
				//passage de l'access token d'isabelle autorisée à créer un livre
				.headers(headers -> headers.setBearerAuth(isabelleTokens.accessToken()))
				.bodyValue(bookToCreate)
				.exchange()
				.expectStatus().isCreated()
				.expectBody(Book.class).value(actualBook -> assertThat(actualBook).isNotNull())
				.returnResult().getResponseBody();
		//chgmt du prix
		var bookToUpdate = new Book(createdBook.id(),
				createdBook.isbn(),createdBook.title(),createdBook.author(), 7.95,
				createdBook.publisher(), createdBook.createdDate(),createdBook.lastModifiedDate(), createdBook.createdBy(), createdBook.lastModifiedBy(), createdBook.version());

		webTestClient
				.put()
				.uri("/books/" + bookIsbn)
				//passage de l'access token d'isabelle autorisée à créer un livre
				.headers(headers -> headers.setBearerAuth(isabelleTokens.accessToken()))
				.bodyValue(bookToUpdate)
				.exchange()
				.expectStatus().isOk()
				.expectBody(Book.class).value(actualBook -> {
					assertThat(actualBook).isNotNull();
					assertThat(actualBook.price()).isEqualTo(bookToUpdate.price());
				});

	}

	@Test
	void whenDeleteRequestThenBookDeleted(){
		var bookIsbn = "1231231234";
		var bookToCreate = Book.of(bookIsbn, "Title", "Author", 9.90, "Polarsophia");
		webTestClient
				.post()
				.uri("/books")
				//passage de l'access token d'isabelle autorisée à créer un livre
				.headers(headers -> headers.setBearerAuth(isabelleTokens.accessToken()))
				.bodyValue(bookToCreate)
				.exchange()
				.expectStatus().isCreated();

		webTestClient
				.delete()
				.uri("/books/" + bookIsbn)
				//passage de l'access token d'isabelle autorisée à créer un livre
				.headers(headers -> headers.setBearerAuth(isabelleTokens.accessToken()))
				.exchange()
				.expectStatus().isNoContent();

		webTestClient
				.get()
				.uri("/books/" + bookIsbn)
				.exchange()
				.expectStatus().isNotFound()
				.expectBody(String.class).value(errorMessage->assertThat(errorMessage)
						.isEqualTo("The book with isbn " + bookIsbn + " was not found."));
	}

	//test de retour 403 (forbidden) si l'utilisateur n'a pas les droits d'accès (authentifié mais non autorisé)
	@Test
	void whenPostRequestUnauthorizedThen403(){
		var bookIsbn = "1231231235";
		var bookToCreate = Book.of(bookIsbn, "Title", "Author", 9.90, "Polarsophia");
		webTestClient
				.post()
				.uri("/books")
				//passage de l'access token de Bjorn non autorisé à créer un livre
				.headers(headers -> headers.setBearerAuth(bjornTokens.accessToken()))
				.bodyValue(bookToCreate)
				.exchange()
				.expectStatus().isForbidden();
	}

	//test d'un retour 401 (unauthorized) si un POST est effectué par un utilisateur non authentifié
	@Test
	void whenPostRequestUnauthenticatedThen401(){
		var bookIsbn = "1231231236";
		var bookToCreate = Book.of(bookIsbn, "Title", "Author", 9.90, "Polarsophia");
		webTestClient
				.post()
				.uri("/books")
				.bodyValue(bookToCreate)
				.exchange()
				.expectStatus().isUnauthorized();
	}
}
