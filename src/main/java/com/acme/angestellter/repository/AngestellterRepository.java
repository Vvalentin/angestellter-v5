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
package com.acme.angestellter.repository;

import com.acme.angestellter.entity.Angestellter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.stream.IntStream;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import static com.acme.angestellter.repository.DB.ANGESTELLTE;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;

/**
 * Repository für den DB-Zugriff bei Angestellte.
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
@Repository
@Slf4j
@SuppressWarnings("PublicConstructor")
public class AngestellterRepository {
    /**
     * Einen Angestellten anhand seiner ID suchen.
     *
     * @param id Die Id des gesuchten Angestellten
     * @return Optional mit dem gefundenen Angestellten oder leeres Optional
     */
    public Optional<Angestellter> findById(final UUID id) {
        log.debug("findById: id={}", id);
        final var result = ANGESTELLTE.stream()
            .filter(angestellter -> Objects.equals(angestellter.getId(), id))
            .findFirst();
        log.debug("findById: {}", result);
        return result;
    }

    /**
     * Angestellten anhand von Suchkriterien ermitteln.
     * Z.B. mit GET https://localhost:8080/api?nachname=A&amp;plz=7
     *
     * @param suchkriterien Suchkriterien.
     * @return Gefundene Angestellten oder leere Collection.
     */
    @SuppressWarnings({"ReturnCount", "JavadocLinkAsPlainText"})
    public @NonNull Collection<Angestellter> find(final Map<String, String> suchkriterien) {
        log.debug("find: suchkriterien={}", suchkriterien);

        if (suchkriterien.isEmpty()) {
            return findAll();
        }

        // for-Schleife statt "forEach" wegen return
        for (final var entry : suchkriterien.entrySet()) {
            switch (entry.getKey()) {
                case "email" -> {
                    final var angestellterOpt = findByEmail(entry.getValue());
                    //noinspection OptionalIsPresent
                    return angestellterOpt.isPresent() ? List.of(angestellterOpt.get()) : emptyList();
                }
                case "nachname" -> {
                    return findByNachname(entry.getValue());
                }
                default -> {
                    log.debug("find: ungueltiges Suchkriterium={}", entry.getKey());
                    return emptyList();
                }
            }
        }

        return emptyList();
    }

    /**
     * Alle Angestellten als Collection ermitteln, wie sie später auch von der DB kommen.
     *
     * @return Alle Angestellten
     */
    public @NonNull Collection<Angestellter> findAll() {
        return ANGESTELLTE;
    }

    /**
     * Angestellter zu gegebener Emailadresse aus der DB ermitteln.
     *
     * @param email Emailadresse für die Suche
     * @return Gefundener Angestellter oder leeres Optional
     */
    public Optional<Angestellter> findByEmail(final String email) {
        log.debug("findByEmail: {}", email);
        final var result = ANGESTELLTE.stream()
            .filter(angestellter -> Objects.equals(angestellter.getEmail(), email))
            .findFirst();
        log.debug("findByEmail: {}", result);
        return result;
    }

    /**
     * Abfrage, ob es einen Angestellten mit gegebener Emailadresse gibt.
     *
     * @param email Emailadresse für die Suche
     * @return true, falls es einen solchen Angestellten gibt, sonst false
     */
    public boolean isEmailExisting(final String email) {
        log.debug("isEmailExisting: email={}", email);
        final var count = ANGESTELLTE.stream()
            .filter(angestellter -> Objects.equals(angestellter.getEmail(), email))
            .count();
        log.debug("isEmailExisting: count={}", count);
        return count > 0L;
    }

    /**
     * Angestellten anhand des Nachnamens suchen.
     *
     * @param nachname Der (Teil-) Nachname der gesuchten Angestellten
     * @return Die gefundenen Angestellten oder eine leere Collection
     */
    public @NonNull Collection<Angestellter> findByNachname(final CharSequence nachname) {
        log.debug("findByNachname: nachname={}", nachname);
        final var angestellte = ANGESTELLTE.stream()
            .filter(angestellter -> angestellter.getNachname().contains(nachname))
            .toList();
        log.debug("findByNachname: angestellte={}", angestellte);
        return angestellte;
    }

    /**
     * Abfrage, welche Nachnamen es zu einem Präfix gibt.
     *
     * @param prefix Nachname-Präfix.
     * @return Die passenden Nachnamen oder eine leere Collection.
     */
    public @NonNull Collection<String> findNachnamenByPrefix(final @NonNull String prefix) {
        log.debug("findByNachname: prefix={}", prefix);
        final var nachnamen = ANGESTELLTE.stream()
            .map(Angestellter::getNachname)
            .filter(nachname -> nachname.startsWith(prefix))
            .distinct()
            .toList();
        log.debug("findByNachname: nachnamen={}", nachnamen);
        return nachnamen;
    }

    /**
     * Einen neuen Angestellten anlegen.
     *
     * @param angestellter Das Objekt des neu anzulegenden Angestellten.
     * @return Der neu angelegte Angestellter mit generierter ID
     */
    public @NonNull Angestellter create(final @NonNull Angestellter angestellter) {
        log.debug("create: {}", angestellter);
        angestellter.setId(randomUUID());
        ANGESTELLTE.add(angestellter);
        log.debug("create: {}", angestellter);
        return angestellter;
    }

    /**
     * Einen vorhandenen Angestellten aktualisieren.
     *
     * @param angestellter Das Objekt mit den neuen Daten
     */
    public void update(final @NonNull Angestellter angestellter) {
        log.debug("update: {}", angestellter);
        final OptionalInt index = IntStream
            .range(0, ANGESTELLTE.size())
            .filter(i -> Objects.equals(ANGESTELLTE.get(i).getId(), angestellter.getId()))
            .findFirst();
        log.trace("update: index={}", index);
        if (index.isEmpty()) {
            return;
        }
        ANGESTELLTE.set(index.getAsInt(), angestellter);
        log.debug("update: {}", angestellter);
    }

    /**
     * Einen vorhandenen Angestellten löschen.
     *
     * @param id Die ID des zu löschenden Angestellten.
     */
    public void deleteById(final UUID id) {
        log.debug("deleteById: id={}", id);
        final OptionalInt index = IntStream
            .range(0, ANGESTELLTE.size())
            .filter(i -> Objects.equals(ANGESTELLTE.get(i).getId(), id))
            .findFirst();
        log.trace("deleteById: index={}", index);
        index.ifPresent(ANGESTELLTE::remove);
        log.debug("deleteById: #ANGESTELLTE={}", ANGESTELLTE.size());
    }
}
