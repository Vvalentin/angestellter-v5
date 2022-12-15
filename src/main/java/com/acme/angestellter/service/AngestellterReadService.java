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
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Anwendungslogik für Angestellten.
 *  * <img src="../../../../../asciidoc/AngestellterReadService.svg" alt="Klassendiagramm">
 *  * Schreiboperationen werden mit Transaktionen durchgeführt und Lese-Operationen mit Readonly-Transaktionen:
 *  * <a href="https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#transactions">siehe Dokumentation</a>.
 *
 */
@Service
@RequiredArgsConstructor
@Slf4j
public final class AngestellterReadService {
    private final AngestellterRepository repo;

    /**
     * Einen Angestellten anhand seiner ID suchen.
     *
     * @param id Die Id des gesuchten Angestellten
     * @return Den gefundenen Angestellten
     * @throws NotFoundException Falls kein Angestellter gefunden wurde
     */
    public @NonNull Angestellter findById(final UUID id) {
        log.debug("findById: id={}", id);
        final var angestellter = repo.findById(id)
            .orElseThrow(() -> new NotFoundException(id));
        log.debug("findById: {}", angestellter);
        return angestellter;
    }

    /**
     * Angestellten anhand von Suchkriterien als Collection suchen.
     *
     * @param suchkriterien Die Suchkriterien
     * @return Die gefundenen Angestellten oder eine leere Liste
     * @throws NotFoundException Falls keine Angestellten gefunden wurden
     */
    @SuppressWarnings({"ReturnCount", "NestedIfDepth"})
    public @NonNull Collection<Angestellter> find(final Map<String, String> suchkriterien) {
        log.debug("find: suchkriterien={}", suchkriterien);

        if (suchkriterien.isEmpty()) {
            return repo.findAll();
        }

        if (suchkriterien.size() == 1) {
            final var nachname = suchkriterien.get("nachname");
            if (nachname != null) {
                final var angestellte = repo.findByNachname(nachname);
                if (angestellte.isEmpty()) {
                    throw new NotFoundException(suchkriterien);
                }
                log.debug("find (nachname): {}", angestellte);
                return angestellte;
            }

            final var email = suchkriterien.get("email");
            if (email != null) {
                final var angestellter = repo.findByEmail(email);
                if (angestellter.isEmpty()) {
                    throw new NotFoundException(suchkriterien);
                }
                final var angestellte = List.of(angestellter.get());
                log.debug("find (email): {}", angestellte);
                return angestellte;
            }
        }

        final var angestellte = repo.find(suchkriterien);
        if (angestellte.isEmpty()) {
            throw new NotFoundException(suchkriterien);
        }
        log.debug("find: {}", angestellte);
        return angestellte;
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
