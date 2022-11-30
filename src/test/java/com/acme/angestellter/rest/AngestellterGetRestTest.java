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
package com.acme.angestellter.rest;

import com.jayway.jsonpath.JsonPath;
import java.util.Map;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.hateoas.client.LinkDiscoverer;
import org.springframework.hateoas.mediatype.hal.HalLinkDiscoverer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import static com.acme.angestellter.dev.DevConfig.DEV;
import static com.acme.angestellter.entity.Angestellter.NACHNAME_PATTERN;
import static com.acme.angestellter.rest.AngestellterGetController.REST_PATH;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.condition.JRE.JAVA_18;
import static org.junit.jupiter.api.condition.JRE.JAVA_19;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.hateoas.MediaTypes.HAL_JSON;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

@Tag("integration")
@Tag("rest")
@Tag("rest_get")
@DisplayName("REST-Schnittstelle fuer GET-Requests testen")
@ExtendWith(SoftAssertionsExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles(DEV)
@EnabledForJreRange(min = JAVA_18, max = JAVA_19)
@SuppressWarnings("WriteTag")
class AngestellterGetRestTest {
    static final String SCHEMA = "http";
    static final String HOST = "localhost";
    static final String USER_ADMIN = "admin";
    static final String PASSWORD = "p";

    private static final String ID_VORHANDEN = "00000000-0000-0000-0000-000000000001";
    private static final String ID_NICHT_VORHANDEN = "ffffffff-ffff-ffff-ffff-ffffffffffff";

    private static final String NACHNAME = "Alpha";

    private static final String ID_PATH = "/{id}";
    private static final String NACHNAME_PARAM = "nachname";

    private final String baseUrl;
    private final WebClient client;
    private final AngestellterRepository angestellterRepo;

    @InjectSoftAssertions
    private SoftAssertions softly;

    AngestellterGetRestTest(@LocalServerPort final int port, final ApplicationContext ctx) {
        final var getController = ctx.getBean(AngestellterGetController.class);
        assertThat(getController).isNotNull();

        final var uriComponents = UriComponentsBuilder.newInstance()
            .scheme(SCHEMA)
            .host(HOST)
            .port(port)
            .path(REST_PATH)
            .build();
        baseUrl = uriComponents.toUriString();
        client = WebClient
            .builder()
            .filter(basicAuthentication(USER_ADMIN, PASSWORD))
            .baseUrl(baseUrl)
            .build();
        final var clientAdapter = WebClientAdapter.forClient(client);
        final var proxyFactory = HttpServiceProxyFactory
            .builder(clientAdapter)
            .build();
        angestellterRepo = proxyFactory.createClient(AngestellterRepository.class);
    }

    @Test
    @DisplayName("Immer erfolgreich")
    void immerErfolgreich() {
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("Noch nicht fertig")
    @Disabled
    void nochNichtFertig() {
        //noinspection DataFlowIssue
        assertThat(false).isTrue();
    }

    @Test
    @DisplayName("Suche nach allen Angestellten")
    @SuppressWarnings("DataFlowIssue")
    void findAll() {
        // when
        final var angestellte = angestellterRepo.getAngestellte(emptyMap()).block();

        // then
        softly.assertThat(angestellte).isNotNull();
        softly.assertThat(angestellte._embedded()).isNotNull();
        softly.assertThat(angestellte._embedded().angestellte())
            .isNotNull()
            .isNotEmpty();
    }

    @ParameterizedTest(name = "[{index}] Suche mit vorhandenem Nachnamen: nachname={0}")
    @ValueSource(strings = NACHNAME)
    @DisplayName("Suche mit vorhandenem Nachnamen")
    @SuppressWarnings("DataFlowIssue")
    void findByNachname(final String nachname) {
        // given
        final var suchkriterien = Map.of(NACHNAME_PARAM, nachname);

        // when
        final var angestellte = angestellterRepo.getAngestellte(suchkriterien).block();

        // then
        softly.assertThat(angestellte).isNotNull();
        softly.assertThat(angestellte._embedded()).isNotNull();
        final var angestellteList = angestellte._embedded().angestellte();
        softly.assertThat(angestellteList)
            .isNotNull()
            .isNotEmpty();
        angestellteList
            .stream()
            .map(AngestellterDownload::nachname)
            .forEach(nachnameTmp -> softly.assertThat(nachnameTmp).isEqualTo(nachname));
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Nested
    @DisplayName("Suche anhand der ID")
    class FindById {
        @ParameterizedTest(name = "[{index}] Suche mit vorhandener ID und JsonPath: id={0}")
        @ValueSource(strings = ID_VORHANDEN)
        @DisplayName("Suche mit vorhandener ID und JsonPath")
        void findByIdJson(final String id) {
            // given

            // when
            final var body = client
                .get()
                .uri(ID_PATH, id)
                .accept(HAL_JSON)
                .exchangeToMono(response -> response.bodyToMono(String.class))
                .block();

            // then
            assertThat(body).isNotNull().isNotBlank();

            final var nachnamePath = "$.nachname";
            final String nachname = JsonPath.read(body, nachnamePath);
            softly.assertThat(nachname).matches(NACHNAME_PATTERN);

            final var emailPath = "$.email";
            final String email = JsonPath.read(body, emailPath);
            softly.assertThat(email).contains("@");

            final LinkDiscoverer linkDiscoverer = new HalLinkDiscoverer();
            final var selfLink = linkDiscoverer.findLinkWithRel("self", body).get().getHref();
            softly.assertThat(selfLink).isEqualTo(baseUrl + '/' + id);
        }

        @ParameterizedTest(name = "[{index}] Suche mit vorhandener ID: id={0}")
        @ValueSource(strings = ID_VORHANDEN)
        @DisplayName("Suche mit vorhandener ID")
        void findById(final String id) {
            // given

            // when
            final var angestellter = angestellterRepo.getAngestellter(id).block();

            // then
            assertThat(angestellter).isNotNull();
            softly.assertThat(angestellter.nachname()).isNotNull();
            softly.assertThat(angestellter.email()).isNotNull();
            softly.assertThat(angestellter.adresse().plz()).isNotNull();
            softly.assertThat(angestellter._links().self().href()).endsWith("/" + id);
        }

        @ParameterizedTest(name = "[{index}] Suche mit syntaktisch ungueltiger oder nicht-vorhandener ID: {0}")
        @ValueSource(strings = ID_NICHT_VORHANDEN)
        @DisplayName("Suche mit syntaktisch ungueltiger oder nicht-vorhandener ID")
        void findByIdNichtVorhanden(final String id) {
            // when
            final var statusCode = client
                .get()
                .uri(ID_PATH, id)
                .exchangeToMono(response -> Mono.just(response.statusCode()))
                .block();

            // then
            assertThat(statusCode).isEqualTo(NOT_FOUND);
        }
    }
}
