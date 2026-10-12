# Contributing to Dynmap for PowerNukkitX

Thanks for wanting to help. This port is small, so even a single well tested fix makes a real difference.

## Before you start

* **Bugs**: open an issue with the bug report template first, unless the fix is tiny and obvious.
* **Features**: open a feature request and wait for a thumbs up before writing a lot of code. It saves both of us from a pull request that can't be merged.
* **Upstream problems**: if something is also broken on Bukkit, Forge or Fabric, it belongs in [webbukkit/dynmap](https://github.com/webbukkit/dynmap), not here.

## Project layout

```
dynmap-pnx/
├── gradle/libs.versions.toml           dependency and plugin versions
├── gradle.properties                   pinned Dynmap revision and Gradle settings
├── settings.gradle.kts                 includes ../dynmap-src as a composite build
├── build.gradle.kts                    shading and plugin.yml expansion
└── src/main/java/org/dynmap/pnx/
    ├── DynmapPlugin.java               entry point and block state registry
    ├── PNXServer.java                  DynmapServerInterface for PNX
    ├── PNXWorld.java                   DynmapWorld backed by a PNX Level
    ├── PNXMapChunkCache.java           reads PNX chunks into GenericChunk
    ├── PNXListener.java                PNX events → Dynmap events
    ├── PNXPlayer.java                  player wrapper
    ├── PNXCommandSender.java           console and non player senders
    └── PNXLog.java                     logger bridge
```

The plugin only contains the bridge between PowerNukkitX and Dynmap. Rendering, the web server and the web UI all live in DynmapCore, which is built from source next to this repo.

## Development setup

You need **JDK 21** and **git**. Gradle 8.14 does not run on newer JDKs, so point `JAVA_HOME` at a 21 install if your default is something else.

Clone both repositories side by side. The folder names matter because `settings.gradle.kts` looks for `../dynmap-src`:

```bash
mkdir dynmapx && cd dynmapx
git clone https://github.com/xRookieFight/dynmap-pnx.git
git clone https://github.com/webbukkit/dynmap.git dynmap-src

cd dynmap-src
git checkout "$(sed -n 's/^dynmapRef=//p' ../dynmap-pnx/gradle.properties)"

cd ../dynmap-pnx
./gradlew build
```

The plugin jar ends up in `build/libs/Dynmap-<version>-pnx.jar`.

If your Dynmap checkout lives somewhere else, pass its path instead of moving it:

```bash
./gradlew build -PdynmapSource=/path/to/dynmap
```

To compile against a different PowerNukkitX build, override the version from the catalog:

```bash
./gradlew build -PpnxVersion=3.0.1-SNAPSHOT
```

## Testing your change

There is no automated test suite yet, because almost everything here needs a running server. Please test by hand:

1. Build the jar and drop it into `plugins/` of a PowerNukkitX 3.x server, or use the Docker image (see the README).
2. Start the server, open `http://localhost:8123` and check the map loads.
3. Run `/dynmap fullrender <world>` on a world that exercises your change.
4. Place and break blocks, join and leave, chat, and make sure the map and player list follow along.

Mention the PNX version, the world type and what you looked at in the pull request.

## Code style

* Java 21, four spaces, LF line endings. `.editorconfig` covers the basics.
* Match the surrounding code: naming, null checks, early returns.
* **No comments or doc blocks in Java sources.** The code base is kept comment free on purpose. If something needs explaining, pick better names or put the explanation in the pull request description.
* Keep the bridge thin. If a change really belongs in DynmapCore, propose it upstream.
* Add new dependencies through `gradle/libs.versions.toml`, never as inline strings.
* Don't reformat files you aren't otherwise touching.

## Commits and pull requests

* Branch from `main` with a descriptive name such as `fix/nether-biome-colors` or `feat/sign-markers`.
* One purpose per commit. Commit titles are short imperative sentences: `Fix biome lookup for custom dimensions`.
* Fill in the pull request template, link the issue and describe how you tested.
* CI must be green. It builds the plugin against the pinned Dynmap revision and builds the Docker image when the Dockerfile changes.

## Bumping Dynmap or PowerNukkitX

* **Dynmap**: change `dynmapRef` in `gradle.properties` to the new upstream commit, and the `dynmap` version in `libs.versions.toml` if upstream changed its version. CI and the Dockerfile both read the ref from there.
* **PowerNukkitX**: change `powernukkitx` in `libs.versions.toml` and the `PNX_VERSION` build argument default in the `Dockerfile`.

Build and smoke test on a real server before opening the pull request.

## Releases

Maintainers push a `v*` tag, for example `v3.9-pnx.1`. The release workflow builds the jar with that version, attaches it to a GitHub release and the Docker workflow publishes a matching image to GHCR.

## License

By contributing you agree that your work is licensed under the [Apache License 2.0](LICENSE), the same license as Dynmap.
