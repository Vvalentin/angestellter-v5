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
package com.acme.kunde.graphql;

import com.acme.kunde.service.ConstraintViolationsException;
import com.acme.kunde.service.EmailExistsException;
import com.acme.kunde.service.NotFoundException;
import graphql.GraphQLError;
import graphql.schema.DataFetchingEnvironment;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.stereotype.Component;

/**
 * Abbildung von Exceptions auf GraphQLError.
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
@Component
@SuppressWarnings("NullableProblems")
final class ExceptionHandler extends DataFetcherExceptionResolverAdapter {
    /**
     * Abbildung der Exceptions auf GraphQLError.
     *
     * @param ex Exception aus dem Anwendungskern
     * @param env Environment-Objekt
     */
    @Override
    @SuppressWarnings({"ReturnCount", "CommentedOutCode"})
    protected GraphQLError resolveToSingleError(
        final Throwable ex,
        @SuppressWarnings("NullableProblems") final DataFetchingEnvironment env
    ) {
        // "Pattern Matching"
        return switch (ex) {
            case final NotFoundException notFound -> new NotFoundError(notFound.getId(), notFound.getSuchkriterien());
            case final EmailExistsException emailExists -> new EmailExistsError(emailExists.getEmail());
            case final DateTimeParseException dateTimeParse -> new DateTimeParseError(dateTimeParse.getParsedString());
            default -> super.resolveToSingleError(ex, env);
        };
    }

    /**
     * Abbildung der Exceptions aus KundeGraphQlController auf GraphQLError.
     *
     * @param ex Exception aus KundeGraphQlController
     * @param env Environment-Objekt
     */
    @Override
    protected List<GraphQLError> resolveToMultipleErrors(
        final Throwable ex,
        @SuppressWarnings("NullableProblems") final DataFetchingEnvironment env
    ) {
        if (ex instanceof final ConstraintViolationsException cve) {
            return cve.getViolations()
                .stream()
                .map(ConstraintViolationError::new)
                .collect(Collectors.toList());
        }

        return super.resolveToMultipleErrors(ex, env);
    }
}
