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
package com.acme.angestellter;

import java.util.List;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import static com.acme.angestellter.security.Rolle.ACTUATOR;
import static com.acme.angestellter.security.Rolle.ADMIN;
import static com.acme.angestellter.security.Rolle.ANGESTELLTER;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.security.crypto.factory.PasswordEncoderFactories.createDelegatingPasswordEncoder;

/**
 * Security-Konfiguration.
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">J??rgen Zimmermann</a>
 */
// https://github.com/spring-projects/spring-security/tree/master/samples
interface SecurityConfig {
    /**
     * Bean-Definition, um den Zugriffsschutz an der REST-Schnittstelle zu konfigurieren.
     *
     * @param http Injiziertes Objekt von HttpSecurity als Ausgangspunkt f??r die Konfiguration.
     * @return Objekt von SecurityFilterChain
     * @throws Exception Wegen HttpSecurity.authorizeHttpRequests()
     */
    @Bean
    @SuppressWarnings("LambdaBodyLength")
    default SecurityFilterChain securityFilterChainFn(final HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(authorize -> {
                final var restPath = "/rest";
                final var restPathAngestellterId = restPath + "/*";
                authorize
                    // https://spring.io/blog/2020/06/30/url-matching-with-pathpattern-in-spring-mvc
                    // https://docs.spring.io/spring-security/reference/6.0.0-RC1/servlet/integrations/mvc.html
                    .requestMatchers(GET, restPath).hasRole(ADMIN.name())
                    .requestMatchers(GET, restPath + "/nachname/*").hasRole(ADMIN.name())
                    .requestMatchers(GET, restPathAngestellterId).hasAnyRole(ADMIN.name(), ANGESTELLTER.name())
                    .requestMatchers(PUT, restPathAngestellterId).hasRole(ADMIN.name())
                    .requestMatchers(PATCH, restPathAngestellterId).hasRole(ADMIN.name())
                    .requestMatchers(DELETE, restPathAngestellterId).hasRole(ADMIN.name())

                    .requestMatchers(EndpointRequest.to(HealthEndpoint.class)).permitAll()
                    .requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole(ACTUATOR.name())

                    .requestMatchers(POST, restPath).permitAll()
                    .requestMatchers(POST, "/graphql").permitAll()
                    .requestMatchers(GET, "/v3/api-docs.yaml").permitAll()
                    .requestMatchers(GET, "/v3/api-docs").permitAll()
                    .requestMatchers(GET, "/graphiql").permitAll()
                    .requestMatchers(GET, "/error").permitAll()

                    .anyRequest().authenticated();
            })
            .httpBasic()
            .and()
            .formLogin().disable()
            .csrf().disable()
            .build();
    }

    /**
     * Bean-Definition, um den Verschl??sselungsalgorithmus f??r Passw??rter bereitzustellen. Es wird der
     * Default-Algorithmus von Spring Security verwendet: bcrypt.
     *
     * @return Objekt f??r die Verschl??sselung von Passw??rtern.
     */
    @Bean
    default PasswordEncoder passwordEncoder() {
        return createDelegatingPasswordEncoder();
    }

    /**
     * Bean, um Test-User anzulegen. Dazu geh??ren jeweils ein Benutzername, ein Passwort und diverse Rollen.
     * Das wird in Beispiel 2 verbessert werden.
     *
     * @param passwordEncoder Injiziertes Objekt zur Passwort-Verschl??sselung
     * @return Ein Objekt, mit dem diese (Test-) User verwaltet werden, z.B. f??r die k??nftige Suche.
     */
    @Bean
    default UserDetailsService userDetailsService(final PasswordEncoder passwordEncoder) {
        final var password = passwordEncoder.encode("p");

        final var users = List.of(
            User.withUsername("admin")
                .password(password)
                .roles(ADMIN.name(), ANGESTELLTER.name(), ACTUATOR.name())
                .build(),
            User.withUsername("alpha")
                .password(password)
                .roles(ANGESTELLTER.name())
                .build()
        );

        return new InMemoryUserDetailsManager(users);
    }
}
