# Third-Party Notices / Drittanbieter-Lizenzen

This project ("DATEV-BWA Controlling") is licensed under the **MIT License** (see `LICENSE`).
It uses third-party open-source components that retain their own licenses. This file documents
those components and fulfils their attribution requirements.

Dieses Projekt steht unter der **MIT-Lizenz** (siehe `LICENSE`). Es verwendet quelloffene
Drittkomponenten, die ihren eigenen Lizenzen unterliegen. Diese Datei dokumentiert sie und erfüllt
deren Namensnennungs-Pflichten.

> The license under which *this* project's own source code is released (MIT) is independent of the
> licenses of the libraries it uses. None of the dependencies below is licensed under GPL or AGPL.
> The copyleft components present (LGPL / EPL / MPL) explicitly permit use by software under any
> license; their only obligations are attribution and replaceability — both satisfied here, since
> every dependency ships as a separate, replaceable JAR/module (no static linking, no modification).

## Übersicht / Summary

| Lizenz / License | Anteil | Distribution mit MIT-Code |
|---|---|---|
| Apache-2.0 | Mehrheit (Spring, Spring AI, Flyway, Tomcat, Thymeleaf, Jackson, Batik, …) | unproblematisch |
| MIT / BSD / 0BSD / EDL / PostgreSQL-License | viele (SLF4J, Mockito*, Testcontainers*, ASM, PostgreSQL-JDBC, Hamcrest*, Angular, …) | unproblematisch |
| **LGPL-2.1 (-or-later)** | Hibernate ORM, Flying Saucer (core+pdf), OpenPDF (dual mit MPL-2.0), JNA (dual mit Apache-2.0) | erlaubt (dynamisch genutzt, ersetzbar) |
| **EPL-1.0 / EPL-2.0** | Logback (dual mit LGPL), AspectJ Weaver, JUnit*, Jakarta Persistence API | erlaubt (datei-basiertes weak copyleft) |
| EPL-2.0 / GPL-2-with-Classpath-Exception | Jakarta Annotation/Transaction API | erlaubt (Classpath-Exception ist für genau diesen Fall gedacht) |

`*` = nur Test-Scope (`org.junit*`, `org.mockito*`, `org.testcontainers*`, `org.hamcrest*`,
`org.assertj*`, `com.squareup.okhttp3:mockwebserver`) — wird **nicht** mit der Anwendung ausgeliefert.

## Laufzeitrelevante Copyleft-Komponenten (im Auslieferungsumfang)

Folgende mitausgelieferte Bibliotheken stehen unter Copyleft-Lizenzen. Sie werden **unverändert** und
als eigenständige, austauschbare JARs (Spring-Boot-Fat-JAR `BOOT-INF/lib/`) genutzt — damit sind die
LGPL-/EPL-Bedingungen (Lizenznennung + Ersetzbarkeit) erfüllt. Wer die Anwendung weitergibt, behält
diese Datei bei.

- **Hibernate ORM** `org.hibernate.orm:hibernate-core:6.6.13.Final` — LGPL-2.1-or-later — https://hibernate.org/orm
- **Flying Saucer** `org.xhtmlrenderer:flying-saucer-core` / `flying-saucer-pdf:9.13.0` — LGPL-2.1-or-later — http://code.google.com/p/flying-saucer/
- **OpenPDF** `com.github.librepdf:openpdf:2.0.5` — LGPL-2.1 / MPL-2.0 (dual) — https://github.com/LibrePDF/OpenPDF
- **Logback** `ch.qos.logback:logback-classic` / `logback-core:1.5.18` — EPL-1.0 / LGPL-2.1 (dual) — http://logback.qos.ch
- **JNA** `net.java.dev.jna:jna:5.13.0` — Apache-2.0 / LGPL-2.1-or-later (dual; hier unter Apache-2.0 nutzbar) — https://github.com/java-native-access/jna
- **AspectJ Weaver** `org.aspectj:aspectjweaver:1.9.24` — EPL-2.0 — https://www.eclipse.org/aspectj/
- **Jakarta Persistence API** `jakarta.persistence:jakarta.persistence-api:3.1.0` — EPL-2.0 / EDL-1.0 — https://github.com/eclipse-ee4j/jpa-api

Die PDF-Erzeugung (Flying Saucer/OpenPDF) ist das einzige Feature mit einer dedizierten
LGPL-Direktabhängigkeit; sie ist über die HTML→PDF-Schnittstelle bei Bedarf durch eine andere
Implementierung ersetzbar.

## Frontend (npm, Laufzeit-Abhängigkeiten)

| Paket | Lizenz |
|---|---|
| `@angular/*` (core, common, forms, router, material, cdk, animations, compiler, platform-browser) | MIT |
| `angular-oauth2-oidc` | MIT |
| `echarts` | Apache-2.0 |
| `ngx-echarts` | MIT |
| `rxjs` | Apache-2.0 |
| `tslib` | 0BSD |

## Container-Images (Laufzeit-Stack)

| Image | Lizenz |
|---|---|
| `quay.io/keycloak/keycloak` | Apache-2.0 |
| `postgres` | PostgreSQL License (BSD-artig) |
| `caddy` | Apache-2.0 |
| `nginx` (Frontend-Image-Basis) | BSD-2-Clause |
| `ollama/ollama` | MIT |

