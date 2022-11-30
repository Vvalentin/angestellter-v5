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
package com.acme.angestellter.service;

import com.acme.angestellter.entity.Adresse;
import com.acme.angestellter.entity.Angestellter;
import com.acme.angestellter.repository.AngestellterRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import java.net.URL;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import static com.acme.angestellter.entity.FamilienstandType.LEDIG;
import static com.acme.angestellter.entity.GeschlechtType.WEIBLICH;
import static com.acme.angestellter.entity.InteresseType.LESEN;
import static com.acme.angestellter.entity.InteresseType.REISEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.condition.JRE.JAVA_18;
import static org.junit.jupiter.api.condition.JRE.JAVA_19;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;
import static java.math.BigDecimal.ONE;

@Tag("unit")
@Tag("service_write")
@DisplayName("Anwendungskern fuer Schreiben testen")
@Execution(CONCURRENT)
@EnabledForJreRange(min = JAVA_18, max = JAVA_19)
@ExtendWith(SoftAssertionsExtension.class)
@SuppressWarnings({"WriteTag", "MagicNumber"})
class AngestellterWriteServiceTest {
    private static final String NEUE_PLZ = "12345";
    private static final String NEUER_ORT = "Testort";
    private static final String NEUER_NACHNAME = "Neuernachname";
    private static final String NEUE_EMAIL = "email@test.de";
    private static final String NEUES_GEBURTSDATUM = "2022-02-01";
    private static final String CURRENCY_CODE = "EUR";
    private static final String NEUE_HOMEPAGE = "https://test.de";

    private static final String ID_UPDATE = "00000000-0000-0000-0000-000000000030";
    private static final String ID_DELETE = "00000000-0000-0000-0000-000000000050";

    private final AngestellterRepository repo = new AngestellterRepository();

    // https://hibernate.org/validator/documentation/getting-started
    @SuppressWarnings("resource")
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private final AngestellterWriteService service = new AngestellterWriteService(repo, validator);

    @InjectSoftAssertions
    private SoftAssertions softly;

    @ParameterizedTest(name = "[{index}] Neuanlegen eines neuen Angestellten: nachname={0}, email={1}")
    @CsvSource(
        NEUER_NACHNAME + "," + NEUE_EMAIL + "," + NEUES_GEBURTSDATUM + "," + CURRENCY_CODE + "," + NEUE_HOMEPAGE +
            "," + NEUE_PLZ + "," + NEUER_ORT
    )
    @DisplayName("Neuanlegen eines neuen Angestellten")
    void create(final ArgumentsAccessor args) {
        // given
        final var umsatz = Umsatz
            .builder()
            .betrag(ONE)
            .waehrung(Currency.getInstance(args.get(3, String.class)))
            .build();
        final var adresse = Adresse
            .builder()
            .plz(args.get(5, String.class))
            .ort(args.get(6, String.class))
            .build();
        final var angestellter = Angestellter
            .builder()
            .id(null)
            .nachname(args.get(0, String.class))
            .email(args.get(1, String.class))
            .hasNewsletter(true)
            .geburtsdatum(args.get(2, LocalDate.class))
            .umsatz(umsatz)
            .homepage(args.get(4, URL.class))
            .geschlecht(WEIBLICH)
            .familienstand(LEDIG)
            .interessen(List.of(LESEN, REISEN))
            .adresse(adresse)
            .build();

        // when
        final var angestellterCreated = service.create(angestellter);

        // then
        softly.assertThat(angestellterCreated.getId()).isNotNull();
        softly.assertThat(angestellterCreated.getEmail()).isEqualTo(NEUE_EMAIL);
        softly.assertThat(angestellterCreated.getAdresse().getPlz()).isEqualTo(NEUE_PLZ);
    }

    @ParameterizedTest(name = "[{index}] Aendern eines vorhandenen Angestellten: id={0}")
    @ValueSource(strings = ID_UPDATE)
    @DisplayName("Aendern eines vorhandenen Angestellten")
    void update(final String id) {
        // given
        final var angestellterId = UUID.fromString(id);
        final var angestellterOpt = repo.findById(angestellterId);
        assertThat(angestellterOpt).isNotEmpty();
        final var angestellter = angestellterOpt.get();
        angestellter.setNachname(NEUER_NACHNAME);

        // when
        service.update(angestellter, angestellterId);

        // then
        final var result = repo.findById(angestellterId);
        assertThat(result).isNotEmpty();
        assertThat(result.get().getNachname()).isEqualTo(NEUER_NACHNAME);
    }

    @ParameterizedTest(name = "[{index}] Loeschen eines vorhandenen Angestellten: id={0}")
    @ValueSource(strings = ID_DELETE)
    @DisplayName("Loeschen eines vorhandenen Angestellten")
    void deleteById(final String id) {
        // given
        final var angestellterId = UUID.fromString(id);

        // when
        service.deleteById(angestellterId);

        // then
        final var result = repo.findById(angestellterId);
        assertThat(result).isEmpty();
    }
}
