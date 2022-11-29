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
package com.acme.kunde.entity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.UniqueElements;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Daten eines Kunden. In DDD ist Kunde ist ein Aggregate Root.
 * <img src="../../../../../asciidoc/Kunde.svg" alt="Klassendiagramm">
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
// https://thorben-janssen.com/java-records-hibernate-jpa
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Getter
@Setter
@ToString
@SuppressWarnings({"ClassFanOutComplexity", "JavadocDeclaration", "RequireEmptyLineBeforeBlockTagGroup"})
public class Kunde {
    /**
     * Muster für einen gültigen Nachnamen.
     */
    public static final String NACHNAME_PATTERN =
        "(o'|von|von der|von und zu|van)?[A-ZÄÖÜ][a-zäöüß]+(-[A-ZÄÖÜ][a-zäöüß]+)?";

    /**
     * Kleinster Wert für eine Kategorie.
     */
    public static final long MIN_KATEGORIE = 0L;

    /**
     * Maximaler Wert für eine Kategorie.
     */
    public static final long MAX_KATEGORIE = 9L;

    /**
     * Die ID des Kunden.
     * @param id Die ID.
     * @return Die ID.
     */
    @EqualsAndHashCode.Include
    private UUID id;

    /**
     * Der Nachname des Kunden.
     * @param nachname Der Nachname.
     * @return Der Nachname.
     */
    @NotNull
    @Pattern(regexp = NACHNAME_PATTERN)
    private String nachname;

    /**
     * Die Emailadresse des Kunden.
     * @param email Die Emailadresse.
     * @return Die Emailadresse.
     */
    @Email
    @NotNull
    private String email;

    /**
     * Die Kategorie des Kunden.
     * @param kategorie Die Kategorie.
     * @return Die Kategorie.
     */
    @Min(MIN_KATEGORIE)
    @Max(MAX_KATEGORIE)
    private int kategorie;

    /**
     * Hat der Kunde den Newsletter abonniert.
     * @param hasNewsletter Ist der Newsletter abonniert?
     * @return Ist der Newsletter abonniert?
     */
    private boolean hasNewsletter;

    /**
     * Das Geburtsdatum des Kunden.
     * @param geburtsdatum Das Geburtsdatum.
     * @return Das Geburtsdatum.
     */
    @Past
    private LocalDate geburtsdatum;

    /**
     * Die URL zur Homepage des Kunden.
     * @param homepage Die URL zur Homepage.
     * @return Die URL zur Homepage.
     */
    private URL homepage;

    /**
     * Das Geschlecht des Kunden.
     * @param geschlecht Das Geschlecht.
     * @return Das Geschlecht.
     */
    private GeschlechtType geschlecht;

    /**
     * Der Familienstand des Kunden.
     * @param familienstand Der Familienstand.
     * @return Der Familienstand.
     */
    private FamilienstandType familienstand;

    /**
     * Die Interessen des Kunden.
     * @param interessen Die Interessen.
     * @return Die Interessen.
     */
    @UniqueElements
    @ToString.Exclude
    private List<InteresseType> interessen;

    /**
     * Der Umsatz des Kunden.
     * @param umsatz Der Umsatz.
     * @return Der Umsatz.
     */
    @ToString.Exclude
    private Umsatz umsatz;

    /**
     * Die Adresse des Kunden.
     * @param adresse Die Adresse.
     * @return Die Adresse.
     */
    @Valid
    @ToString.Exclude
    private Adresse adresse;
}
