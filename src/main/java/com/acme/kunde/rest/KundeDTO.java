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
package com.acme.kunde.rest;

import com.acme.kunde.entity.Adresse;
import com.acme.kunde.entity.FamilienstandType;
import com.acme.kunde.entity.GeschlechtType;
import com.acme.kunde.entity.InteresseType;
import com.acme.kunde.entity.Kunde;
import com.acme.kunde.entity.Umsatz;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;

/**
 * ValueObject für das Neuanlegen und Ändern eines neuen Kunden. Beim Lesen wird die Klasse KundeModel für die Ausgabe
 * verwendet.
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 * @param nachname Gültiger Nachname eines Kunden, d.h. mit einem geeigneten Muster.
 * @param email Email eines Kunden.
 * @param kategorie Kategorie eines Kunden mit eingeschränkten Werten.
 * @param hasNewsletter Flag, ob es ein Newsletter-Abo gibt.
 * @param geburtsdatum Das Geburtsdatum eines Kunden.
 * @param homepage Die Homepage eines Kunden.
 * @param geschlecht Das Geschlecht eines Kunden.
 * @param familienstand Der Familienstand eines Kunden.
 * @param interessen Die Interessen eines Kunden.
 * @param umsatz Der Umsatz eines Kunden.
 * @param adresse Die Adresse eines Kunden.
 */
@SuppressWarnings("RecordComponentNumber")
record KundeDTO(
    String nachname,
    String email,
    int kategorie,
    boolean hasNewsletter,
    LocalDate geburtsdatum,
    URL homepage,
    GeschlechtType geschlecht,
    FamilienstandType familienstand,
    List<InteresseType> interessen,
    UmsatzDTO umsatz,
    AdresseDTO adresse
) {
    /**
     * Konvertierung in ein Objekt des Anwendungskerns.
     *
     * @return Kundeobjekt für den Anwendungskern
     */
    Kunde toKunde() {
        final var umsatzEntity = umsatz() == null
            ? null
            : Umsatz
                .builder()
                .betrag(umsatz().betrag())
                .waehrung(umsatz().waehrung())
                .build();
        final var adresseEntity = adresse() == null
            ? null
            : Adresse
                .builder()
                .plz(adresse().plz())
                .ort(adresse().ort())
                .build();
        return Kunde
            .builder()
            .id(null)
            .nachname(nachname)
            .email(email)
            .kategorie(kategorie)
            .hasNewsletter(hasNewsletter)
            .geburtsdatum(geburtsdatum)
            .homepage(homepage)
            .geschlecht(geschlecht)
            .familienstand(familienstand)
            .interessen(interessen)
            .umsatz(umsatzEntity)
            .adresse(adresseEntity)
            .build();
    }
}
