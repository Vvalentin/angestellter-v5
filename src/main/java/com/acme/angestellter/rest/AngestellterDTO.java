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

import com.acme.angestellter.entity.Adresse;
import com.acme.angestellter.entity.Angestellter;
import com.acme.angestellter.entity.FamilienstandType;
import com.acme.angestellter.entity.GeschlechtType;
import java.time.LocalDate;


/**
 * ValueObject für das Neuanlegen und Ändern eines neuen Angestellten.
 * Beim Lesen wird die Klasse AngestellterModel für die Ausgabe.
 * verwendet.
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 * @param nachname Gültiger Nachname eines Angestellten, d.h. mit einem geeigneten Muster.
 * @param email Email eines Angestellten.
 * @param kategorie Kategorie eines Angestellten mit eingeschränkten Werten.
 * @param hasNewsletter Flag, ob es ein Newsletter-Abo gibt.
 * @param geburtsdatum Das Geburtsdatum eines Angestellten.
 * @param homepage Die Homepage eines Angestellten.
 * @param geschlecht Das Geschlecht eines Angestellten.
 * @param familienstand Der Familienstand eines Angestellten.
 * @param interessen Die Interessen eines Angestellten.
 * @param umsatz Der Umsatz eines Angestellten.
 * @param adresse Die Adresse eines Angestellten.
 */
@SuppressWarnings("RecordComponentNumber")
record AngestellterDTO(
    String nachname,
    String email,
    LocalDate geburtsdatum,
    GeschlechtType geschlecht,
    FamilienstandType familienstand,
    AdresseDTO adresse
) {
    /**
     * Konvertierung in ein Objekt des Anwendungskerns.
     *
     * @return Angestellterobjekt für den Anwendungskern
     */
    Angestellter toAngestellter() {

        final var adresseEntity = adresse() == null
            ? null
            : Adresse
                .builder()
                .plz(adresse().plz())
                .ort(adresse().ort())
                .build();
        return Angestellter
            .builder()
            .id(null)
            .nachname(nachname)
            .email(email)
            .geburtsdatum(geburtsdatum)
            .geschlecht(geschlecht)
            .familienstand(familienstand)
            .adresse(adresseEntity)
            .build();
    }
}
