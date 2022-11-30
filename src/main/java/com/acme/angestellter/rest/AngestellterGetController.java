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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.acme.angestellter.rest;

import com.acme.angestellter.service.AngestellterReadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import static com.acme.angestellter.rest.AngestellterGetController.REST_PATH;
import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Eine @RestController-Klasse bildet die REST-Schnittstelle, wobei die HTTP-Methoden, Pfade und MIME-Typen auf die
 * Methoden der Klasse abgebildet werden.
 * <img src="../../../../../asciidoc/AngestellterGetController.svg" alt="Klassendiagramm">
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
@RestController
@RequestMapping(REST_PATH)
@Tag(name = "Angestellter API")
@RequiredArgsConstructor
@Slf4j
class AngestellterGetController {
    /**
     * Basispfad für die REST-Schnittstelle.
     */
    static final String REST_PATH = "/rest";

    /**
     * Muster für eine UUID. `$HEX_PATTERN{8}-($HEX_PATTERN{4}-){3}$HEX_PATTERN{12}` enthält eine _capturing group_
     * und ist nicht zulässig.
     */
    static final String ID_PATTERN =
        "[\\dA-Fa-f]{8}-[\\dA-Fa-f]{4}-[\\dA-Fa-f]{4}-[\\dA-Fa-f]{4}-[\\dA-Fa-f]{12}";

    /**
     * Pfad, um Nachnamen abzufragen.
     */
    @SuppressWarnings("TrailingComment")
    private static final String NACHNAME_PATH = "/nachname"; //NOSONAR

    private final AngestellterReadService service;
    private final UriHelper uriHelper;

    // https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html#webflux-ann-methods
    // https://localhost:8080/swagger-ui.html
    /**
     * Suche anhand der Angestellter-ID als Pfad-Parameter.
     *
     * @param id ID des zu suchenden Angestellten
     * @param request Das Request-Objekt, um Links für HATEOAS zu erstellen.
     * @return Gefundener Angestellter mit Atom-Links.
     */
    @GetMapping(path = "{id:" + ID_PATTERN + "}", produces = HAL_JSON_VALUE)
    @Operation(summary = "Suche mit der Angestellter-ID", tags = "Suchen")
    @ApiResponse(responseCode = "200", description = "Angestellter gefunden")
    @ApiResponse(responseCode = "404", description = "Angestellter nicht gefunden")
    AngestellterModel findById(@PathVariable final UUID id, final HttpServletRequest request) {
        log.debug("findById: id={}", id);

        // Geschaeftslogik bzw. Anwendungskern
        final var angestellter = service.findById(id);

        // HATEOAS
        final var model = new AngestellterModel(angestellter);
        // evtl. Forwarding von einem API-Gateway
        final var baseUri = uriHelper.getBaseUri(request).toString();
        final var idUri = baseUri + '/' + angestellter.getId();
        final var selfLink = Link.of(idUri);
        final var listLink = Link.of(baseUri, LinkRelation.of("list"));
        final var addLink = Link.of(baseUri, LinkRelation.of("add"));
        final var updateLink = Link.of(idUri, LinkRelation.of("update"));
        final var removeLink = Link.of(idUri, LinkRelation.of("remove"));
        model.add(selfLink, listLink, addLink, updateLink, removeLink);

        log.debug("findById: {}", model);
        return model;
    }

    /**
     * Suche mit diversen Suchkriterien als Query-Parameter.
     *
     * @param suchkriterien Query-Parameter als Map.
     * @param request Das Request-Objekt, um Links für HATEOAS zu erstellen.
     * @return Gefundenen Angestellten als CollectionModel.
     */
    @GetMapping(produces = HAL_JSON_VALUE)
    @Operation(summary = "Suche mit Suchkriterien", tags = "Suchen")
    @ApiResponse(responseCode = "200", description = "CollectionModel mid den Angestellten")
    @ApiResponse(responseCode = "404", description = "Keine Angestellten gefunden")
    CollectionModel<? extends AngestellterModel> find(
        @RequestParam final Map<String, String> suchkriterien,
        final HttpServletRequest request
    ) {
        log.debug("find: suchkriterien={}", suchkriterien);

        final var baseUri = uriHelper.getBaseUri(request).toString();

        // Geschaeftslogik bzw. Anwendungskern
        final var models = service.find(suchkriterien)
            .stream()
            .map(angestellter -> {
                final var model = new AngestellterModel(angestellter);
                model.add(Link.of(baseUri + '/' + angestellter.getId()));
                return model;
            })
            .toList();

        log.debug("find: {}", models);
        return CollectionModel.of(models);
    }

    /**
     * Abfrage, welche Nachnamen es zu einem Präfix gibt.
     *
     * @param prefix Nachname-Präfix als Pfadvariable.
     * @return Die passenden Nachnamen oder Statuscode 404, falls es keine gibt.
     */
    @GetMapping(path = NACHNAME_PATH + "/{prefix}", produces = APPLICATION_JSON_VALUE)
    String findNachnamenByPrefix(@PathVariable final String prefix) {
        log.debug("findNachnamenByPrefix: {}", prefix);
        final var nachnamen = service.findNachnamenByPrefix(prefix);
        log.debug("findNachnamenByPrefix: {}", nachnamen);
        return nachnamen.toString();
    }
}
