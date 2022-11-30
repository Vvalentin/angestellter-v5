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
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.LocalDate;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

/**
 * Model-Klasse für Spring HATEOAS. @lombok.Data fasst die Annotationsn @ToString, @EqualsAndHashCode, @Getter, @Setter
 * und @RequiredArgsConstructor zusammen.
 * <img src="../../../../../asciidoc/AngestellterModel.svg" alt="Klassendiagramm">
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
@JsonPropertyOrder({
    "nachname", "email", "kategorie", "hasNewsletter", "geburtsdatum", "homepage", "geschlecht", "familienstand",
    "interessen", "umsatz", "adresse"
})
@Relation(collectionRelation = "angestellte", itemRelation = "angestellter")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Getter
@Setter
@ToString(callSuper = true)
class AngestellterModel extends RepresentationModel<AngestellterModel> {
    private final String nachname;

    @EqualsAndHashCode.Include
    private final String email;
    private final LocalDate geburtsdatum;
    private final GeschlechtType geschlecht;
    private final FamilienstandType familienstand;
    private final Adresse adresse;

    AngestellterModel(final Angestellter angestellter) {
        nachname = angestellter.getNachname();
        email = angestellter.getEmail();
        geburtsdatum = angestellter.getGeburtsdatum();
        geschlecht = angestellter.getGeschlecht();
        familienstand = angestellter.getFamilienstand();
        adresse = angestellter.getAdresse();
    }
}