## Vollständige Backend-Abhängigkeitsliste

Die folgende Liste ist maschinengeneriert und reproduzierbar mit:

```
cd backend && mvn org.codehaus.mojo:license-maven-plugin:2.4.0:add-third-party
# Ergebnis: backend/target/generated-sources/license/THIRD-PARTY.txt
```

```
Lists of 204 third-party dependencies.
     (Eclipse Public License - v 1.0) (GNU Lesser General Public License) Logback Classic Module (ch.qos.logback:logback-classic:1.5.18 - http://logback.qos.ch/logback-classic)
     (Eclipse Public License - v 1.0) (GNU Lesser General Public License) Logback Core Module (ch.qos.logback:logback-core:1.5.18 - http://logback.qos.ch/logback-core)
     (Apache License, Version 2.0) ClassMate (com.fasterxml:classmate:1.7.0 - https://github.com/FasterXML/java-classmate)
     (The Apache Software License, Version 2.0) Jackson-annotations (com.fasterxml.jackson.core:jackson-annotations:2.18.3 - https://github.com/FasterXML/jackson)
     (The Apache Software License, Version 2.0) Jackson-core (com.fasterxml.jackson.core:jackson-core:2.18.3 - https://github.com/FasterXML/jackson-core)
     (The Apache Software License, Version 2.0) jackson-databind (com.fasterxml.jackson.core:jackson-databind:2.18.3 - https://github.com/FasterXML/jackson)
     (The Apache Software License, Version 2.0) Jackson-dataformat-TOML (com.fasterxml.jackson.dataformat:jackson-dataformat-toml:2.18.3 - https://github.com/FasterXML/jackson-dataformats-text)
     (The Apache Software License, Version 2.0) Jackson datatype: jdk8 (com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.18.3 - https://github.com/FasterXML/jackson-modules-java8/jackson-datatype-jdk8)
     (The Apache Software License, Version 2.0) Jackson datatype: JSR310 (com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.3 - https://github.com/FasterXML/jackson-modules-java8/jackson-datatype-jsr310)
     (The Apache Software License, Version 2.0) jackson-module-jsonSchema (com.fasterxml.jackson.module:jackson-module-jsonSchema:2.18.3 - https://github.com/FasterXML/jackson-module-jsonSchema)
     (The Apache Software License, Version 2.0) Jackson-module-parameter-names (com.fasterxml.jackson.module:jackson-module-parameter-names:2.18.3 - https://github.com/FasterXML/jackson-modules-java8/jackson-module-parameter-names)
     (The Apache Software License, Version 2.0) docker-java-api (com.github.docker-java:docker-java-api:3.4.1 - https://github.com/docker-java/docker-java)
     (The Apache Software License, Version 2.0) docker-java-transport (com.github.docker-java:docker-java-transport:3.4.1 - https://github.com/docker-java/docker-java)
     (The Apache Software License, Version 2.0) docker-java-transport-zerodep (com.github.docker-java:docker-java-transport-zerodep:3.4.1 - https://github.com/docker-java/docker-java)
     (GNU Lesser General Public License (LGPL), Version 2.1) (Mozilla Public License Version 2.0) openpdf (com.github.librepdf:openpdf:2.0.5 - https://github.com/LibrePDF/OpenPDF/openpdf)
     (Apache License, Version 2.0) JCIP Annotations under Apache License (com.github.stephenc.jcip:jcip-annotations:1.0-1 - http://stephenc.github.com/jcip-annotations)
     (The Apache License, Version 2.0) Java JSON Schema Generator (com.github.victools:jsonschema-generator:4.37.0 - https://github.com/victools/jsonschema-generator)
     (The Apache License, Version 2.0) Java JSON Schema Generator Module – jackson (com.github.victools:jsonschema-module-jackson:4.37.0 - https://github.com/victools/jsonschema-generator)
     (The Apache License, Version 2.0) Java JSON Schema Generator Module – swagger (2.x) (com.github.victools:jsonschema-module-swagger-2:4.37.0 - https://github.com/victools/jsonschema-generator/jsonschema-module-swagger-2)
     (The Apache Software License, Version 2.0) json-path (com.jayway.jsonpath:json-path:2.9.0 - https://github.com/jayway/JsonPath)
     (MIT License) JTokkit (com.knuddels:jtokkit:1.1.0 - https://github.com/knuddelsgmbh/jtokkit)
     (The Apache Software License, Version 2.0) Nimbus JOSE+JWT (com.nimbusds:nimbus-jose-jwt:9.37.3 - https://bitbucket.org/connect2id/nimbus-jose-jwt)
     (The Apache Software License, Version 2.0) mockwebserver (com.squareup.okhttp3:mockwebserver:4.12.0 - https://square.github.io/okhttp/)
     (The Apache Software License, Version 2.0) okhttp (com.squareup.okhttp3:okhttp:4.12.0 - https://square.github.io/okhttp/)
     (The Apache Software License, Version 2.0) okio (com.squareup.okio:okio:3.6.0 - https://github.com/square/okio/)
     (The Apache Software License, Version 2.0) okio (com.squareup.okio:okio-jvm:3.6.0 - https://github.com/square/okio/)
     (Eclipse Distribution License - v 1.0) istack common utility code runtime (com.sun.istack:istack-commons-runtime:4.1.2 - https://projects.eclipse.org/projects/ee4j/istack-commons/istack-commons-runtime)
     (Apache License 2.0) JSON library from Android SDK (com.vaadin.external.google:android-json:0.0.20131108.vaadin1 - http://developer.android.com/sdk)
     (The Apache Software License, Version 2.0) HikariCP (com.zaxxer:HikariCP:5.1.0 - https://github.com/brettwooldridge/HikariCP)
     (Apache-2.0) Apache Commons Codec (commons-codec:commons-codec:1.17.2 - https://commons.apache.org/proper/commons-codec/)
     (Apache-2.0) Apache Commons IO (commons-io:commons-io:2.20.0 - https://commons.apache.org/proper/commons-io/)
     (Apache-2.0) Apache Commons Logging (commons-logging:commons-logging:1.3.0 - https://commons.apache.org/proper/commons-logging/)
     (The Apache Software License, Version 2.0) context-propagation (io.micrometer:context-propagation:1.1.3 - https://github.com/micrometer-metrics/context-propagation)
     (The Apache Software License, Version 2.0) micrometer-commons (io.micrometer:micrometer-commons:1.14.6 - https://github.com/micrometer-metrics/micrometer)
     (The Apache Software License, Version 2.0) micrometer-core (io.micrometer:micrometer-core:1.14.6 - https://github.com/micrometer-metrics/micrometer)
     (The Apache Software License, Version 2.0) micrometer-jakarta9 (io.micrometer:micrometer-jakarta9:1.14.6 - https://github.com/micrometer-metrics/micrometer)
     (The Apache Software License, Version 2.0) micrometer-observation (io.micrometer:micrometer-observation:1.14.6 - https://github.com/micrometer-metrics/micrometer)
     (Apache License, Version 2.0) Non-Blocking Reactive Foundation for the JVM (io.projectreactor:reactor-core:3.7.5 - https://github.com/reactor/reactor-core)
     (Apache License, Version 2.0) Jandex: Core (io.smallrye:jandex:3.2.0 - https://smallrye.io)
     (Apache License 2.0) swagger-annotations (io.swagger.core.v3:swagger-annotations:2.2.25 - https://github.com/swagger-api/swagger-core/modules/swagger-annotations)
     (EDL 1.0) Jakarta Activation API (jakarta.activation:jakarta.activation-api:2.1.3 - https://github.com/jakartaee/jaf-api)
     (EPL 2.0) (GPL2 w/ CPE) Jakarta Annotations API (jakarta.annotation:jakarta.annotation-api:2.1.1 - https://projects.eclipse.org/projects/ee4j.ca)
     (The Apache Software License, Version 2.0) Jakarta Dependency Injection (jakarta.inject:jakarta.inject-api:2.0.1 - https://github.com/eclipse-ee4j/injection-api)
     (Eclipse Distribution License v. 1.0) (Eclipse Public License v. 2.0) Jakarta Persistence API (jakarta.persistence:jakarta.persistence-api:3.1.0 - https://github.com/eclipse-ee4j/jpa-api)
     (EPL 2.0) (GPL2 w/ CPE) jakarta.transaction API (jakarta.transaction:jakarta.transaction-api:2.0.1 - https://projects.eclipse.org/projects/ee4j.jta)
     (Apache License 2.0) Jakarta Bean Validation API (jakarta.validation:jakarta.validation-api:3.0.2 - https://beanvalidation.org)
     (Eclipse Distribution License - v 1.0) Jakarta XML Binding API (jakarta.xml.bind:jakarta.xml.bind-api:4.0.2 - https://github.com/jakartaee/jaxb-api/jakarta.xml.bind-api)
     (The Apache Software License, Version 2.0) Bean Validation API (javax.validation:validation-api:1.1.0.Final - http://beanvalidation.org)
     (Eclipse Public License 1.0) JUnit (junit:junit:4.13.2 - http://junit.org)
     (Apache License, Version 2.0) Byte Buddy (without dependencies) (net.bytebuddy:byte-buddy:1.15.11 - https://bytebuddy.net/byte-buddy)
     (Apache License, Version 2.0) Byte Buddy agent (net.bytebuddy:byte-buddy-agent:1.15.11 - https://bytebuddy.net/byte-buddy-agent)
     (Apache-2.0) (LGPL-2.1-or-later) Java Native Access (net.java.dev.jna:jna:5.13.0 - https://github.com/java-native-access/jna)
     (The Apache Software License, Version 2.0) ASM based accessors helper used by json-smart (net.minidev:accessors-smart:2.5.2 - https://urielch.github.io/)
     (The Apache Software License, Version 2.0) JSON Small and Fast Parser (net.minidev:json-smart:2.5.2 - https://urielch.github.io/)
     (The BSD License) StringTemplate 4 (org.antlr:ST4:4.3.4 - http://nexus.sonatype.org/oss-repository-hosting.html/ST4)
     (BSD licence) ANTLR 3 Runtime (org.antlr:antlr-runtime:3.5.3 - http://www.antlr.org)
     (BSD-3-Clause) ANTLR 4 Runtime (org.antlr:antlr4-runtime:4.13.0 - https://www.antlr.org/antlr4-runtime/)
     (Apache-2.0) Apache Commons Compress (org.apache.commons:commons-compress:1.24.0 - https://commons.apache.org/proper/commons-compress/)
     (Apache-2.0) Apache Commons CSV (org.apache.commons:commons-csv:1.14.1 - https://commons.apache.org/proper/commons-csv/)
     (Apache-2.0) Apache Log4j API (org.apache.logging.log4j:log4j-api:2.24.3 - https://logging.apache.org/log4j/2.x/log4j/log4j-api/)
     (Apache-2.0) Log4j API to SLF4J Adapter (org.apache.logging.log4j:log4j-to-slf4j:2.24.3 - https://logging.apache.org/log4j/2.x/log4j/log4j-to-slf4j/)
     (Apache License, Version 2.0) tomcat-embed-core (org.apache.tomcat.embed:tomcat-embed-core:10.1.40 - https://tomcat.apache.org/)
     (Apache License, Version 2.0) tomcat-embed-el (org.apache.tomcat.embed:tomcat-embed-el:10.1.40 - https://tomcat.apache.org/)
     (Apache License, Version 2.0) tomcat-embed-websocket (org.apache.tomcat.embed:tomcat-embed-websocket:10.1.40 - https://tomcat.apache.org/)
     (The Apache Software License, Version 2.0) org.apache.xmlgraphics:batik-anim (org.apache.xmlgraphics:batik-anim:1.19 - http://xmlgraphics.apache.org/batik/batik-anim/)
     (The Apache Software License, Version 2.0) org.apache.xmlgraphics:batik-awt-util (org.apache.xmlgraphics:batik-awt-util:1.19 - http://xmlgraphics.apache.org/batik/batik-awt-util/)
     (The Apache Software License, Version 2.0) org.apache.xmlgraphics:batik-bridge (org.apache.xmlgraphics:batik-bridge:1.19 - http://xmlgraphics.apache.org/batik/batik-bridge/)
     (The Apache Software License, Version 2.0) org.apache.xmlgraphics:batik-codec (org.apache.xmlgraphics:batik-codec:1.19 - http://xmlgraphics.apache.org/batik/batik-codec/)
     (The Apache Software License, Version 2.0) org.apache.xmlgraphics:batik-constants (org.apache.xmlgraphics:batik-constants:1.19 - http://xmlgraphics.apache.org/batik/batik-constants/)
     (The Apache Software License, Version 2.0) org.apache.xmlgraphics:batik-css (org.apache.xmlgraphics:batik-css:1.19 - http://xmlgraphics.apache.org/batik/batik-css/)
     (The Apache Software License, Version 2.0) org.apache.xmlgraphics:batik-dom (org.apache.xmlgraphics:batik-dom:1.19 - http://xmlgraphics.apache.org/batik/batik-dom/)
     (The Apache Software License, Version 2.0) org.apache.xmlgraphics:batik-ext (org.apache.xmlgraphics:batik-ext:1.19 - http://xmlgraphics.apache.org/batik/batik-ext/)
     (The Apache Software License, Version 2.0) org.apache.xmlgraphics:batik-gvt (org.apache.xmlgraphics:batik-gvt:1.19 - http://xmlgraphics.apache.org/batik/batik-gvt/)
     (The Apache Software License, Version 2.0) org.apache.xmlgraphics:batik-i18n (org.apache.xmlgraphics:batik-i18n:1.19 - http://xmlgraphics.apache.org/batik/batik-i18n/)
     (The Apache Software License, Version 2.0) org.apache.xmlgraphics:batik-parser (org.apache.xmlgraphics:batik-parser:1.19 - http://xmlgraphics.apache.org/batik/batik-parser/)
     (The Apache Software License, Version 2.0) org.apache.xmlgraphics:batik-script (org.apache.xmlgraphics:batik-script:1.19 - http://xmlgraphics.apache.org/batik/batik-script/)
     (The Apache Software License, Version 2.0) org.apache.xmlgraphics:batik-shared-resources (org.apache.xmlgraphics:batik-shared-resources:1.19 - http://xmlgraphics.apache.org/batik/batik-shared-resources/)
     (The Apache Software License, Version 2.0) org.apache.xmlgraphics:batik-svg-dom (org.apache.xmlgraphics:batik-svg-dom:1.19 - http://xmlgraphics.apache.org/batik/batik-svg-dom/)
     (The Apache Software License, Version 2.0) org.apache.xmlgraphics:batik-svggen (org.apache.xmlgraphics:batik-svggen:1.19 - http://xmlgraphics.apache.org/batik/batik-svggen/)
     (The Apache Software License, Version 2.0) org.apache.xmlgraphics:batik-transcoder (org.apache.xmlgraphics:batik-transcoder:1.19 - http://xmlgraphics.apache.org/batik/batik-transcoder/)
     (The Apache Software License, Version 2.0) org.apache.xmlgraphics:batik-util (org.apache.xmlgraphics:batik-util:1.19 - http://xmlgraphics.apache.org/batik/batik-util/)
     (The Apache Software License, Version 2.0) org.apache.xmlgraphics:batik-xml (org.apache.xmlgraphics:batik-xml:1.19 - http://xmlgraphics.apache.org/batik/batik-xml/)
     (The Apache Software License, Version 2.0) Apache XML Graphics Commons (org.apache.xmlgraphics:xmlgraphics-commons:2.11 - http://xmlgraphics.apache.org/commons/)
     (The Apache License, Version 2.0) org.apiguardian:apiguardian-api (org.apiguardian:apiguardian-api:1.1.2 - https://github.com/apiguardian-team/apiguardian)
     (Eclipse Public License - v 2.0) AspectJ Weaver (org.aspectj:aspectjweaver:1.9.24 - https://www.eclipse.org/aspectj/)
     (Apache License, Version 2.0) AssertJ Core (org.assertj:assertj-core:3.26.3 - https://assertj.github.io/doc/#assertj-core)
     (The Apache Software License, Version 2.0) attoparser (org.attoparser:attoparser:2.0.7.RELEASE - https://www.attoparser.org)
     (Apache 2.0) Awaitility (org.awaitility:awaitility:4.2.2 - http://awaitility.org)
     (The MIT License) Checker Qual (org.checkerframework:checker-qual:3.48.3 - https://checkerframework.org/)
     (EDL 1.0) Angus Activation Registries (org.eclipse.angus:angus-activation:2.0.2 - https://github.com/eclipse-ee4j/angus-activation/angus-activation)
     (Apache License, Version 2.0) flyway-core (org.flywaydb:flyway-core:10.20.1 - https://flywaydb.org/flyway-core)
     (Apache License, Version 2.0) flyway-database-postgresql (org.flywaydb:flyway-database-postgresql:10.20.1 - https://flywaydb.org/flyway-database-postgresql)
     (Eclipse Distribution License - v 1.0) JAXB Core (org.glassfish.jaxb:jaxb-core:4.0.5 - https://eclipse-ee4j.github.io/jaxb-ri/)
     (Eclipse Distribution License - v 1.0) JAXB Runtime (org.glassfish.jaxb:jaxb-runtime:4.0.5 - https://eclipse-ee4j.github.io/jaxb-ri/)
     (Eclipse Distribution License - v 1.0) TXW2 Runtime (org.glassfish.jaxb:txw2:4.0.5 - https://eclipse-ee4j.github.io/jaxb-ri/)
     (BSD License 3) Hamcrest (org.hamcrest:hamcrest:2.2 - http://hamcrest.org/JavaHamcrest/)
     (BSD License 3) Hamcrest Core (org.hamcrest:hamcrest-core:2.2 - http://hamcrest.org/JavaHamcrest/)
     (BSD-2-Clause) (Public Domain, per Creative Commons CC0) HdrHistogram (org.hdrhistogram:HdrHistogram:2.2.2 - http://hdrhistogram.github.io/HdrHistogram/)
     (Apache License Version 2.0) Hibernate Commons Annotations (org.hibernate.common:hibernate-commons-annotations:7.0.3.Final - http://hibernate.org)
     (GNU Library General Public License v2.1 or later) Hibernate ORM - hibernate-core (org.hibernate.orm:hibernate-core:6.6.13.Final - https://hibernate.org/orm)
     (Apache License 2.0) Hibernate Validator Engine (org.hibernate.validator:hibernate-validator:8.0.2.Final - http://hibernate.org/validator/hibernate-validator)
     (Apache License 2.0) JBoss Logging 3 (org.jboss.logging:jboss-logging:3.6.1.Final - http://www.jboss.org)
     (The Apache Software License, Version 2.0) IntelliJ IDEA Annotations (org.jetbrains:annotations:13.0 - http://www.jetbrains.org)
     (The Apache License, Version 2.0) Kotlin Stdlib (org.jetbrains.kotlin:kotlin-stdlib:1.9.25 - https://kotlinlang.org/)
     (The Apache License, Version 2.0) Kotlin Stdlib Common (org.jetbrains.kotlin:kotlin-stdlib-common:1.9.25 - https://kotlinlang.org/)
     (The Apache License, Version 2.0) Kotlin Stdlib Jdk7 (org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.9.25 - https://kotlinlang.org/)
     (The Apache License, Version 2.0) Kotlin Stdlib Jdk8 (org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.25 - https://kotlinlang.org/)
     (Eclipse Public License v2.0) JUnit Jupiter (Aggregator) (org.junit.jupiter:junit-jupiter:5.11.4 - https://junit.org/junit5/)
     (Eclipse Public License v2.0) JUnit Jupiter API (org.junit.jupiter:junit-jupiter-api:5.11.4 - https://junit.org/junit5/)
     (Eclipse Public License v2.0) JUnit Jupiter Engine (org.junit.jupiter:junit-jupiter-engine:5.11.4 - https://junit.org/junit5/)
     (Eclipse Public License v2.0) JUnit Jupiter Params (org.junit.jupiter:junit-jupiter-params:5.11.4 - https://junit.org/junit5/)
     (Eclipse Public License v2.0) JUnit Platform Commons (org.junit.platform:junit-platform-commons:1.11.4 - https://junit.org/junit5/)
     (Eclipse Public License v2.0) JUnit Platform Engine API (org.junit.platform:junit-platform-engine:1.11.4 - https://junit.org/junit5/)
     (Public Domain, per Creative Commons CC0) LatencyUtils (org.latencyutils:LatencyUtils:2.0.3 - http://latencyutils.github.io/LatencyUtils/)
     (MIT) mockito-core (org.mockito:mockito-core:5.14.2 - https://github.com/mockito/mockito)
     (MIT) mockito-junit-jupiter (org.mockito:mockito-junit-jupiter:5.14.2 - https://github.com/mockito/mockito)
     (Apache License, Version 2.0) Objenesis (org.objenesis:objenesis:3.3 - http://objenesis.org/objenesis)
     (The Apache License, Version 2.0) org.opentest4j:opentest4j (org.opentest4j:opentest4j:1.3.0 - https://github.com/ota4j-team/opentest4j)
     (BSD-3-Clause) asm (org.ow2.asm:asm:9.7.1 - http://asm.ow2.io/)
     (BSD-2-Clause) PostgreSQL JDBC Driver (org.postgresql:postgresql:42.7.5 - https://jdbc.postgresql.org)
     (MIT-0) reactive-streams (org.reactivestreams:reactive-streams:1.0.4 - http://www.reactive-streams.org/)
     (MIT) Duct Tape (org.rnorth.duct-tape:duct-tape:1.0.8 - https://github.com/rnorth/duct-tape)
     (The Apache Software License, Version 2.0) JSONassert (org.skyscreamer:jsonassert:1.5.3 - https://github.com/skyscreamer/JSONassert)
     (MIT) JUL to SLF4J bridge (org.slf4j:jul-to-slf4j:2.0.17 - http://www.slf4j.org)
     (MIT) SLF4J API Module (org.slf4j:slf4j-api:2.0.17 - http://www.slf4j.org)
     (Apache License, Version 2.0) Spring AOP (org.springframework:spring-aop:6.2.6 - https://github.com/spring-projects/spring-framework)
     (Apache License, Version 2.0) Spring Aspects (org.springframework:spring-aspects:6.2.6 - https://github.com/spring-projects/spring-framework)
     (Apache License, Version 2.0) Spring Beans (org.springframework:spring-beans:6.2.6 - https://github.com/spring-projects/spring-framework)
     (Apache License, Version 2.0) Spring Context (org.springframework:spring-context:6.2.6 - https://github.com/spring-projects/spring-framework)
     (Apache License, Version 2.0) Spring Context Support (org.springframework:spring-context-support:6.2.6 - https://github.com/spring-projects/spring-framework)
     (Apache License, Version 2.0) Spring Core (org.springframework:spring-core:6.2.6 - https://github.com/spring-projects/spring-framework)
     (Apache License, Version 2.0) Spring Expression Language (SpEL) (org.springframework:spring-expression:6.2.6 - https://github.com/spring-projects/spring-framework)
     (Apache License, Version 2.0) Spring Commons Logging Bridge (org.springframework:spring-jcl:6.2.6 - https://github.com/spring-projects/spring-framework)
     (Apache License, Version 2.0) Spring JDBC (org.springframework:spring-jdbc:6.2.6 - https://github.com/spring-projects/spring-framework)
     (Apache License, Version 2.0) Spring Messaging (org.springframework:spring-messaging:6.2.6 - https://github.com/spring-projects/spring-framework)
     (Apache License, Version 2.0) Spring Object/Relational Mapping (org.springframework:spring-orm:6.2.6 - https://github.com/spring-projects/spring-framework)
     (Apache License, Version 2.0) Spring TestContext Framework (org.springframework:spring-test:6.2.6 - https://github.com/spring-projects/spring-framework)
     (Apache License, Version 2.0) Spring Transaction (org.springframework:spring-tx:6.2.6 - https://github.com/spring-projects/spring-framework)
     (Apache License, Version 2.0) Spring Web (org.springframework:spring-web:6.2.6 - https://github.com/spring-projects/spring-framework)
     (Apache License, Version 2.0) Spring WebFlux (org.springframework:spring-webflux:6.2.6 - https://github.com/spring-projects/spring-framework)
     (Apache License, Version 2.0) Spring Web MVC (org.springframework:spring-webmvc:6.2.6 - https://github.com/spring-projects/spring-framework)
     (Apache 2.0) Spring AI Chat Client Auto Configuration (org.springframework.ai:spring-ai-autoconfigure-model-chat-client:1.0.0 - https://github.com/spring-projects/spring-ai)
     (Apache 2.0) Spring AI Chat Memory Auto Configuration (org.springframework.ai:spring-ai-autoconfigure-model-chat-memory:1.0.0 - https://github.com/spring-projects/spring-ai)
     (Apache 2.0) Spring AI Chat Observation Auto Configuration (org.springframework.ai:spring-ai-autoconfigure-model-chat-observation:1.0.0 - https://github.com/spring-projects/spring-ai)
     (Apache 2.0) Spring AI Embedding Observation Auto Configuration (org.springframework.ai:spring-ai-autoconfigure-model-embedding-observation:1.0.0 - https://github.com/spring-projects/spring-ai)
     (Apache 2.0) Spring AI Image Observation Auto Configuration (org.springframework.ai:spring-ai-autoconfigure-model-image-observation:1.0.0 - https://github.com/spring-projects/spring-ai)
     (Apache 2.0) Spring AI Ollama Auto Configuration (org.springframework.ai:spring-ai-autoconfigure-model-ollama:1.0.0 - https://github.com/spring-projects/spring-ai)
     (Apache 2.0) Spring AI OpenAI Auto Configuration (org.springframework.ai:spring-ai-autoconfigure-model-openai:1.0.0 - https://github.com/spring-projects/spring-ai)
     (Apache 2.0) Spring AI Chat Model Auto Configuration (org.springframework.ai:spring-ai-autoconfigure-model-tool:1.0.0 - https://github.com/spring-projects/spring-ai)
     (Apache 2.0) Spring AI Retry Auto Configuration (org.springframework.ai:spring-ai-autoconfigure-retry:1.0.0 - https://github.com/spring-projects/spring-ai)
     (Apache 2.0) Spring AI Chat Client (org.springframework.ai:spring-ai-client-chat:1.0.0 - https://github.com/spring-projects/spring-ai)
     (Apache 2.0) Spring AI Commons (org.springframework.ai:spring-ai-commons:1.0.0 - https://github.com/spring-projects/spring-ai)
     (Apache 2.0) Spring AI Model (org.springframework.ai:spring-ai-model:1.0.0 - https://github.com/spring-projects/spring-ai)
     (Apache 2.0) Spring AI Model - Ollama (org.springframework.ai:spring-ai-ollama:1.0.0 - https://github.com/spring-projects/spring-ai)
     (Apache 2.0) Spring AI Model - OpenAI (org.springframework.ai:spring-ai-openai:1.0.0 - https://github.com/spring-projects/spring-ai)
     (Apache 2.0) Spring AI Retry (org.springframework.ai:spring-ai-retry:1.0.0 - https://github.com/spring-projects/spring-ai)
     (Apache 2.0) Spring AI Starter - Ollama (org.springframework.ai:spring-ai-starter-model-ollama:1.0.0 - https://github.com/spring-projects/spring-ai)
     (Apache 2.0) Spring AI Starter - OpenAI (org.springframework.ai:spring-ai-starter-model-openai:1.0.0 - https://github.com/spring-projects/spring-ai)
     (Apache 2.0) Spring AI Template StringTemplate (org.springframework.ai:spring-ai-template-st:1.0.0 - https://github.com/spring-projects/spring-ai)
     (Apache License, Version 2.0) spring-boot (org.springframework.boot:spring-boot:3.4.5 - https://spring.io/projects/spring-boot)
     (Apache License, Version 2.0) spring-boot-actuator (org.springframework.boot:spring-boot-actuator:3.4.5 - https://spring.io/projects/spring-boot)
     (Apache License, Version 2.0) spring-boot-actuator-autoconfigure (org.springframework.boot:spring-boot-actuator-autoconfigure:3.4.5 - https://spring.io/projects/spring-boot)
     (Apache License, Version 2.0) spring-boot-autoconfigure (org.springframework.boot:spring-boot-autoconfigure:3.4.5 - https://spring.io/projects/spring-boot)
     (Apache License, Version 2.0) spring-boot-starter (org.springframework.boot:spring-boot-starter:3.4.5 - https://spring.io/projects/spring-boot)
     (Apache License, Version 2.0) spring-boot-starter-actuator (org.springframework.boot:spring-boot-starter-actuator:3.4.5 - https://spring.io/projects/spring-boot)
     (Apache License, Version 2.0) spring-boot-starter-data-jpa (org.springframework.boot:spring-boot-starter-data-jpa:3.4.5 - https://spring.io/projects/spring-boot)
     (Apache License, Version 2.0) spring-boot-starter-jdbc (org.springframework.boot:spring-boot-starter-jdbc:3.4.5 - https://spring.io/projects/spring-boot)
     (Apache License, Version 2.0) spring-boot-starter-json (org.springframework.boot:spring-boot-starter-json:3.4.5 - https://spring.io/projects/spring-boot)
     (Apache License, Version 2.0) spring-boot-starter-logging (org.springframework.boot:spring-boot-starter-logging:3.4.5 - https://spring.io/projects/spring-boot)
     (Apache License, Version 2.0) spring-boot-starter-oauth2-resource-server (org.springframework.boot:spring-boot-starter-oauth2-resource-server:3.4.5 - https://spring.io/projects/spring-boot)
     (Apache License, Version 2.0) spring-boot-starter-security (org.springframework.boot:spring-boot-starter-security:3.4.5 - https://spring.io/projects/spring-boot)
     (Apache License, Version 2.0) spring-boot-starter-test (org.springframework.boot:spring-boot-starter-test:3.4.5 - https://spring.io/projects/spring-boot)
     (Apache License, Version 2.0) spring-boot-starter-thymeleaf (org.springframework.boot:spring-boot-starter-thymeleaf:3.4.5 - https://spring.io/projects/spring-boot)
     (Apache License, Version 2.0) spring-boot-starter-tomcat (org.springframework.boot:spring-boot-starter-tomcat:3.4.5 - https://spring.io/projects/spring-boot)
     (Apache License, Version 2.0) spring-boot-starter-validation (org.springframework.boot:spring-boot-starter-validation:3.4.5 - https://spring.io/projects/spring-boot)
     (Apache License, Version 2.0) spring-boot-starter-web (org.springframework.boot:spring-boot-starter-web:3.4.5 - https://spring.io/projects/spring-boot)
     (Apache License, Version 2.0) spring-boot-test (org.springframework.boot:spring-boot-test:3.4.5 - https://spring.io/projects/spring-boot)
     (Apache License, Version 2.0) spring-boot-test-autoconfigure (org.springframework.boot:spring-boot-test-autoconfigure:3.4.5 - https://spring.io/projects/spring-boot)
     (Apache License, Version 2.0) spring-boot-testcontainers (org.springframework.boot:spring-boot-testcontainers:3.4.5 - https://spring.io/projects/spring-boot)
     (Apache License, Version 2.0) Spring Data Core (org.springframework.data:spring-data-commons:3.4.5 - https://spring.io/projects/spring-data)
     (Apache License, Version 2.0) Spring Data JPA (org.springframework.data:spring-data-jpa:3.4.5 - https://projects.spring.io/spring-data-jpa)
     (Apache 2.0) Spring Retry (org.springframework.retry:spring-retry:2.0.11 - https://github.com/spring-projects/spring-retry)
     (Apache License, Version 2.0) spring-security-config (org.springframework.security:spring-security-config:6.4.5 - https://spring.io/projects/spring-security)
     (Apache License, Version 2.0) spring-security-core (org.springframework.security:spring-security-core:6.4.5 - https://spring.io/projects/spring-security)
     (Apache License, Version 2.0) spring-security-crypto (org.springframework.security:spring-security-crypto:6.4.5 - https://spring.io/projects/spring-security)
     (Apache License, Version 2.0) spring-security-oauth2-core (org.springframework.security:spring-security-oauth2-core:6.4.5 - https://spring.io/projects/spring-security)
     (Apache License, Version 2.0) spring-security-oauth2-jose (org.springframework.security:spring-security-oauth2-jose:6.4.5 - https://spring.io/projects/spring-security)
     (Apache License, Version 2.0) spring-security-oauth2-resource-server (org.springframework.security:spring-security-oauth2-resource-server:6.4.5 - https://spring.io/projects/spring-security)
     (Apache License, Version 2.0) spring-security-test (org.springframework.security:spring-security-test:6.4.5 - https://spring.io/projects/spring-security)
     (Apache License, Version 2.0) spring-security-web (org.springframework.security:spring-security-web:6.4.5 - https://spring.io/projects/spring-security)
     (MIT) Testcontainers :: Database-Commons (org.testcontainers:database-commons:1.20.6 - https://java.testcontainers.org)
     (MIT) Testcontainers :: JDBC (org.testcontainers:jdbc:1.20.6 - https://java.testcontainers.org)
     (MIT) Testcontainers :: JUnit Jupiter Extension (org.testcontainers:junit-jupiter:1.20.6 - https://java.testcontainers.org)
     (MIT) Testcontainers :: JDBC :: PostgreSQL (org.testcontainers:postgresql:1.20.6 - https://java.testcontainers.org)
     (MIT) Testcontainers Core (org.testcontainers:testcontainers:1.20.6 - https://java.testcontainers.org)
     (The Apache Software License, Version 2.0) thymeleaf (org.thymeleaf:thymeleaf:3.1.3.RELEASE - http://www.thymeleaf.org/thymeleaf-lib/thymeleaf)
     (The Apache Software License, Version 2.0) thymeleaf-spring6 (org.thymeleaf:thymeleaf-spring6:3.1.3.RELEASE - http://www.thymeleaf.org/thymeleaf-lib/thymeleaf-spring6)
     (The Apache Software License, Version 2.0) unbescape (org.unbescape:unbescape:1.1.6.RELEASE - http://www.unbescape.org)
     (GNU Lesser General Public License (LGPL), version 2.1 or later) Flying Saucer Core Renderer (org.xhtmlrenderer:flying-saucer-core:9.13.0 - http://code.google.com/p/flying-saucer/flying-saucer-core/)
     (GNU Lesser General Public License (LGPL), version 2.1 or later) Flying Saucer PDF Rendering (org.xhtmlrenderer:flying-saucer-pdf:9.13.0 - http://code.google.com/p/flying-saucer/flying-saucer-pdf/)
     (The Apache Software License, Version 2.0) org.xmlunit:xmlunit-core (org.xmlunit:xmlunit-core:2.10.0 - https://www.xmlunit.org/)
     (Apache License, Version 2.0) SnakeYAML (org.yaml:snakeyaml:2.3 - https://bitbucket.org/snakeyaml/snakeyaml)
     (The Apache Software License, Version 2.0) (The SAX License) (The W3C License) XML Commons External Components XML APIs (xml-apis:xml-apis:1.4.01 - http://xml.apache.org/commons/components/external/)
     (The Apache Software License, Version 2.0) XML Commons External Components XML APIs Extensions (xml-apis:xml-apis-ext:1.3.04 - http://xml.apache.org/commons/components/external/)
```
