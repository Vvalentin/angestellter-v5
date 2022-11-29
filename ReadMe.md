# Hinweise zum Programmierbeispiel

<!--
  Copyright (C) 2020 - present Juergen Zimmermann, Hochschule Karlsruhe

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
-->

[Juergen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)

> Diese Datei ist in Markdown geschrieben und kann z.B. mit IntelliJ IDEA
> gelesen werden. Näheres zu Markdown gibt es z.B. bei
> [Markdown Guide](https://www.markdownguide.org) oder
> [JetBrains](https://www.jetbrains.com/help/hub/Markdown-Syntax.html)

Inhalt

- [Powershell](#Powershell)
- [Speichereinstellung für Gradle](#Speichereinstellung-für-Gradle)
- [Übersetzung und lokale Ausführung](#Übersetzung-und-lokale-Ausführung)
  - [Ausführung in IntelliJ IDEA](#Ausführung-in-IntelliJ-IDEA)
  - [Kommandozeile](#Kommandozeile)
  - [Port 8080 oder 8443](#Port-8080-oder-8443)
  - [Aufrufe mit HTTP Client von IntelliJ IDEA oder mit der PowerShell](#Aufrufe-mit-HTTP-Client-von-IntelliJ-IDEA-oder-mit-der-PowerShell)
    [OpenAPI-mit-Swagger](#OpenAPI-mit-Swagger)
  - [Liveness und Readiness](#Liveness-und-Readiness)
  - [Kotlin Daemon](#Kotlin-Daemon)
  - [Kontinuierliches Monitoring von Dateiänderungen](#Kontinuierliches-Monitoring-von-Dateiänderungen)
- [Image, Container und Docker Dashboard](#Image,-Container-und-Docker-Dashboard)
  - [Image erstellen](#Image-erstellen)
  - [Container mit dem manuell erstellten Image starten](#Container-mit-dem-manuell-erstellten-Image-starten)
  - [Docker Compose](#Docker-Compose)
  - [Cloud Native Buildpacks durch Spring Boot](#Cloud-Native-Buildpacks-durch-Spring-Boot)
  - [Image inspizieren](#Image-inspizieren)
  - [Image kopieren](#Image-kopieren)
- [Kubernetes, Helm und Skaffold](#Kubernetes,-Helm-und-Skaffold)
  - [WICHTIG: Schreibrechte für die Log-Datei](#WICHTIG:-Schreibrechte-für-die-Logdatei)
  - [Rechnername in der Datei hosts](#Rechnername-in-der-Datei-hosts)
  - [CLI durch kubectl](#CLI-durch-kubectl)
  - [Eigener Namespace und Defaults](#Eigener-Namespace-und-Defaults)
  - [Image für Kubernetes erstellen](#Image-für-Kubernetes-erstellen)
  - [Installation in Kubernetes: Deployment, Service und Configmap](#Installation-in-Kubernetes:-Deployment,-Service-und-Configmap)
  - [Deinstallieren des Microservice](#Deinstallieren-des-Microservice)
  - [Helm als Package Manager für Kubernetes](#Helm-als-Package-Manager-für-Kubernetes)
  - [Installation mit helmfile und Port Forwarding](#Installation-mit-helmfile-und-Port-Forwarding)
  - [Skaffold für Continuous Deployment](#Skaffold-für-Continuous-Deployment)
  - [kubectl top](#kubectl-top)
  - [Validierung der Installation](#Validierung-der-Installation)
  - [Administration des Kubernetes-Clusters](#Administration-des-Kubernetes-Clusters)
- [Statische Codeanalyse](#Statische-Codeanalyse)
  - [ktlint und Detekt](#ktlint-und-Detekt)
  - [SonarQube](#SonarQube)
- [Dokumentation](#Dokumentation)
  - [Dokumentation durch AsciiDoctor und PlantUML](#Dokumentation-durch-AsciiDoctor-und-PlantUML)
  - [API Dokumentation durch Dokka](#API-Dokumentation-durch-Dokka)
- [Monitoring mit Grafana und Prometheus](#Monitoring-mit-Grafana-und-Prometheus)
- [Einfache Lasttests mit Fortio](#Einfache-Lasttests-mit-Fortio)


## Powershell

Überprüfung, ob sich Powershell-Skripte starten lassen:

```powershell
    Get-ExecutionPolicy -list
```

`CurrentUser` muss _zumindest_ das Recht `RemoteSigned` haben. Ggf. muss dieses
Ausführungsrecht gesetzt werden:

```powershell
    Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

Ggf. genügt `RemoteSigned` nicht und man muss `Bypass` verwenden, sodass
keine Ausführung blockiert wird und dabei keine Warnings ausgegeben werden.
Das hängt von der eigenen Windows-Installation ab. Details siehe
https://docs.microsoft.com/en-us/powershell/module/microsoft.powershell.security/set-executionpolicy?view=powershell-7.2

---

## Speichereinstellung für Gradle

Falls die Speichereinstellung für Gradle zu großzügig ist, kann man in
`gradle.properties` bei `org.gradle.jvmargs` den voreingestellten Wert
(2 GB) ggf. reduzieren.

---

## Übersetzung und lokale Ausführung

### Ausführung in IntelliJ IDEA

Am rechten Rand auf den Button _Gradle_ klicken und in _Tasks_ > _application_
_bootRun_ durch einen Doppelklick starten.

Danach gibt es in der Werkzeugleiste im Auswahlmenü den Eintrag _kunde [bootRun]_
und man kann den Server auch damit (neu-) starten, stoppen und ggf. debuggen.

```Text
    LOG_PATH=./build/log;APPLICATION_LOGLEVEL=trace;HIBERNATE_LOGLEVEL=debug
```

### Kommandozeile

In einer Powershell wird der Server mit dem Spring-_Profile_ `dev` gestartet:

```powershell
    .\gradlew bootRun
```

Mit `<Strg>C` kann man den Server herunterfahren, weil in der Datei
`application.yml` im Verzeichnis `src\main\resources` _graceful shutdown_
konfiguriert ist.

### Port 8080 oder 8443

Normalerweise nimmt man beim Entwickeln für HTTP den Port `8080` und für HTTPS
den Port `8443`. Damit das Entwickeln mit Kubernetes nicht zu komplex wird,
wird beim finalen Beispiel am Vorlesungsende auf TLS bzw. HTTPS verzichtet und
"nur" HTTP genutzt. Deshalb wird in diesem Beispiel bereits der Port 8080 auch
für HTTPS genutzt.

### Aufrufe mit HTTP Client von IntelliJ IDEA oder mit der PowerShell

Im Verzeichnis `extras\http-client` gibt es Dateien mit der Endung `.http`, in
denen HTTP-Requests vordefiniert sind. Diese kann man mit verschiedenen
Umgebungen ("environment") ausführen.

Mit _Invoke-WebRequest_ kann man in einer PowerShell eine Sequenz von z.B. 5 Requests
folgendermaßen absetzen und beobachten:

```powershell
    $secpasswd = ConvertTo-SecureString p -AsPlainText -Force
    $credential = New-Object System.Management.Automation.PSCredential('admin', $secpasswd)
    for ($i = 1; $i -le 5; $i++) {
        Write-Output ''
        $response = Invoke-WebRequest https://localhost:8080/00000000-0000-0000-0000-00000000000$i `
            -HttpVersion 2 -SslProtocol Tls13 -SkipCertificateCheck `
            -Authentication Basic -Credential $credential -Headers @{Accept = 'application/hal+json'}
        $statuscode = $response.StatusCode
        Write-Output "${i}: ${statuscode}"
    }
    Write-Output $response.RawContent
```

Alternativ kann man auch _cURL_ verwenden.

### OpenAPI mit Swagger

Mit der URL `https://localhost:8080/swagger-ui.html` kann man in einem
Webbrowser den RESTful Web Service über eine Weboberfläche nutzen, die
von _Swagger_ auf der Basis von der Spezifikation _OpenAPI_ generiert wurde.
Die _Swagger JSON Datei_ kann man mit `https://localhost:8080/v3/api-docs`
abrufen.

### Liveness und Readiness

Mit der URL `https://localhost:8080/actuator/health/liveness` kann die
_Liveness_ des gestarteten Servers überprüft werden, d.h. ob der Server an
sich gestartet ist. Dazu gehört _nicht_ die Prüfung, ob evtl. externe Server
wie z.B. DB-Server oder Mailserver erreichbar sind.

Mit der URL `https://localhost:8080/actuator/health/readiness` kann die
_Readiness_ des gestarteten Servers überprüft werden, d.h. ob der Server bereit
ist, Requests entgegenzunehmen.

Diese URLs für Liveness und Readiness können später für die Lastbalanzierung
z.B. in der Cloud verwendet werden.

### Kotlin Daemon

Falls der _Kotlin Daemon_ beim Übersetzen nicht mehr reagiert, sollte man
alte Dateien im Verzeichnis `%LOCALAPPDATA%\kotlin\daemon` löschen.

### Kontinuierliches Monitoring von Dateiänderungen

Kontinuierliches Monitoring durch die _DevTools_ von _Spring Boot_
kann für einen lokal laufenden Server genutzt werden.

---

## Image, Container und Docker Dashboard

### Docker-Daemon und Benutzerkennung

Die Kommunikation mit dem _Docker-Daemon_, d.h. _Dienst_ bei Windows, sollte
mit der Benutzerkennung erfolgen, mit der man _Docker Desktop_ installiert hat,
weil diese Benutzerkennung bei der Installation zur Windows-Gruppe
`docker-users` hinzugefügt wurde. Deshalb sollte man auch _Docker Desktop_
stets mit dieser Benutzerkennung starten.

### Image erstellen

Durch die (Konfigurations-) Datei `Dockerfile` kann man ein Docker-Image
erstellen und z.B. durch ein _Multi-stage Build_ optimieren. Ob das
`Dockerfile` gemäß _Best Practices_ erstellt wurde, kann man in einer
PowerShell mit folgendem Kommando überprüfen:

```powershell
    Get-Content Dockerfile | docker run --rm --interactive hadolint/hadolint:2.12.1-beta-debian
```

Eine weitverbreitete Namenskonvention für ein Docker-Image ist
`<registry-name>/<username>/<image-name>:<image-tag>`. Damit kann man dann ein
Docker-Image mit folgendem Kommando erstellen.

```powershell
    docker buildx build --tag juergenzimmermann/kunde:1.0.0-dockerfile .
```

Wenn das Image gebaut wird, kann ggf. noch die Option `--no-cache` angegeben
werden, um zu unterbinden, dass Daten aus dem (Docker-) Cache verwendet werden.

Nachdem das Docker-Image erstellt ist, kann man es im _Docker Dashboard_
inspizieren. Falls das Docker Dashboard geschlossen wurde, kann man es
folgendermaßen wieder öffnen:

- Im _System Tray_ (rechts unten in der _Taskleiste_) ist das Docker-Icon
  (_Whale_).
- Wenn man das Whale-Icon anklickt, wird das _Docker Dashboard_ aufgerufen.

**Beachte**: Die Datei `Dockerfile` wird im 2. Beispiel bzw. für die 2. Abgabe
nicht mehr benötigt.

Mit der PowerShell kann man Docker-Images folgendermaßen auflisten und löschen,
wobei das Unterkommando `rmi` die Kurzform für `image rm` ist:

```powershell
    docker images | sort
    docker rmi myImage:myTag
```

Im Laufe der Zeit kann es immer wieder Images geben, bei denen der Name
und/oder das Tag `<none>` ist, sodass das Image nicht mehr verwendbar und
deshalb nutzlos ist. Solche Images kann man mit dem nachfolgenden Kommando
herausfiltern und dann unter Verwendung ihrer Image-ID, z.B. `9dd7541706f0`
löschen:

```powershell
    docker images | Select-String -Pattern '<none>'
    docker rmi 9dd7541706f0
```

### Container mit dem manuell erstellten Image starten

Einen Docker-Container kann man mit einem Docker-Image starten, indem man das
nachfolgende Kommando aufruft:

```powershell
    docker run --publish 8080:8080 `
      --env TZ=Europe/Berlin `
      --env SPRING_PROFILES_ACTIVE=dev `
      --env APPLICATION_LOGLEVEL=trace `
      --mount 'type=bind,source=C:\Zimmermann\volumes\kunde-v1,destination=/tmp' `
      --memory 1024m --cpus 1 --hostname kunde `
      --name kunde --rm juergenzimmermann/kunde:1.0.0-dockerfile
```

Jetzt läuft der Microservice als Docker-Container mit `HTTPS`, wobei auch der
Container-interne Port 8080 des Microservice "kunde" als Port 8080 für
localhost freigegeben wurde. Die Log-Datei `application.log` befindet sich im
Container im Verzeichnis `/tmp`, das durch Mounting im
Windows-Verzeichnis `C:\Zimmermann\volumes\kunde-v1` zugreifbar ist.

Mit dem _HTTP Client_ von IntelliJ IDEA können nun HTTP-Requests (GET, POST,
PUT, PATCH, DELETE) abgeschickt werden.

Den gestarteten Docker-Container kann man im Docker Dashboard sehen und später
auch beenden. Dabei wird der im Container enthaltene Server für den Microservice
mit "graceful shutdown" heruntergefahren.

### Docker Compose

Statt direkt `docker run` mit all den Optionen aufzurufen, kann man das neue
_Docker Compose V2_, das in Go statt in Python implementiert ist, als
Docker-Plugin verwenden. Dazu ist die (Default-) Konfigurationsdatei
`docker-compose.yaml` notwendig, die im Verzeichnis `extras` gespeichert ist.
Dann lässt sich der Container folgendermaßen starten und später in einer
weiteren PowerShell herunterfahren:

```powershell
    docker compose up

    # In einer 2. PowerShell:
    docker compose exec kunde bash
        ps -ef
        env
        ls -l /layers
        ls -l /layers/paketo-buildpacks_adoptium/jre
        exit

    docker compose down
```

Statt eine PowerShell zu verwenden, kann man Docker Compose auch direkt in
IntelliJ aufrufen, indem man über das Kontextmenü ("rechte Maustaste") den
Unterpunkt _Run 'docker-compose.yaml:...'_ aufruft. Im Tool-Window _Services_
sieht man dann unterhalb von _Docker_ den Eintrag _Docker-compose: kunde_ mit
dem Service _kunde_. Sobald man `/kunde` auswählt, kann man Folgendes inspizieren:
- Log
- Properties
- Environment Variables
- Port Bindings
- Volume Bindings

**Beachte**: Die Datei `docker-compose.yaml` wird im 2. Beispiel bzw. für
die 2. Abgabe nicht mehr benötigt.

### Cloud Native Buildpacks durch Spring Boot

Mit der Gradle-Task `bootBuildImage` von Spring Boot kann man unter Verwendung
von _Cloud Native Buildpacks_ (cnb) ein optimiertes und geschichtetes ("Layer")
Docker-Image erstellen, ohne dass ein `Dockerfile` notwendig ist.

```powershell
    .\gradlew bootBuildImage
```

Während der Task `bootBuildImage` werden für die "Buildpacks" von Paketo
Docker-Images und auch einige Archive von Github heruntergeladen.

Mit dem folgenden Kommando kann man dann einen Docker-Container mit dem Image
`juergenzimmermann/kunde` starten (s.o.).

```powershell
    cd extras
    docker compose up
    ...
    # In einer 2. PowerShell:
    docker compose down
```

### Image inspizieren

#### docker inspect

Mit dem Unterkommando `inspect` von `docker` kann man inspizieren, wie ein
Image konfiguriert und aufgebaut ist. Die Ausgabe erfolgt im Format `JSON`,
z.B.:

```powershell
    docker inspect juergenzimmermann/kunde:1.0.0
```

#### pack inspect

Wenn ein Docker-Image mit Buildpacks gebaut wurde, kann man mit folgendem
Kommando inspizieren, mit welchen Software-Paketen es gebaut wurde:

```PowerSkell
    pack inspect juergenzimmermann/kunde:1.0.0
```

#### dive

Mit _dive_ kann man ein Docker-Image und die einzelnen Layer inspizieren, z.B.:

```powershell
    cd \Zimmermann\dive
    .\dive juergenzimmermann/kunde:1.0.0
```

#### Tool Window "Services" von IntelliJ IDEA

Im Tool Window _Services_ von IntelliJ IDEA gibt es den Eintrag _Docker_ mit
dem Unterpunkt _Images_, wo man ähnlich wie mit _dive_ ein Image und dessen
Layers inspizieren kann.

### Image kopieren

Mit `docker save` kann man ein Docker Image im Format `tar` abspeichern und
dann ggf. kopieren:

```powershell
    docker save juergenzimmermann/kunde:1.0.0 > kunde.tar
```

Mit `docker load` kann man anschließend ein Image aus dem Format `tar`
wiederherstellen:

```powershell
    docker load < kunde.tar
```

---

## Kubernetes, Helm und Skaffold

### WICHTIG: Schreibrechte für die Logdatei

Wenn die Anwendung in Kubernetes läuft, ist die Log-Datei `application.log` im
Verzeichnis `C:\Zimmermann\volumes\kunde-v1`. Das bedeutet auch zwangsläufig,
dass diese Datei durch den _Linux-User_ vom (Kubernetes-) Pod angelegt und
geschrieben wird, wozu die erforderlichen Berechtigungen in Windows gegeben
sein müssen.

### Rechnername in der Datei hosts

Wenn man mit Kubernetes arbeitet, bedeutet das auch, dass man i.a. über TCP
kommuniziert. Deshalb sollte man überprüfen, ob in der Datei
`C:\Windows\System32\drivers\etc\hosts` der eigene Rechnername mit seiner
IP-Adresse eingetragen ist. Zum Editieren dieser Datei sind Administrator-Rechte
notwendig.

### CLI durch kubectl

`kubectl` ist ein _CLI_ (= Command Line Interface) für Kubernetes und bietet
etliche Unterkommandos, wie z.B. `kubectl apply`, `kubectl create`, `kubectl get`,
`kubectl describe` oder `kubectl delete`.

### Eigener Namespace und Defaults

In Kubernetes gibt es Namespaces ("Namensräume") ähnlich wie in

- Betriebssystemen durch Verzeichnisse, z.B. in Windows oder Unix
- Programmiersprachen, z.B. durch `package` in Kotlin und Java
- Datenbanksystemen, z.B. in Oracle und PostgreSQL.

Genauso wie in Datenbanksystemen gibt es in Kubernetes _keine_ untergeordneten
Namespaces. Vor allem ist es in Kubernetes empfehlenswert für die eigene
Software einen _neuen_ Namespace anzulegen und **NICHT** den Default-Namespace
zu benutzen. Das wurde bei der Installation von Kubernetes durch den eigenen
Namespace `acme` bereits erledigt. Außerdem wurde aus Sicherheitsgründen beim
defaultmäßigen Service-Account das Feature _Automounting_ deaktiviert und der
Kubernetes-Cluster wurde intern defaultmäßig so abgesichert, dass

- über das Ingress-Gateway keine Requests von anderen Kubernetes-Services zulässig sind
- über das Egress-Gateway keine Requests an andere Kubernetes-Services zulässig sind.

### Image für Kubernetes erstellen

Zunächst wird ein Docker-Image benötigt, das z.B. mit dem
[Gradle-Plugin von Spring Boot](#Cloud-Native-Buildpacks-durch-Spring-Boot)
als optimiertes und geschichtetes Image erstellt wird.

Buildpacks durch Spring Boot unterstützen Java 17. Bei Verwendung der
Buildpacks werden ggf.  einige Archive von Github heruntergeladen, wofür es
leider kein Caching gibt. Ein solches Image kann mit dem Linux-User `cnb`
gestartet werden.

```powershell
    .\gradlew bootBuildImage
```

### Installation in Kubernetes: Deployment, Service und Configmap

Damit ein Docker-Image in Kubernetes als Docker-Container laufen kann, sind
zusätzliche Einstellungen z.B. durch Umgebungsvariable notwendig. Solche
Key-Value-Paare kann man in Kubernetes durch eine _Configmap_ bereitstellen.

Das Image wird innerhalb von Kubernetes durch ein _Deployment_ konfiguriert
und kann mit einer _Configmap_ für die Umgebungsvariable verknüpft werden.
Außerdem wird ein Deployment i.a. mit einem _Service_ verbunden, um externen
Zugriff von z.B. anderen Deployments zu ermöglichen. Die zugehörige Manifest-
bzw. Konfigurationsdatei für _Configmap_, _Service_ und _Deployment_ ist
`kubernetes.yaml` im Unterverzeichnis `kubernetes`.

Die Installation in Kubernetes erfolgt durch das nachfolgende Kommando, sodass
der Microservice dann innerhalb von Kubernetes mit `HTTPS` läuft. Dabei wird
die Logdatei im internen Verzeichnis `/var/log/spring` angelegt, welches  durch
_Mounting_ dem Windows-Verzeichnis `C:\Zimmermann\volumes\kunde-v1` entspricht
und mit _Schreibberechtigung_ existieren muss, z.B. für die  Benutzergruppen
`Authentifizierte Benutzer` und `Benutzer`.

```powershell
    # Image mit Spring Boot und Cloud Native Buildpack
    .\gradlew bootBuildImage

    kubectl create -f extras\kubernetes\kubernetes.yaml --namespace acme
```

Die Datei `kubernetes.yaml` wird übrigens im 2. Beispiel bzw. für die 2. Abgabe
_nicht_ mehr benötigt.

Falls es beim Deployment-Vorgang zum Fehler _CrashLoopBackOff_ kann es z.B.
eine dieser Ursachen haben:

* Fehler beim Deployment-Vorgang selbst
* Fehler bei "Liveness Probe"

### Skalierung

Wenn weitere Replica-Server im laufenden Betrieb erforderlich sind, kann man
das Deployment folgendermaßen skalieren:

```powershell
    kubectl scale deployment/kunde-v1 --replicas 2 --namespace acme
```

### Deinstallieren des Microservice

Um den Microservice vollständig zu deinstallieren, müssen _Configmap_,
_Deployment_ und _Service_ aus Kubernetes entfernt werden:

```powershell
    kubectl delete -f extras\kubernetes\kubernetes.yaml --namespace acme
```

Dabei wird implizit auch das _Pod_ entfernt, was vereinfachend als
virtualisierter Rechner betrachtet werden kann.

### Helm als Package Manager für Kubernetes

_Helm_ ist ein _Package Manager_ für Kubernetes mit einem _Template_-Mechanismus
auf der Basis von _Go_.

Zunächst muss man mit dem
[Gradle-Plugin von Spring Boot](#Cloud-Native-Buildpacks-durch-Spring-Boot)
ein Docker-Image erstellen:

```powershell
    .\gradlew bootBuildImage
```

Das _Helm-Chart_ heißt `kunde` und ist deshalb in einem gleichnamigen Verzeichnis,
das zur besseren Strukturierung unterhalb von `extras` ist. Die Metadaten sind
in der Datei `Chart.yaml` und die einzelnen Manifest-Dateien sind im
Unterverzeichis `templates` im Format YAML. In diesen Dateien gibt es Platzhalter
("templates") mit der Syntax der Programmiersprache Go. Die Defaultwerte für diese
Platzhalter sind in der Datei `values.yaml` und können beim Installieren
durch weitere YAML-Dateien überschrieben werden. Im unten stehenden Beispiel
wird so ein _Helm-Service_ mit dem _Release-Namen_ `kunde` mit dem Helm-Chart aus dem
aktuellen Verzeicnis in Kubernetes installiert. Dabei muss die Umgebungsvariable
`HELM_NAMESPACE` auf den Wert `acme` gesetzt sein.

```powershell
    # Ueberpruefen, ob die Umgebungsvariable HELM_NAMESPACE gesetzt ist:
    Write-Output $env:HELM_NAMESPACE

    cd extras\kunde
    helm lint --strict .
    helm-docs

    # einfacher: helmfile oder Skaffold (s.u.)
    helm install kunde . -f values.yaml -f dev.yaml --dry-run
    helm install kunde . -f values.yaml -f dev.yaml
    helm list
    helm status kunde
```

Die Option `--dry-run` bewirkt, dass keine Installation durchgeführt wird, sondern
nur *EINE* YAML-Datei generiert wird, in der die gesamte Installationskonfiguration
enthalten ist.

Das Deployment ist dabei so realisiert, dass `TLS` und `HTTP2` zur Vereinfachung
deaktiviert sind. Außerdem wird die Logdatei im internen Verzeichnis `/tmp` angelegt,
welches durch _Mounting_ dem Windows-Verzeichnis `C:\Zimmermann\volumes\kunde-v1`
entspricht und mit _Schreibberechtigung_ existieren muss.

Später kann das Helm-Chart mit dem Release-Namen _kunde_ auch deinstalliert werden:

```powershell
    cd extras\kunde
    helm uninstall kunde
```

### Installation mit helmfile und Port Forwarding

```powershell
    helmfile apply

    helm list
    helm status kunde

    kubectl describe svc/kunde -n acme
    # in Lens: Network > Endpoints
    kubectl get ep -n acme

    .\port-forward.ps1

    # Deinstallieren
    helmfile destroy
```

Gegenüber Skaffold (s.u.) hat helmfile allerdings folgende **Defizite**:

- helmfile funktioniert nur mit Helm, aber nicht mit _Kustomize_, _kubectl_, _kpt_
- Continuous Deployment wird nicht unterstützt
- Die Konsole des Kubernetes-Pods sieht man nicht
- Port-Forwarding muss man selbst einrichten bzw. aufrufen

Um beim Entwickeln von localhost (und damit von außen) auf einen
Kubernetes-Service zuzugreifen, ist _Port-Forwarding_ die einfachste
Möglichkeit, indem das nachfolgende Kommando für den installierten Service mit
Name _kunde_ aufgerufen wird. Alternativ kann auch das Skript `port-forward.ps1`
aufgerufen werden.

```powershell
    kubectl port-forward svc/kunde 8080 -n acme
```

Nach dem Port-Forwarding kann man z.B. mit dem _HTTP Client_ von IntelliJ auf
den in Kubernetes laufenden Service zugreifen.

Mit _Invoke-WebRequest_ ist z.B. folgender Aufruf in einer PowerShell denkbar:

```powershell
    $secpasswd = ConvertTo-SecureString p -AsPlainText -Force
    $credential = New-Object System.Management.Automation.PSCredential('admin', $secpasswd)
    $response = Invoke-WebRequest https://localhost:8080/00000000-0000-0000-0000-000000000001 `
       -SslProtocol Tls13 -HttpVersion 2 -SkipCertificateCheck `
       -Authentication Basic -Credential $credential -Headers @{Accept = 'application/hal+json'}
    Write-Output $response.RawContent
```

Mit _cURL_ lautet der Aufruf:

```powershell
    curl --verbose --user admin:p http://localhost:8080/00000000-0000-0000-0000-000000000001
```

Ein _Ingress Controller_ ist zuständig für das _Traffic Management_ bzw. Routing
der eingehenden Requests zu den Kubernetes Services. Ein solcher Ingress Controller
wurde durch `extras\kunde\templates\ingress.yaml` installiert und kann von
außen z.B. folgendermaßen aufgerufen werden, falls der eigentliche Kommunikationsendpunkt
in Kubernetes verfügbar ist.

```powershell
    # ca. 2. Min. warten, bis der Endpoint bei kunde verfuegbar ist (in Lens: Network > Endpoints)
    kubectl get ep -n acme

    $secpasswd = ConvertTo-SecureString p -AsPlainText -Force
    $credential = New-Object System.Management.Automation.PSCredential('admin', $secpasswd)

    # GET-Request fuer REST-Schnittstelle mit Invoke-WebRequest:
    $response = Invoke-WebRequest https://kubernetes.docker.internal/kunden/00000000-0000-0000-0000-000000000001 `
        -Headers @{Accept = 'application/hal+json'} `
        -SslProtocol Tls13 -HttpVersion 2 -SkipCertificateCheck `
        -Authentication Basic -Credential $credential
    Write-Output $response.RawContent

    # GraphQL mit Invoke-WebRequest:
    $response = Invoke-WebRequest https://kubernetes.docker.internal/kunden/graphql `
        -Method Post -Body '{"query": "query { kunde(id: \"00000000-0000-0000-0000-000000000001\") { nachname } }"}' `
        -ContentType 'application/json' `
        -SslProtocol Tls13 -HttpVersion 2 -SkipCertificateCheck `
        -Authentication Basic -Credential $credential
    Write-Output $response.RawContent

    # GET-Request fuer REST-Schnittstelle mit cURL:
    curl --verbose --user admin:p --tlsv1.3 --http2 --insecure https://kubernetes.docker.internal/kunden/00000000-0000-0000-0000-000000000001

    # GraphQL mit cURL:
    curl --verbose --data '{"query": "query { kunde(id: \"00000000-0000-0000-0000-000000000001\") { nachname } }"}' `
        --header 'Content-Type: application/json' `
        --tlsv1.3 --insecure `
        --user admin:p `
        https://kubernetes.docker.internal/kunden/graphql
```

### Skaffold für Continuous Deployment

_Skaffold_ ist ein Werkzeug, das den eigenen Quellcode beobachtet. Wenn Skaffold
Änderungen festestellt, wird das Image automatisch neu gebaut und ein Redeployment
in Kubernetes durchgeführt.

Um das Image mit dem Tag `1.0.0` zu bauen, muss die Umgebungsvariable `TAG` auf
den Wert `1.0.0` gesetzt sein. Dabei ist auf die Großschreibung bei der
Umgebungsvariablen zu achten.

In `skaffold.yaml` ist konfiguriert, dass das Image mit _Cloud Native
Buildpacks_ gebaut wird.

Weiterhin gibt es in Skaffold die Möglichkeit, _Profile_ zu definieren, um z.B.
verschiedene Werte bei der Installation mit Helm zu verwenden. Dazu ist in
skaffold.yaml beispielsweise konfiguriert, dass die Umgebungsvariable
`SKAFFOLD_PROFILE` auf `dev` gesetzt sein muss, um bei Helm zusätzlich die Datei
`dev.yaml` zu verwenden.

Das Deployment wird mit Skaffold nun folgendermaßen durchgeführt und kann mit
`<Strg>C` abgebrochen bzw. zurückgerollt werden:

```powershell
    skaffold dev

    helm list
    helm status kunde

    kubectl describe svc/kunde -n acme
    # in Lens: Network > Endpoints
    kubectl get ep -n acme

    <Strg>C
    skaffold delete
```

Bis der Endpoint für den Service "kunde" verfügbar ist, muss man ggf. ein
bisschen warten. Aufgrund der Einstellungen für _Liveness_ und _Readiness_
kann es einige Minuten dauern, bis in der PowerShell angezeigt wird, dass die
Installation erfolgreich war. Mit [Lens](#Lens) oder [Octant](#Octant) kann man
jedoch die Log-Einträge inspizieren und so vorher sehen, ob die Installation
erfolgreich war. Sobald der Endpoint verfügbar ist, sieht man in der PowerShell
auch die Konsole des gestarteten (Kubernetes-) Pods.

Außerdem generiert Skaffold noch ein SHA-Tag zusätzlich zu `1.0.0`.
Das kann man mit `docker images | sort` sehen. Von Zeit zu Zeit sollte man
mittels `docker rmi <image:tag>` aufräumen.

Wenn man nun in IntelliJ IDEA den Quellcode des Microservice editiert und dieser
durch IJ unmittelbar übersetzt wird, dann überwacht dabei Skaffold die
Quellcode-Dateien, baut ein neues Image und führt einen neuen Deployment-Vorgang
aus. Deshalb spricht man von **Continuous Deployment**.

### kubectl top

Mit `kubectl top pods -n acme` kann man sich die CPU- und RAM-Belegung der Pods
anzeigen lassen. Ausgehend von diesen Werten kann man `resources.requests` und
`resources.limits` in `dev.yaml` ggf. anpassen. Voraussetzung für `kubectl top`
ist, dass der `metrics-server` für Kubernetes im Namespace `kube-system`
installiert wurde.

---

### Validierung der Installation

#### Polaris

Ob _Best Practices_ bei der Installation eingehalten wurden, kann man mit
_Polaris_ überprüfen. Um den Aufruf zu vereinfachen, gibt es im Unterverzeichnis
`extras\kubernetes` das Skript `polaris.ps1`:

```powershell
    cd extras\kubernetes
    .\polaris.ps1
```

Nun kann Polaris in einem Webbrowser mit der URL `http://localhost:8008`
aufgerufen werden.

#### kube-score

Ob _Best Practices_ bei den _Manifest-Dateien_ eingehalten wurden, kann man mit
_kube-score_ überprüfen. Um den Aufruf zu vereinfachen, gibt es im
Unterverzeichnis `extras\kubernetes` das Skript `kube-score.ps1`:

```powershell
    cd extras\kubernetes
    .\kube-score.ps1
```

#### Kubevious

Ob _Best Practices_ bei der Installation eingehalten wurden, kann man mit
_Kubevious_ überprüfen. Um den Aufruf zu vereinfachen, gibt es im Unterverzeichnis
`extras\kubernetes` das Skript `kubevious.ps1`. Dazu muss Kubevious zuerst mit
einem _Helm Chart_ installiert werden.

```PowerShell
    cd extras\kubernetes
    .\kubevious.ps1 install
    .\kubevious.ps1
```

Nun kann Kubevious in einem Webbrowser mit der URL `http://localhost:7077`
aufgerufen werden, da im Skript auch Port-Forwarding für den Kubevious-Pod
eingerichtet wurde. Abschließend sollte man Kubevious wieder mit deinstallieren:
`.\kubevious uninstall`.

### Administration des Kubernetes Clusters

#### Services Tool Window von Intellij IDEA

Über den Menüpunkt _View_ mit den Unterpunkten _Tool Windows_ und _Services_
kann man das _Services Tool Window_ öffnen. Dort sieht man den Eintrag für
_docker-desktop_ und kann über das Icon _Namespace_ am linken Rand vom
Default-Namespace auf den Namespace "acme" umschalten. Danach kann man über
die Unterpunkte _Workloads_ und _Pods_ zu einem laufenden Pod, z.B.
`kunde-1234567890-12345`, navigieren und diesen mit der linken Maustaste
selektieren. Nun kann man z.B. über die Icons _Download Log_ oder _Run Shell_
die Log-Einträge inspizieren oder eine Shell öffnen. Alternativ kann man beim
Pod auch das Kontextmenü bzw. die rechte Maustaste benutzen.

#### Octant

_Octant_ ist von VMware und vor allem für Entwickler/inn/en geeignet. Octant muss
von einer PowerShell aus gestartet werden, in dem man im Verzeichnis
`C:\Zimmermann\octant` das Kommando `.\octant.exe` aufruft.

Rechts oben kann man den Namespace auswählen, z.B. `acme` statt `default`.
Unter _Namespace Overview_ hat man eine Übersicht über z.B. _Pods_,
_Deployments_, _Stateful Sets_, _Services_, _Config Maps_ und _Secrets_.

In der Navigationsleiste am linken Rand gibt es Menüpunkte für

- _Workloads_: Pods, Deployments, Stateful Sets
- _Discovery and Loadbalancing_: Services
- _Config and Storage_: Config Maps und Secrets

Bei einem ausgewählten Pod hat man direkten Zugriff auf

- die Logging-Ausgaben in der Konsole
- ein Terminal mit einer Shell, falls das zugrundeliegende Docker-Image
  die `/bin/sh` enthält, wie z.B. beim Image für das DB-System.

#### Lens

Lens von Mirantis https://k8slens.dev bietet eine grafische Oberfläche wie
[Octant](#Octant), um die Log-Ausgaben zu inspizieren oder eine Shell zu
benutzen. Darüberhinaus gibt es Monitoring-Möglichkeiten für z.B. die CPU.

In der Navigationsleiste am linken Rand gibt es Menüpunkte für

- _Workloads_: Pods, Deployments, Stateful Sets
- _Configuration_: ConfigMaps und Secrets
- _Network_: Services

Bei z.B. den Pods kann man rechts oben einen bestimmten Namespace auswählen,
damit man eine bessere Übersicht hat. Nun kann man über das Overflow-Menü am
rechten Rand direkt zugreifen auf

- die Logging-Ausgaben in der Konsole
- ein Terminal mit einer Linux-Shell, falls das zugrundeliegende Docker-Image
  eine Shell enthält. Dabei werden folgende Shells in dieser Reihenfolge
  unterstützt:
  - bash
  - ash
  - sh

#### Alternative Werkzeuge

Statt _Lens_ oder _Octant_ kann man auch andere Werkzeuge verwenden:

_Kubernetes Dashboard_ <https://github.com/kubernetes/dashboard> ist vor allem
für IT-Administratoren geeignet und gedacht. Für Entwickler/inn/en ist m.E.
[Lens](#Lens) oder [Octant](#Octant) besser geeignet.

_Kui_ von IBM ist eine interessante Kombination von grafischer Benutzungsoberfläche
und _anklickbarer_ Kommandozeile in Verbindung mit der Möglichkeit, mehrere
Tabs zu verwenden.

_k9s_ ist ein CLI (= Command Line Interface), mit dem effizient auf Kubernetes
zugegriffen werden kann und das bei denjenigen Entwickler*innen sehr beliebt
ist, die ständig mit Kubernetes arbeiten

Für _VS Code_ (statt _IntelliJ IDEA_) gibt es die Extension _Kubernetes_ von
Microsoft. Diese Extension ist ähnlich wie _Octant_ auf die Bedürfnisse der
Entwickler/inn/en zugeschnitten und ermöglicht den einfachen Zugriff auf ein
Terminal oder die Logs.

---

## Statische Codeanalyse

### ktlint und Detekt

Eine statische Codeanalyse ist durch die beiden Werkzeuge _ktlint_ und _Detekt_
möglich, indem man die folgenden Gradle-Tasks aufruft:

```powershell
    .\gradlew ktlint detektMain
```

### SonarQube

Für eine statische Codeanalyse durch _SonarQube_ muss zunächst der
SonarQube-Server mit _Docker Compose_ als Docker-Container gestartet werden:

```powershell
    cd extras\sonarqube
    docker compose up
```

Wenn der Server zum ersten Mal gestartet wird, ruft man in einem Webbrowser die
URL `http://localhost:9000` auf. In der Startseite muss man sich einloggen und
verwendet dazu als Loginname `admin` und ebenso als Password `admin`. Danach
wird man weitergeleitet, um das initiale Passwort zu ändern. Das neue Passwort
trägt man dann in das Skript `sonar-scanner.ps1` im Wurzelverzeichnis ein.
Zur Konfiguration für künftige Aufrufe des _SonarQube-Scanners_ trägt man jetzt
noch in der Konfigurationsdatei `sonar-project.properties` den Projektnamen beim
der Property `sonar.projectKey` ein.

Nachdem der Server gestartet ist, wird der SonarQube-Scanner in einer zweiten
PowerShell mit dem Skript `sonar-scanner.ps1` gestartet. Das Resultat kann dann
in der Webseite des zuvor gestarteten Servers über die URL `http://localhost:9000`
inspiziert werden.

Abschließend wird der oben gestartete Server heruntergefahren.

```powershell
    cd extras\sonarqube
    docker compose down
```

---

## Dokumentation

### Dokumentation durch AsciiDoctor und PlantUML

Eine HTML- und PDF-Dokumentation aus AsciiDoctor-Dateien, die ggf. UML-Diagramme
mit PlantUML enthalten, wird durch folgende Gradle-Tasks erstellt:

```powershell
    .\gradlew asciidoctor asciidoctorPdf
```

### API Dokumentation durch Dokka

Eine API-Dokumentation in Form von HTML-Seiten kann man durch das Gradle-Plugin
Dokka_ erstellen, und zwar wahlweise mit einem Dokka-eigenen Layout oder dem
Layout von javadoc:

```powershell
    .\gradlew dokkaHtml
    .\gradlew dokkaJavadoc
```

---

## Monitoring mit Grafana und Prometheus

_Prometheus_ ist ein Werkzeug für Monitoring, das bei CNCF (Cloud Native Computing
Foundation) entwickelt wird. Damit kann man dann die _Auslastung_ der einzelnen
Ressourcen und ggf. die _Überlastung_ des gesamten Systems beobachten. Typische Daten
für das Monitoring sind:

- CPU-Auslastung
- Auslastung von RAM und Festplatte
- Anzahl Requests
- Verarbeitungszeit für Requests
- Anzahl Exceptions

Um solche Daten ermitteln zu können, müssen die Clients von Prometheus, d.h. die
zu überwachenden Systeme bzw. Microservices, geeignet instrumentiert werden. Dazu
ist z.B. _Micrometer_ geeignet, das mit dem _Spring Framework_ integriert ist. Mit
_Spring Boot Actuator_ kann man dann auf solche Rohdaten zugreifen:

- https://localhost:8080/actuator/metrics
- https://localhost:8080/actuator/metrics/jvm.memory.used
- https://localhost:8080/actuator/prometheus

Wenn nun die Daten in Prometheus gesammelt, aggregiert und aufbereitet sind, dann
können sie mit _Grafana_ visualisiert werden.

Um die beiden Server für Grafana und Prometheus zu starten, kann man deren Docker-Images
verwenden. Damit die Notebooks nicht überlastet werden, sind Grafana und Prometheus
**NICHT** für Kubernetes konfiguriert, sondern für Spring-basierte Server, die direkt
mit Windows laufen.

```powershell
    cd extras\grafana-prometheus
    docker compose up
    ...
    docker compose down
```

Welche Systeme durch _Prometheus_ überwacht werden, kann man in einem Webbrowser
mit der URL `http://localhost:9090/targets` anzeigen.

Die Visualisierung der Monitoring-Daten in _Grafana_ erfolgt über die URL
`http://localhost:3001`. Dort kann man sich mit dem Benutzernamen `admin` und
dem Passwort `admin` einloggen. Beim ersten Aufruf klickt man den Link
_Add your first data source_ an und wählt bei _Time series databases_ den Eintrag
_Prometheus_ aus. Abschließend gibt man bei _URL_ dem Wert `http://host.docker.internal:9090`
ein und klickt auf den Button _Save & test_.

Nun kann man in der linken Menüleiste den Menüpunkt für _Dashboards_ und den
Unterpunkt _+ Import_ auswählen. Im folgenden Dialog klickt man auf den Button
_Upload JSON file_, wählt die Datei `jvm-micrometer_rev1.json` aus dem
Unterverzeichnis `extras\grafana-prometheus` aus und selektiert beim Label
_Prometheus_ die Option _Prometheus (default)_. Mit dem Button _Import_ wird
schließlich das jetzt verfügbare Dashboard _JVM (Micrometer)_ importiert.

Wenn man künftig in der linken Menüleiste den Eintrag für _Dashboards_ anklickt,
kann man das Dashboard _JVM (Micrometer)_ auswählen. Damit in Grafana auch Daten
angezeigt werden können, kann man z.B. mit dem HTTP-Client von IntelliJ GET-Requests
absetzen.

Auf der Webseite `https://rigorousthemes.com/blog/best-grafana-dashboard-examples`
kann man einen Eindruck erhalten, welche Visualisierungen mit Grafana möglich sind.

---

## Einfache Lasttests mit Fortio

Alternative und populäre Werkzeuge für Lasttests sind:

- _Locust_ von https://github.com/locustio/locust: SKripte werden in Python implementiert
- _k6_ aus dem Projekt Grafana https://github.com/grafana/k6: Skripte werden in JavaScript implementiert

_Fortio_ als Werkzeug für Lasttests ist als eigenständiges Projekt aus _Istio_
hervorgegangen. Zunächst startet man den Fortio-Server:

```powershell
    cd extras\kubernetes
    .\fortio.ps1
```

Nachdem der Fortio-Server gestartet ist, ruft man in einem Webbrowser die URL
`http://localhost:8088/fortio` auf. Dort kann man einen einfachen Lasttest
konfigurieren, indem man die Beispielwerte eingibt, die beim Skript `fortio.ps1`
ausgegeben wurden. Abschließend klickt man auf den Button `Start`, um den
einfachen Lasttest zu starten.
