rootProject.name = "dynmap-pnx"

includeBuild(providers.gradleProperty("dynmapSource").getOrElse("../dynmap-src"))
