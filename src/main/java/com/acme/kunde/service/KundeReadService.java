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
package com.acme.kunde.service;

import com.acme.kunde.entity.Kunde;
import com.acme.kunde.repository.KundeRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Anwendungslogik für Kunden.
 * <img src="../../../../../asciidoc/KundeReadService.svg" alt="Klassendiagramm">
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public final class KundeReadService {
    private final KundeRepository repo;

    /**
     * Einen Kunden anhand seiner ID suchen.
     *
     * @param id Die Id des gesuchten Kunden
     * @return Der gefundene Kunde
     * @throws NotFoundException Falls kein Kunde gefunden wurde
     */
    public @NonNull Kunde findById(final UUID id) {
        log.debug("findById: id={}", id);
        final var kunde = repo.findById(id)
            .orElseThrow(() -> new NotFoundException(id));
        log.debug("findById: {}", kunde);
        return kunde;
    }

    /**
     * Kunden anhand von Suchkriterien als Collection suchen.
     *
     * @param suchkriterien Die Suchkriterien
     * @return Die gefundenen Kunden oder eine leere Liste
     * @throws NotFoundException Falls keine Kunden gefunden wurden
     */
    @SuppressWarnings({"ReturnCount", "NestedIfDepth"})
    public @NonNull Collection<Kunde> find(final Map<String, String> suchkriterien) {
        log.debug("find: suchkriterien={}", suchkriterien);

        if (suchkriterien.isEmpty()) {
            return repo.findAll();
        }

        if (suchkriterien.size() == 1) {
            final var nachname = suchkriterien.get("nachname");
            if (nachname != null) {
                final var kunden = repo.findByNachname(nachname);
                if (kunden.isEmpty()) {
                    throw new NotFoundException(suchkriterien);
                }
                log.debug("find (nachname): {}", kunden);
                return kunden;
            }

            final var email = suchkriterien.get("email");
            if (email != null) {
                final var kunde = repo.findByEmail(email);
                if (kunde.isEmpty()) {
                    throw new NotFoundException(suchkriterien);
                }
                final var kunden = List.of(kunde.get());
                log.debug("find (email): {}", kunden);
                return kunden;
            }
        }

        final var kunden = repo.find(suchkriterien);
        if (kunden.isEmpty()) {
            throw new NotFoundException(suchkriterien);
        }
        log.debug("find: {}", kunden);
        return kunden;
    }

    /**
     * Abfrage, welche Nachnamen es zu einem Präfix gibt.
     *
     * @param prefix Nachname-Präfix.
     * @return Die passenden Nachnamen.
     * @throws NotFoundException Falls keine Nachnamen gefunden wurden.
     */
    public Collection<String> findNachnamenByPrefix(final String prefix) {
        final var nachnamen = repo.findNachnamenByPrefix(prefix);
        if (nachnamen.isEmpty()) {
            throw new NotFoundException();
        }
        return nachnamen;
    }
}
