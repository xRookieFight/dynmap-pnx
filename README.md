<div align="center">

# Dynmap for PowerNukkitX

**A live, Google Maps style web map of your Bedrock server, powered by [Dynmap](https://github.com/webbukkit/dynmap) and running natively on [PowerNukkitX](https://github.com/PowerNukkitX/PowerNukkitX).**

[![Build](https://github.com/xRookieFight/dynmap-pnx/actions/workflows/build.yml/badge.svg)](https://github.com/xRookieFight/dynmap-pnx/actions/workflows/build.yml)
[![Docker](https://github.com/xRookieFight/dynmap-pnx/actions/workflows/docker.yml/badge.svg)](https://github.com/xRookieFight/dynmap-pnx/actions/workflows/docker.yml)
[![Release](https://img.shields.io/github/v/release/xRookieFight/dynmap-pnx?include_prereleases&sort=semver)](https://github.com/xRookieFight/dynmap-pnx/releases)
[![License](https://img.shields.io/github/license/xRookieFight/dynmap-pnx)](LICENSE)
![Java 21](https://img.shields.io/badge/java-21-orange)
![PowerNukkitX 3.0](https://img.shields.io/badge/PowerNukkitX-3.0-blue)

</div>

---

## Contents

* [Why](#why)
* [Features](#features)
* [Compatibility](#compatibility)
* [Installation](#installation)
* [Running with Docker](#running-with-docker)
* [Usage](#usage)
* [Configuration](#configuration)
* [How it works](#how-it-works)
* [Known limitations](#known-limitations)
* [Building from source](#building-from-source)
* [Contributing](#contributing)
* [Credits and license](#credits-and-license)

## Why

Dynmap is the classic web map for Java Edition servers, but it never had a Bedrock server port. PowerNukkitX is a full featured Bedrock server written in Java, so DynmapCore can run inside it with a thin bridge. This project is that bridge: it feeds PNX worlds, chunks, players and events into an unmodified DynmapCore, so you get the same web map, renderers and marker API you know from Bukkit.

## Features

* 🗺️ **Full Dynmap web UI** on port `8123`: flat, surface and cave maps, zoom, day/night lighting.
* 🧱 **Native block mapping**: every PowerNukkitX block state is registered with Dynmap at startup. Bedrock block ids line up with the Java names Dynmap's colour schemes use, so vanilla blocks get proper colours out of the box.
* 🌍 **Multi world**: overworld, nether and the end are detected from the level dimension, including worlds loaded or unloaded at runtime.
* ⚡ **Live updates**: placing and breaking blocks invalidates the affected tiles immediately.
* 👥 **Player tracking**: players, health and armor show up on the map in real time.
* 💬 **Chat bridge**: in game chat appears on the web map and web chat is broadcast to the server.
* 📍 **Markers and commands**: `/dynmap`, `/dmap`, `/dmarker` and `/dynmapexp` work as they do on other platforms.
* 🐳 **Docker image** with PowerNukkitX and the plugin preinstalled.

## Compatibility

| Component | Version |
|---|---|
| PowerNukkitX | 3.0.x (built against `3.0.0-SNAPSHOT`) |
| Plugin API (`api` in plugin.yml) | 1.0.0 |
| Dynmap core | 3.9-SNAPSHOT, pinned commit in [`gradle.properties`](gradle.properties) |
| Java | 21 |

## Installation

1. Download `Dynmap-<version>-pnx.jar` from the [latest release](https://github.com/xRookieFight/dynmap-pnx/releases).
2. Put it into the `plugins/` folder of your PowerNukkitX server.
3. Start the server once. Dynmap creates `plugins/dynmap/` with its configuration and web files.
4. Open `http://<server-ip>:8123` in a browser.
5. Run `/dynmap fullrender <world>` from the console to render existing terrain. New chunks render on their own as players explore.

> [!TIP]
> If you are behind a firewall or a hosting panel, open TCP port `8123` in addition to the usual UDP `19132`.

## Running with Docker

The image bundles a PowerNukkitX server with the plugin already in place. The plugin jar is copied into `/data/plugins` on every start, so updating the image updates the plugin.

```bash
docker run -d --name pnx-dynmap \
  -p 19132:19132/udp \
  -p 8123:8123 \
  -v pnx-data:/data \
  -it ghcr.io/xrookiefight/dynmap-pnx:latest
```

With Docker Compose:

```yaml
services:
  pnx:
    image: ghcr.io/xrookiefight/dynmap-pnx:latest
    container_name: pnx-dynmap
    stdin_open: true
    tty: true
    ports:
      - "19132:19132/udp"
      - "8123:8123"
    volumes:
      - ./data:/data
    environment:
      JAVA_OPTS: "-Xms2G -Xmx4G -XX:+UseZGC -XX:+ZGenerational"
    restart: unless-stopped
```

Attach to the console with `docker attach pnx-dynmap` (detach with `Ctrl+P` `Ctrl+Q`).

To build the image yourself:

```bash
docker build -t dynmap-pnx .
docker build --build-arg PNX_VERSION=3.0.1-SNAPSHOT -t dynmap-pnx .
```

| Build argument | Default | Purpose |
|---|---|---|
| `JAVA_VERSION` | `21` | Temurin base image version |
| `PNX_VERSION` | `3.0.0-SNAPSHOT` | PowerNukkitX server build to bundle |
| `PNX_REPOSITORY` | `https://repo.powernukkitx.org/releases` | Maven repository to fetch PNX from |

The Dockerfile clones Dynmap at the commit pinned in `gradle.properties`, so the build is reproducible and needs nothing but Docker.

## Usage

| Command | What it does |
|---|---|
| `/dynmap render` | Render the area around you |
| `/dynmap fullrender <world>` | Render a whole world |
| `/dynmap radiusrender <radius>` | Render a radius around you |
| `/dynmap cancelrender <world>` | Stop a running render |
| `/dynmap pause all` | Pause all rendering |
| `/dynmap stats` | Render statistics |
| `/dynmap hide` / `show` | Hide or show yourself on the map |
| `/dmap worldlist` | List worlds and their maps |
| `/dmarker add <label>` | Add a marker at your position |
| `/dynmapexp` | Export map data to OBJ |

The full command reference is in the [Dynmap wiki](https://github.com/webbukkit/dynmap/wiki/Commands).

### Permissions

All commands sit behind `dynmap.cmd`, which is granted to everyone by default. Individual subcommands then check the usual Dynmap nodes such as `dynmap.fullrender`, `dynmap.marker.add` or `dynmap.hide.self`. Operators have all of them. See the [permission list](https://github.com/webbukkit/dynmap/wiki/Permissions).

## Configuration

Everything lives in `plugins/dynmap/`:

| File | Purpose |
|---|---|
| `configuration.txt` | Main config: web server port, renderers, update rates, chat, storage |
| `worlds.txt` | Per world settings: map list, render limits, visibility |
| `markers.yml` | Markers created with `/dmarker` |
| `permissions.yml.example` | Template for Dynmap's own permission file |
| `web/` | The web UI, editable if you want to restyle it |

Common tweaks in `configuration.txt`:

```yaml
webserver-port: 8123
webserver-bindaddress: 0.0.0.0
deftemplatesuffix: hires
```

Dynmap's [configuration guide](https://github.com/webbukkit/dynmap/wiki/Configuration.txt) applies unchanged. Storage backends such as MySQL, PostgreSQL and SQLite work as well since they are part of DynmapCore.

## How it works

```
 PowerNukkitX server                        DynmapCore
┌─────────────────────┐   events   ┌──────────────────────────┐
│ Levels, chunks,     │──────────▶│ MapManager, renderers,    │
│ players, chat       │  PNXListener│ marker API, web server    │──▶ http://host:8123
│                     │◀──────────│                          │
│ Registries.BLOCK    │  PNXServer  └──────────────────────────┘
└─────────┬───────────┘                   ▲
          │ IChunk API                    │ GenericChunk
          └──────── PNXMapChunkCache ─────┘
```

* At startup `DynmapPlugin` walks the PNX block registry and builds a Dynmap block state for each PNX state, keyed by its state hash.
* `PNXMapChunkCache` reads block states, biomes and light straight from loaded `IChunk` objects instead of parsing NBT, because PNX stores chunks in Bedrock's LevelDB format.
* `PNXServer` and `PNXWorld` expose scheduling, players, bans, permissions and world metadata through Dynmap's platform interfaces.
* `PNXListener` turns PNX events into Dynmap events and tile invalidations.

## Known limitations

* **Biome colours** are looked up by numeric id. Bedrock and Java biome ids mostly agree for vanilla biomes, but custom or newer biomes may fall back to the default colour.
* **Custom blocks** from behaviour packs are registered but have no colour scheme entry, so they render with Dynmap's fallback.
* **Offline player lookups** only resolve players who are online.
* **Sign markers** and `getBlockIDAt` are not implemented yet.
* **Material flags** (solid, leaves, logs, water) are guessed from block names, which can be off for unusual blocks.

Pull requests for any of these are very welcome.

## Building from source

Requirements: **JDK 21** and **git**. Gradle is provided by the wrapper.

```bash
git clone https://github.com/xRookieFight/dynmap-pnx.git
git clone https://github.com/webbukkit/dynmap.git dynmap-src
git -C dynmap-src checkout "$(sed -n 's/^dynmapRef=//p' dynmap-pnx/gradle.properties)"

cd dynmap-pnx
./gradlew build
```

The jar is written to `build/libs/Dynmap-<version>-pnx.jar`.

| Gradle property | Default | Purpose |
|---|---|---|
| `dynmapSource` | `../dynmap-src` | Path to the Dynmap checkout used as a composite build |
| `pnxVersion` | from `libs.versions.toml` | Compile against another PowerNukkitX build |
| `releaseVersion` | `3.9-SNAPSHOT` | Version written to the jar and `plugin.yml` |

Dependency versions are managed in [`gradle/libs.versions.toml`](gradle/libs.versions.toml).

## Contributing

Bug reports, fixes and features are all welcome. Read [CONTRIBUTING.md](CONTRIBUTING.md) for the setup, style rules and pull request process. Security issues go through [SECURITY.md](.github/SECURITY.md).

## Credits and license

* [Dynmap](https://github.com/webbukkit/dynmap) by Mike Primm and contributors does all the heavy lifting.
* [PowerNukkitX](https://github.com/PowerNukkitX/PowerNukkitX) provides the server and API.

Licensed under the [Apache License 2.0](LICENSE), the same as Dynmap. See [NOTICE](NOTICE) for attribution. The Docker image additionally contains PowerNukkitX, which is distributed under the [LGPL 3.0](https://github.com/PowerNukkitX/PowerNukkitX/blob/master/LICENSE).
