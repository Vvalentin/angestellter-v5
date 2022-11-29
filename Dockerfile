# syntax=docker/dockerfile:1.4.3

# Copyright (C) 2020 -  Juergen Zimmermann, Hochschule Karlsruhe
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <https://www.gnu.org/licenses/>.

# Aufruf:   docker buildx build --tag juergenzimmermann/kunde:1.0.0-dockerfile .
#           Get-Content Dockerfile | docker run --rm --interactive hadolint/hadolint:2.12.1-beta-debian

# https://docs.docker.com/engine/reference/builder/#syntax
# https://github.com/moby/buildkit/blob/master/frontend/dockerfile/docs/syntax.md
# https://hub.docker.com/r/docker/dockerfile
# https://containers.gitbook.io/build-containers-the-hard-way
# https://docs.docker.com/develop/develop-images/multistage-build

# ---------------------------------------------------------------------------------------
# S t a g e :   b u i l d e r
#
#   Ubuntu "Jammy Jellyfish" 22.04 LTS mit "Eclipse Temurin" aus "Eclipse Adoptium Project" als JDK
#   Docker-Image fuer OpenJDK ist *deprecated*
#   JAR mit eigenem Code und Dependencies z.B. Spring, Jackson
# ---------------------------------------------------------------------------------------
FROM eclipse-temurin:19.0.1_10-jdk-jammy AS builder
# "working directory" fuer die Docker-Kommandos RUN, ENTRYPOINT, CMD, COPY und ADD
WORKDIR /source
COPY build.gradle.kts gradle.properties gradlew settings.gradle.kts ./
COPY gradle ./gradle
COPY src ./src
# JAR-Datei mit den Schichten ("layers") erstellen und aufbereiten bzw. entpacken
# Default-Kommando
RUN <<EOF
./gradlew bootJar
java -Djarmode=layertools -jar ./build/libs/kunde-1.0.0.jar extract
EOF

# ---------------------------------------------------------------------------------------
# S t a g e
#
#   JRE statt JDK
#   Dependencies z.B. Spring, Jackson
#   Loader fuer Spring Boot
#   Eigener uebersetzter Code
# ---------------------------------------------------------------------------------------

FROM eclipse-temurin:19.0.1_10-jre-jammy
WORKDIR /application
COPY --from=builder /source/dependencies/ ./
COPY --from=builder /source/spring-boot-loader/ ./
#COPY --from=build /source/snapshot-dependencies/ ./
COPY --from=builder /source/application/ ./
# CAVEAT: nur bei Cloud Native Buildpacks, siehe /etc/passwd
USER 1000
# Basis-Kommando, das immer ausgefuehrt wird
ENTRYPOINT ["java", "--enable-preview", "org.springframework.boot.loader.JarLauncher"]
