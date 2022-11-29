/*
 * Copyright (C) 2022 - present Juergen Zimmermann, Hochschule Karlsruhe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.acme.kunde.rest;

import com.acme.kunde.entity.InteresseType;
import com.acme.kunde.rest.patch.PatchOperation;
import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import static com.acme.kunde.dev.DevConfig.DEV;
import static com.acme.kunde.entity.GeschlechtType.WEIBLICH;
import static com.acme.kunde.entity.InteresseType.LESEN;
import static com.acme.kunde.entity.InteresseType.REISEN;
import static com.acme.kunde.entity.InteresseType.SPORT;
import static com.acme.kunde.rest.KundeGetController.ID_PATTERN;
import static com.acme.kunde.rest.KundeGetController.REST_PATH;
import static com.acme.kunde.rest.KundeGetRestTest.HOST;
import static com.acme.kunde.rest.KundeGetRestTest.PASSWORD;
import static com.acme.kunde.rest.KundeGetRestTest.SCHEMA;
import static com.acme.kunde.rest.KundeGetRestTest.USER_ADMIN;
import static com.acme.kunde.rest.patch.PatchOperationType.ADD;
import static com.acme.kunde.rest.patch.PatchOperationType.REMOVE;
import static com.acme.kunde.rest.patch.PatchOperationType.REPLACE;
import static java.math.BigDecimal.ONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.condition.JRE.JAVA_18;
import static org.junit.jupiter.api.condition.JRE.JAVA_19;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

@Tag("integration")
@Tag("rest")
@Tag("rest_write")
@DisplayName("REST-Schnittstelle fuer Schreiben testen")
@ExtendWith(SoftAssertionsExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles(DEV)
@EnabledForJreRange(min = JAVA_18, max = JAVA_19)
@SuppressWarnings("WriteTag")
class KundeWriteRestTest {
    private static final String ID_UPDATE_PUT = "00000000-0000-0000-0000-000000000030";
    private static final String ID_UPDATE_PATCH = "00000000-0000-0000-0000-000000000040";
    private static final String ID_DELETE = "00000000-0000-0000-0000-000000000050";

    private static final String NEUER_NACHNAME = "Neuernachname-Rest";
    private static final String NEUE_EMAIL = "email.rest@test.de";
    private static final String NEUE_EMAIL_PATCH = "email.rest@test.de.patch";
    private static final String NEUES_GEBURTSDATUM = "2022-01-31";
    private static final String CURRENCY_CODE = "EUR";
    private static final String NEUE_HOMEPAGE = "https://test.de";

    private static final InteresseType NEUES_INTERESSE = SPORT;
    private static final String NEUE_PLZ = "12345";
    private static final String NEUER_ORT = "Neuerortrest";

    private static final String NEUER_NACHNAME_INVALID = "?!$";
    private static final String NEUE_EMAIL_INVALID = "email@";
    private static final int NEUE_KATEGORIE_INVALID = 11;
    private static final String NEUES_GEBURTSDATUM_INVALID = "3000-01-31";
    private static final String NEUE_PLZ_INVALID = "1234";

    private static final InteresseType ZU_LOESCHENDES_INTERESSE = LESEN;

    private static final String ID_PATH = "/{id}";

    private final WebClient client;
    private final KundeRepository kundeRepo;

    @InjectSoftAssertions
    private SoftAssertions softly;

    KundeWriteRestTest(@LocalServerPort final int port, final ApplicationContext ctx) {
        final var writeController = ctx.getBean(KundeWriteController.class);
        assertThat(writeController).isNotNull();

        final var uriComponents = UriComponentsBuilder.newInstance()
            .scheme(SCHEMA)
            .host(HOST)
            .port(port)
            .path(REST_PATH)
            .build();
        final var baseUrl = uriComponents.toUriString();
        client = WebClient
            .builder()
            .filter(basicAuthentication(USER_ADMIN, PASSWORD))
            .baseUrl(baseUrl)
            .build();
        final var clientAdapter = WebClientAdapter.forClient(client);
        final var proxyFactory = HttpServiceProxyFactory
            .builder(clientAdapter)
            .build();
        kundeRepo = proxyFactory.createClient(KundeRepository.class);
    }

    @SuppressWarnings("DataFlowIssue")
    @Nested
    @DisplayName("Erzeugen")
    class Erzeugen {
        @ParameterizedTest(name = "[{index}] Neuanlegen eines neuen Kunden: nachname={0}, email={1}")
        @CsvSource(
            NEUER_NACHNAME + "," + NEUE_EMAIL + "," + NEUES_GEBURTSDATUM + "," + NEUE_HOMEPAGE + "," + CURRENCY_CODE +
                "," + NEUE_PLZ + "," + NEUER_ORT
        )
        @DisplayName("Neuanlegen eines neuen Kunden")
        void create(final ArgumentsAccessor args) {
            // given
            final var umsatz = new UmsatzDTO(ONE, Currency.getInstance(args.getString(4)));
            final var adresse = new AdresseDTO(args.getString(5), args.getString(6));
            final var kundeDTO = new KundeDTO(
                args.getString(0),
                args.getString(1),
                1,
                true,
                args.get(2, LocalDate.class),
                args.get(3, URL.class),
                WEIBLICH,
                null,
                List.of(LESEN, REISEN),
                umsatz,
                adresse
            );

            // when
            final var response = client
                .post()
                .contentType(APPLICATION_JSON)
                .bodyValue(kundeDTO)
                .exchangeToMono(Mono::just)
                .block();


            // then
            assertThat(response).isNotNull();
            softly.assertThat(response.statusCode()).isEqualTo(CREATED);
            final var location = response.headers().asHttpHeaders().getLocation();
            softly.assertThat(location)
                .isNotNull()
                .isInstanceOf(URI.class);
            softly.assertThat(location.toString()).matches(".*/" + ID_PATTERN + "$");
        }

        @ParameterizedTest(name = "[{index}] Neuanlegen mit ungueltigen Werten: nachname={0}, email={1}")
        @CsvSource(
            NEUER_NACHNAME_INVALID + "," + NEUE_EMAIL_INVALID + "," + NEUE_KATEGORIE_INVALID + "," +
                NEUES_GEBURTSDATUM_INVALID + "," + NEUE_PLZ_INVALID + "," + NEUER_ORT
        )
        @DisplayName("Neuanlegen mit ungueltigen Werten")
        @SuppressWarnings("DynamicRegexReplaceableByCompiledPattern")
        void createInvalid(final ArgumentsAccessor args) {
            // given
            final var adresse = new AdresseDTO(args.getString(4), args.getString(5));
            final var kundeDTO = new KundeDTO(
                args.getString(0),
                args.getString(1),
                args.getInteger(2),
                true,
                args.get(3, LocalDate.class),
                null,
                WEIBLICH,
                null,
                List.of(LESEN, REISEN, REISEN),
                null,
                adresse
            );
            final var violationKeys = List.of(
                "nachname",
                "email",
                "kategorie",
                "geburtsdatum",
                "adresse.plz",
                "interessen"
            );

            // when
            final var body = client
                .post()
                .contentType(APPLICATION_JSON)
                .bodyValue(kundeDTO)
                .exchangeToMono(response -> {
                    assertThat(response.statusCode()).isEqualTo(UNPROCESSABLE_ENTITY);
                    return response.bodyToMono(ProblemDetail.class);
                })
                .block();

            // then
            assertThat(body).isNotNull();
            final var detail = body.getDetail();
            assertThat(detail).isNotNull();
            final var violations = Arrays.asList(detail.split(", ", -1));
            assertThat(violations).hasSameSizeAs(violationKeys);

            final var actualViolationKeys = violations
                .stream()
                // Keys vor ":" extrahieren
                .map(violation -> violation.split(": ", -1)[0])
                .toList();
            assertThat(actualViolationKeys)
                .hasSameSizeAs(violationKeys)
                .hasSameElementsAs(violationKeys);
        }
    }

    @Nested
    @DisplayName("Aendern")
    class Aendern {
        @ParameterizedTest(name = "[{index}] Aendern eines vorhandenen Kunden durch PUT: id={0}")
        @ValueSource(strings = ID_UPDATE_PUT)
        @DisplayName("Aendern eines vorhandenen Kunden durch PUT")
        void put(final String id) {
            // given
            final var kundeOrig = kundeRepo.getKunde(id).block();
            assertThat(kundeOrig).isNotNull();
            final var umsatzOrig = kundeOrig.umsatz();
            final UmsatzDTO umsatz;
            if (umsatzOrig == null) {
                umsatz = null;
            } else {
                umsatz = new UmsatzDTO(ONE, umsatzOrig.waehrung());
            }
            final var adresseOrig = kundeOrig.adresse();
            final var adresse = new AdresseDTO(adresseOrig.plz(), adresseOrig.ort());

            final var kunde = new KundeDTO(
                kundeOrig.nachname(),
                kundeOrig.email() + "put",
                kundeOrig.kategorie(),
                kundeOrig.hasNewsletter(),
                kundeOrig.geburtsdatum(),
                kundeOrig.homepage(),
                kundeOrig.geschlecht(),
                kundeOrig.familienstand(),
                kundeOrig.interessen(),
                umsatz,
                adresse
            );

            // when
            final var statusCode = client
                .put()
                .uri(ID_PATH, id)
                .contentType(APPLICATION_JSON)
                .bodyValue(kunde)
                .retrieve()
                .toBodilessEntity()
                .map(ResponseEntity::getStatusCode)
                .block();

            // then
            assertThat(statusCode).isEqualTo(NO_CONTENT);
        }

        @ParameterizedTest(name = "[{index}] Aendern eines vorhandenen Kunden durch PATCH: id={0}")
        @CsvSource(ID_UPDATE_PATCH + "," + NEUE_EMAIL_PATCH)
        @DisplayName("Aendern eines vorhandenen Kunden durch PATCH")
        void patch(final String id, final String email) {
            // given
            final var replaceOp = new PatchOperation(REPLACE, "/email", email);
            final var addOp = new PatchOperation(ADD, "/interessen", NEUES_INTERESSE.toString());
            final var removeOp = new PatchOperation(REMOVE, "/interessen", ZU_LOESCHENDES_INTERESSE.toString());
            final var operations = List.of(replaceOp, addOp, removeOp);

            // when
            final var statusCode = client
                .patch()
                .uri(ID_PATH, id)
                .contentType(APPLICATION_JSON)
                .bodyValue(operations)
                .retrieve()
                .toBodilessEntity()
                .map(ResponseEntity::getStatusCode)
                .block();

            // then
            assertThat(statusCode).isEqualTo(NO_CONTENT);
        }
    }
    @Nested
    @DisplayName("Loeschen")
    class Loeschen {
        @ParameterizedTest(name = "[{index}] Loeschen eines vorhandenen Kunden: id={0}")
        @ValueSource(strings = ID_DELETE)
        @DisplayName("Loeschen eines vorhandenen Kunden")
        void deleteById(final String id) {
            // when
            final var statusCode = client
                .delete()
                .uri(ID_PATH, id)
                .exchangeToMono(response -> Mono.just(response.statusCode()))
                .block();

            // then
            assertThat(statusCode).isEqualTo(NO_CONTENT);
        }
    }
}
