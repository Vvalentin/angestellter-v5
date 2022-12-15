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
package com.acme.angestellter.service;

import com.acme.angestellter.entity.Angestellter;
import com.acme.angestellter.repository.AngestellterRepository;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Objects;
import java.util.UUID;

/**
 * Anwendungslogik für Angestellten auch mit Bean Validation.
 * <img src="../../../../../asciidoc/AngestellterWriteService.svg" alt="Klassendiagramm">
 */
@Service
@RequiredArgsConstructor
@Slf4j
public final class AngestellterWriteService {
    private final AngestellterRepository repo;

    private final Validator validator;

    /**
     * Einen neuen Angestellten anlegen.
     *
     * @param angestellter Das Objekt des neu anzulegenden Angestellten.
     * @return Der neu angelegte Angestellte mit generierter ID
     * @throws ConstraintViolationsException Falls mindestens ein Constraint verletzt ist.
     * @throws EmailExistsException Es gibt bereits einen Angestellten mit der Emailadresse.
     */
    public Angestellter create(@Valid final Angestellter angestellter) {
        log.debug("create: {}", angestellter);

        final var violations = validator.validate(angestellter);
        if (!violations.isEmpty()) {
            log.debug("create: violations={}", violations);
            throw new ConstraintViolationsException(violations);
        }

        if (repo.isEmailExisting(angestellter.getEmail())) {
            throw new EmailExistsException(angestellter.getEmail());
        }

        final var angestellterDB = repo.create(angestellter);
        log.debug("create: {}", angestellterDB);
        return angestellterDB;
    }

    /**
     * Einen vorhandenen Angestellten aktualisieren.
     *
     * @param angestellter Das Objekt mit den neuen Daten (ohne ID)
     * @param id ID des zu aktualisierenden Angestellten
     * @throws ConstraintViolationsException Falls mindestens ein Constraint verletzt ist.
     * @throws NotFoundException Kein Angestellter zur ID vorhanden.
     * @throws EmailExistsException Es gibt bereits einen Angestellten mit der Emailadresse.
     */
    public void update(final Angestellter angestellter, final UUID id) {
        log.debug("update: {}", angestellter);
        log.debug("update: id={}", id);

        final var violations = validator.validate(angestellter);
        if (!violations.isEmpty()) {
            log.debug("update: violations={}", violations);
            throw new ConstraintViolationsException(violations);
        }

        final var angestellterDbOptional = repo.findById(id);
        if (angestellterDbOptional.isEmpty()) {
            throw new NotFoundException(id);
        }

        final var email = angestellter.getEmail();
        final var angestellterDb = angestellterDbOptional.get();
        if (!Objects.equals(email, angestellterDb.getEmail()) && repo.isEmailExisting(email)) {
            log.debug("update: email {} existiert", email);
            throw new EmailExistsException(email);
        }

        angestellter.setId(id);
        repo.update(angestellter);
    }

    /**
     * Einen vorhandenen Angestellten löschen.
     *
     * @param id Die ID des zu löschenden Angestellten.
     */
    public void deleteById(final UUID id) {
        log.debug("deleteById: id={}", id);
        repo.deleteById(id);
    }
}
