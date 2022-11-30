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

import com.acme.angestellter.rest.patch.InvalidPatchOperationException;
import com.acme.angestellter.rest.patch.AngestellterPatcher;
import com.acme.angestellter.rest.patch.PatchOperation;
import com.acme.angestellter.service.ConstraintViolationsException;
import com.acme.angestellter.service.EmailExistsException;
import com.acme.angestellter.service.AngestellterReadService;
import com.acme.angestellter.service.AngestellterWriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import static com.acme.angestellter.rest.AngestellterGetController.ID_PATTERN;
import static com.acme.angestellter.rest.AngestellterGetController.REST_PATH;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.created;

/**
 * Eine `@RestController`-Klasse bildet die REST-Schnittstelle, wobei die HTTP-Methoden, Pfade und MIME-Typen auf die
 * Methoden der Klasse abgebildet werden.
 * <img src="../../../../../asciidoc/AngestellterWriteController.svg" alt="Klassendiagramm">
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
@RestController
@RequestMapping(REST_PATH)
@Tag(name = "Angestellter API")
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("ClassFanOutComplexity")
class AngestellterWriteController {
    @SuppressWarnings("TrailingComment")
    private static final String PROBLEM_PATH = "/problem/"; //NOSONAR
    private final AngestellterWriteService service;
    private final AngestellterReadService readService;
    private final AngestellterPatcher patcher;

    private final UriHelper uriHelper;

    /**
     * Einen neuen Angestellter-Datensatz anlegen.
     *
     * @param angestellterDTO Das Angestellteobjekt aus dem eingegangenen Request-Body.
     * @param request Das Request-Objekt, um `Location` im Response-Header zu erstellen.
     * @return Response mit Statuscode 201 einschließlich Location-Header oder Statuscode 422 falls Constraints verletzt
     *      sind oder die Emailadresse bereits existiert oder Statuscode 400 falls syntaktische Fehler im Request-Body
     *      vorliegen.
     * @throws URISyntaxException falls die URI im Request-Objekt nicht korrekt wäre
     */
    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Einen neuen Angestellten anlegen", tags = "Neuanlegen")
    @ApiResponse(responseCode = "201", description = "Angestellter neu angelegt")
    @ApiResponse(responseCode = "400", description = "Syntaktische Fehler im Request-Body")
    @ApiResponse(responseCode = "422", description = "Ungültige Werte oder Email vorhanden")
    @SuppressWarnings("TrailingComment")
    ResponseEntity<Void> create(
        @RequestBody final AngestellterDTO angestellterDTO,
        final HttpServletRequest request
    ) throws URISyntaxException {
        log.debug("create: {}", angestellterDTO);

        final var angestellter = service.create(angestellterDTO.toAngestellter());
        final var baseUri = uriHelper.getBaseUri(request).toString();
        final var location = new URI(baseUri + '/' + angestellter.getId()); //NOSONAR
        return created(location).build();
    }

    /**
     * Einen vorhandenen Angestellter-Datensatz überschreiben.
     *
     * @param id ID des zu aktualisierenden Angestellten.
     * @param angestellterDTO Das Angestellteobjekt aus dem eingegangenen Request-Body.
     */
    @PutMapping(path = "{id:" + ID_PATTERN + "}", consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(NO_CONTENT)
    @Operation(summary = "Einen Angestellten mit neuen Werten aktualisieren", tags = "Aktualisieren")
    @ApiResponse(responseCode = "204", description = "Aktualisiert")
    @ApiResponse(responseCode = "400", description = "Syntaktische Fehler im Request-Body")
    @ApiResponse(responseCode = "404", description = "Angestellter nicht vorhanden")
    @ApiResponse(responseCode = "422", description = "Ungültige Werte oder Email vorhanden")
    void update(@PathVariable final UUID id, @RequestBody final AngestellterDTO angestellterDTO
    ) {
        log.debug("update: id={}, {}", id, angestellterDTO);
        service.update(angestellterDTO.toAngestellter(), id);
    }

    /**
     * Einen vorhandenen Angestellter-Datensatz durch PATCH aktualisieren.
     *
     * @param id ID des zu aktualisierenden Angestellten.
     * @param operations Die Collection der Patch-Operationen.
     */
    @PatchMapping(path = "{id:" + ID_PATTERN + "}", consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(NO_CONTENT)
    @Operation(summary = "Einen Angestellten mit einzelnen neuen Werten aktualisieren", tags = "Aktualisieren")
    @ApiResponse(responseCode = "204", description = "Aktualisiert")
    @ApiResponse(responseCode = "400", description = "Syntaktische Fehler im Request-Body")
    @ApiResponse(responseCode = "404", description = "Angestellter nicht vorhanden")
    @ApiResponse(responseCode = "422", description = "Constraints verletzt oder Email vorhanden")
    void patch(@PathVariable final UUID id, @RequestBody final Collection<PatchOperation> operations) {
        log.debug("patch: id={}, operations={}", id, operations);
        final var angestellter = readService.findById(id);
        patcher.patch(angestellter, operations);
        log.debug("patch: {}", angestellter);
        service.update(angestellter, id);
    }

    /**
     * Einen vorhandenen Angestellten anhand seiner ID löschen.
     *
     * @param id ID des zu löschenden Angestellten.
     */
    @DeleteMapping(path = "{id:" + ID_PATTERN + "}")
    @ResponseStatus(NO_CONTENT)
    @Operation(summary = "Einen Angestellten anhand der ID loeschen", tags = "Loeschen")
    @ApiResponse(responseCode = "204", description = "Gelöscht")
    void deleteById(@PathVariable final UUID id)  {
        log.debug("deleteById: id={}", id);
        service.deleteById(id);
    }

    @ExceptionHandler
    @SuppressWarnings("unused")
    ResponseEntity<ProblemDetail> onConstraintViolations(
        final ConstraintViolationsException ex,
        final HttpServletRequest request
    ) {
        log.debug("onConstraintViolations: {}", ex.getMessage());

        final var angestellterViolations = ex.getViolations()
            .stream()
            .map(violation -> violation.getPropertyPath() + ": " +
                violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName() + " " +
                violation.getMessage())
            .toList();
        log.trace("onConstraintViolations: {}", angestellterViolations);
        final String detail;
        if (angestellterViolations.isEmpty()) {
            detail = "N/A";
        } else {
            // [ und ] aus dem String der Liste entfernen
            final var violationsStr = angestellterViolations.toString();
            detail = violationsStr.substring(1, violationsStr.length() - 2);
        }

        final var problemDetail = ProblemDetail.forStatusAndDetail(UNPROCESSABLE_ENTITY, detail);
        problemDetail.setType(URI.create(PROBLEM_PATH + ProblemType.CONSTRAINTS.getValue()));
        final var uri = uriHelper.getBaseUri(request);
        problemDetail.setInstance(uri);

        return ResponseEntity.of(problemDetail).build();
    }

    @ExceptionHandler
    @SuppressWarnings("unused")
    ResponseEntity<ProblemDetail> onEmailExists(final EmailExistsException ex, final HttpServletRequest request) {
        log.debug("onEmailExists: {}", ex.getMessage());
        final var problemDetail = ProblemDetail.forStatusAndDetail(UNPROCESSABLE_ENTITY, ex.getMessage());
        problemDetail.setType(URI.create(PROBLEM_PATH + ProblemType.CONSTRAINTS.getValue()));
        final var uri = uriHelper.getBaseUri(request);
        problemDetail.setInstance(uri);
        return ResponseEntity.of(problemDetail).build();
    }

    @ExceptionHandler
    @SuppressWarnings("unused")
    ResponseEntity<ProblemDetail> onMessageNotReadable(
        final HttpMessageNotReadableException ex,
        final HttpServletRequest request
    ) {
        log.debug("onMessageNotReadable: {}", ex.getMessage());
        final var problemDetail = ProblemDetail.forStatusAndDetail(BAD_REQUEST, ex.getMessage());
        problemDetail.setType(URI.create(PROBLEM_PATH + ProblemType.BAD_REQUEST.getValue()));
        final var uri = uriHelper.getBaseUri(request);
        problemDetail.setInstance(uri);
        return ResponseEntity.of(problemDetail).build();
    }

    @ExceptionHandler
    @SuppressWarnings("unused")
    ResponseEntity<ProblemDetail> onInvalidPatchOperation(
        final InvalidPatchOperationException ex,
        final HttpServletRequest request
    ) {
        log.debug("onInvalidPatchOperation: {}", ex.getMessage());
        final var problemDetail = ProblemDetail.forStatusAndDetail(UNPROCESSABLE_ENTITY, ex.getMessage());
        problemDetail.setType(URI.create(PROBLEM_PATH + ProblemType.UNPROCESSABLE.getValue()));
        final var uri = uriHelper.getBaseUri(request);
        problemDetail.setInstance(uri);
        return ResponseEntity.of(problemDetail).build();
    }
}
