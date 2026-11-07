pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("dev.kikugie.stonecutter") version "0.9.8"
    // Picks the right Fabric Loom variant per node (obfuscated below 26.1).
    id("dev.kikugie.loom-back-compat") version "0.4.2"
}

stonecutter {
    create(rootProject) {
        // One node per (Minecraft version, loader). The addon runs on the NTM - Continuum build of the same node.
        fun match(version: String, vararg loaders: String) = loaders.forEach {
            version("$version-$it", version).buildscript = "build.$it.gradle.kts"
        }

        match("26.1", "fabric", "neoforge")
        match("1.21.1", "fabric", "neoforge")
        match("1.20.1", "fabric", "forge")

        vcsVersion = "26.1-neoforge"
    }
}

rootProject.name = "ntmthermalize"
