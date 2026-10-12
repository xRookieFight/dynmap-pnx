plugins {
    java
    alias(libs.plugins.shadow)
}

group = "us.dynmap"
version = providers.gradleProperty("releaseVersion")
    .map { it.removePrefix("v") }
    .getOrElse("3.9-SNAPSHOT")

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
    maven("https://repo.powernukkitx.org/releases")
    maven("https://repo.opencollab.dev/maven-releases/")
    maven("https://repo.opencollab.dev/maven-snapshots/")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    shadow(libs.dynmap.core)
    implementation(libs.dynmap.core)
    implementation(libs.dynmap.core.api)

    compileOnly(
        project.findProperty("pnxVersion")?.let { "org.powernukkitx:server:$it" }
            ?: libs.powernukkitx.server.get()
    )
}

tasks.processResources {
    val props = mapOf("version" to project.version)
    inputs.properties(props)
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.shadowJar {
    archiveBaseName.set("Dynmap")
    archiveClassifier.set("pnx")
    configurations = listOf(project.configurations.shadow.get())
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
