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
package com.acme.angestellter.rest.patch;
import com.acme.angestellter.entity.Angestellter;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import static com.acme.angestellter.rest.patch.PatchOperationType.ADD;
import static com.acme.angestellter.rest.patch.PatchOperationType.REMOVE;
import static com.acme.angestellter.rest.patch.PatchOperationType.REPLACE;

/**
 * Klasse, um PATCH-Operationen auf Angestellter-Objekte anzuwenden.
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
@Component
@Slf4j
public final class AngestellterPatcher {
    AngestellterPatcher() {
    }

    /**
     * PATCH-Operationen werden auf ein Angestellter-Objekt angewandt.
     *
     * @param angestellter Das zu modifizierende Angestellter-Objekt.
     * @param operations   Die anzuwendenden Operationen.
     * @throws InvalidPatchOperationException Falls die Patch-Operation nicht korrekt ist.
     */
    public void patch(final Angestellter angestellter, final Collection<PatchOperation> operations) {
        final var replaceOps = operations.stream()
            .filter(op -> op.op() == REPLACE)
            .toList();
        log.debug("patch: replaceOps={}", replaceOps);
        replaceOps(angestellter, replaceOps);

        final var addOps = operations.stream()
            .filter(op -> op.op() == ADD)
            .toList();
        log.debug("patch: addOps={}", addOps);

        final var removeOps = operations.stream()
            .filter(op -> op.op() == REMOVE)
            .toList();
        log.debug("patch: removeOps={}", removeOps);
    }

    private void replaceOps(final Angestellter angestellter, final Iterable<PatchOperation> ops) {
        ops.forEach(op -> {
            switch (op.path()) {
                case "/nachname" -> angestellter.setNachname(op.value());
                case "/email" -> angestellter.setEmail(op.value());
                default -> throw new InvalidPatchOperationException();
            }
        });
        log.trace("replaceOps: angestellter={}", angestellter);
    }
}
