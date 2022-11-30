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

import com.acme.angestellter.entity.Adresse;
import com.acme.angestellter.entity.Angestellter;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static com.acme.angestellter.entity.FamilienstandType.GESCHIEDEN;
import static com.acme.angestellter.entity.FamilienstandType.LEDIG;
import static com.acme.angestellter.entity.FamilienstandType.VERHEIRATET;
import static com.acme.angestellter.entity.FamilienstandType.VERWITWET;
import static com.acme.angestellter.entity.GeschlechtType.DIVERS;
import static com.acme.angestellter.entity.GeschlechtType.MAENNLICH;
import static com.acme.angestellter.entity.GeschlechtType.WEIBLICH;
import static com.acme.angestellter.entity.InteresseType.LESEN;
import static com.acme.angestellter.entity.InteresseType.REISEN;
import static com.acme.angestellter.entity.InteresseType.SPORT;
import static java.math.BigDecimal.ZERO;
import static java.util.Collections.emptyList;
import static java.util.Locale.GERMANY;

/**
 * Emulation der Datenbasis für persistente Angestellten.
 */
@SuppressWarnings({"UtilityClassCanBeEnum", "UtilityClass", "MagicNumber", "RedundantSuppression"})
final class DB {
    /**
     * Liste der Angestellten zur Emulation der DB.
     */
    @SuppressWarnings("StaticCollection")
    static final List<Angestellter> ANGESTELLTE = getAngestellte();

    private DB() {
    }

    @SuppressWarnings({"FeatureEnvy", "TrailingComment"})
    private static List<Angestellter> getAngestellte() {
        // Helper-Methoden ab Java 9: List.of(), Set.of, Map.of, Stream.of
        try {
            return Stream.of(
                // admin
                Angestellter.builder()
                    .id(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                    .nachname("Admin")
                    .email("admin@acme.com")
                    .hasNewsletter(true)
                    .geburtsdatum(LocalDate.parse("2022-01-31"))
                    .geschlecht(WEIBLICH)
                    .familienstand(VERHEIRATET)
                    .adresse(Adresse.builder().plz("00000").ort("Aachen").build())
                    .build(),
                // HTTP GET
                Angestellter.builder()
                    .id(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                    .nachname("Alpha") //NOSONAR
                    .email("alpha@acme.de")
                    .geburtsdatum(LocalDate.parse("2022-01-01"))
                    .geschlecht(MAENNLICH)
                    .familienstand(LEDIG)
                    .adresse(Adresse.builder().plz("11111").ort("Augsburg").build())
                    .build(),
                Angestellter.builder()
                    .id(UUID.fromString("00000000-0000-0000-0000-000000000002"))
                    .nachname("Alpha")
                    .email("alpha@acme.edu")
                    .geburtsdatum(LocalDate.parse("2022-01-02"))
                    .geschlecht(WEIBLICH)
                    .familienstand(GESCHIEDEN)
                    .adresse(Adresse.builder().plz("22222").ort("Aalen").build())
                    .build(),
                // HTTP PUT
                Angestellter.builder()
                    .id(UUID.fromString("00000000-0000-0000-0000-000000000030"))
                    .nachname("Alpha")
                    .email("alpha@acme.ch")
                    .geburtsdatum(LocalDate.parse("2022-01-03"))
                    .geschlecht(MAENNLICH)
                    .familienstand(VERWITWET)
                    .adresse(Adresse.builder().plz("33333").ort("Ahlen").build())
                    .build(),
                // HTTP PATCH
                Angestellter.builder()
                    .id(UUID.fromString("00000000-0000-0000-0000-000000000040"))
                    .nachname("Delta")
                    .email("delta@acme.uk")
                    .geburtsdatum(LocalDate.parse("2022-01-04"))
                    .geschlecht(WEIBLICH)
                    .familienstand(VERHEIRATET)
                    .adresse(Adresse.builder().plz("44444").ort("Dortmund").build())
                    .build(),
                // HTTP DELETE
                Angestellter.builder()
                    .id(UUID.fromString("00000000-0000-0000-0000-000000000050"))
                    .nachname("Epsilon")
                    .email("epsilon@acme.jp")
                    .hasNewsletter(true)
                    .geburtsdatum(LocalDate.parse("2022-01-05"))
                    .geschlecht(MAENNLICH)
                    .familienstand(LEDIG)
                    .adresse(Adresse.builder().plz("55555").ort("Essen").build())
                    .build(),
                // zur freien Verfuegung
                Angestellter.builder()
                    .id(UUID.fromString("00000000-0000-0000-0000-000000000060"))
                    .nachname("Phi")
                    .email("phi@acme.cn")
                    .geburtsdatum(LocalDate.parse("2022-01-06"))
                    .geschlecht(DIVERS)
                    .familienstand(LEDIG)
                    .adresse(Adresse.builder().plz("66666").ort("Freiburg").build())
                    .build()
            )
            // CAVEAT Stream.toList() erstellt eine "immutable" List
            .collect(Collectors.toList());
        } catch (final Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
