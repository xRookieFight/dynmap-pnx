# syntax=docker/dockerfile:1.7

ARG JAVA_VERSION=21

FROM eclipse-temurin:${JAVA_VERSION}-jdk AS build

RUN apt-get update \
    && apt-get install -y --no-install-recommends git \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /workspace/dynmap-pnx
COPY gradle.properties ./

RUN set -eu; \
    repo="$(sed -n 's/^dynmapRepository=//p' gradle.properties)"; \
    ref="$(sed -n 's/^dynmapRef=//p' gradle.properties)"; \
    git init -q /workspace/dynmap-src; \
    git -C /workspace/dynmap-src remote add origin "$repo"; \
    git -C /workspace/dynmap-src fetch -q --depth 1 origin "$ref"; \
    git -C /workspace/dynmap-src checkout -q FETCH_HEAD

COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
COPY src ./src

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew shadowJar --no-daemon --console=plain \
    && cp build/libs/Dynmap-*-pnx.jar /workspace/Dynmap-pnx.jar


FROM eclipse-temurin:${JAVA_VERSION}-jdk AS server

ARG PNX_VERSION=3.0.0-SNAPSHOT
ARG PNX_REPOSITORY=https://repo.powernukkitx.org/releases

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

RUN set -eu; \
    base="${PNX_REPOSITORY}/org/powernukkitx/server/${PNX_VERSION}"; \
    case "$PNX_VERSION" in \
      *-SNAPSHOT) \
        stamp="$(curl -fsSL "$base/maven-metadata.xml" \
          | tr -d ' \n' \
          | sed -n 's/.*<classifier>all<\/classifier><extension>jar<\/extension><value>\([^<]*\)<\/value>.*/\1/p')"; \
        [ -n "$stamp" ] || { echo "could not resolve PNX snapshot $PNX_VERSION" >&2; exit 1; } ;; \
      *) stamp="$PNX_VERSION" ;; \
    esac; \
    curl -fsSL -o /powernukkitx.jar "$base/server-${stamp}-all.jar"


FROM eclipse-temurin:${JAVA_VERSION}-jre AS runtime

LABEL org.opencontainers.image.title="dynmap-pnx" \
      org.opencontainers.image.description="PowerNukkitX server with Dynmap preinstalled" \
      org.opencontainers.image.source="https://github.com/xRookieFight/dynmap-pnx" \
      org.opencontainers.image.licenses="Apache-2.0"

RUN useradd --user-group --no-create-home --home-dir /data --shell /usr/sbin/nologin minecraft \
    && mkdir -p /app /data \
    && chown -R minecraft:minecraft /app /data

COPY --from=server --chown=minecraft:minecraft /powernukkitx.jar /app/powernukkitx.jar
COPY --from=build --chown=minecraft:minecraft /workspace/Dynmap-pnx.jar /app/plugins/Dynmap-pnx.jar

USER minecraft:minecraft
WORKDIR /data
VOLUME /data

EXPOSE 19132/udp 8123/tcp

ENV JAVA_OPTS="-Xms1G -Xmx2G -XX:+UseZGC -XX:+ZGenerational"

ENTRYPOINT ["/bin/sh", "-c", "mkdir -p /data/plugins && cp -f /app/plugins/Dynmap-pnx.jar /data/plugins/ && exec java $JAVA_OPTS -Dfile.encoding=UTF-8 --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.io=ALL-UNNAMED --add-opens java.base/java.net=ALL-UNNAMED -cp /app/powernukkitx.jar:./libs/* cn.nukkit.Nukkit \"$@\"", "--"]
