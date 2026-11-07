plugins {
    id("java-library")
    id("dev.kikugie.loom-back-compat")
}

fun prop(name: String): String = property(name) as String
fun propOrNull(name: String): String? = (findProperty(name) as String?)?.takeIf { it.isNotBlank() }

val mcVersion = stonecutter.current.version
val javaVersion = prop("java_version").toInt()
val modId = prop("mod.id")

group = prop("mod.group")
version = "${prop("mod.version")}+$mcVersion"
base.archivesName = "$modId-fabric"

java {
    toolchain.languageVersion = JavaLanguageVersion.of(javaVersion)
}

sourceSets["main"].java {
    exclude("io/github/twister716/ntmthermalize/platform/neoforge/**", "io/github/twister716/ntmthermalize/platform/forge/**")
}

repositories {
    mavenCentral()
    maven("https://maven.blamejared.com") { name = "BlameJared" }
    maven("https://maven.createmod.net") {
        name = "CreateMod"
        content { includeGroup("dev.engine-room.flywheel") }
    }
    exclusiveContent {
        forRepository { maven("https://api.modrinth.com/maven") { name = "Modrinth" } }
        filter { includeGroup("maven.modrinth") }
    }
    exclusiveContent {
        forRepository { maven("https://cursemaven.com") { name = "CurseMaven" } }
        filter { includeGroup("curse.maven") }
    }
    // Tough As Nails and GlitchCore: Glitchfiend publishes them to the Forge maven.
    exclusiveContent {
        forRepository { maven("https://maven.minecraftforge.net") { name = "MinecraftForge" } }
        filter { includeGroup("com.github.glitchfiend") }
    }
    // Golem Core and NTM - Continuum: the jars attached to their GitHub releases.
    // NTM: Space likewise, from its GitHub releases.
    for ((repo, group) in listOf(
        "Golem-Core" to "github.golem-core",
        "NTM-Continuum" to "github.ntm-continuum",
        "NTM-Continuum-Space" to "github.ntm-continuum-space",
    )) {
        exclusiveContent {
            forRepository {
                ivy("https://github.com/Warfactory-Official/$repo/releases/download") {
                    name = repo
                    patternLayout {
                        artifact("v[revision]/[module]-[revision]+[classifier].[ext]")
                        // Releases up to 1.0.1 also carry the Minecraft version before the mod version.
                        artifact("v[revision]/[module]-[classifier]-[revision]+[classifier].[ext]")
                    }
                    metadataSources { artifact() }
                }
            }
            filter { includeGroup(group) }
        }
    }
}

// Below 26.1 Fabric remaps mod dependencies and needs an explicit Mojang mappings layer.
val obfuscated = mcVersion.substringBefore('.').toInt() < 26

propOrNull("deps.lwjgl")?.let { pinned ->
    configurations.configureEach {
        resolutionStrategy.eachDependency {
            if (requested.group == "org.lwjgl") useVersion(pinned)
        }
    }
}

val modCfg = if (obfuscated) "modImplementation" else "implementation"
val devRuntime = if (obfuscated) "modLocalRuntime" else "localRuntime"

dependencies {
    "minecraft"("com.mojang:minecraft:${prop("deps.minecraft")}")
    if (obfuscated) "mappings"(loom.officialMojangMappings())
    modCfg("net.fabricmc:fabric-loader:${prop("deps.fabric_loader")}")
    modCfg("net.fabricmc.fabric-api:fabric-api:${prop("deps.fabric_api")}")

    // Golem Core and NTM - Continuum from their GitHub releases. Golem Core's -dev jar is the regular jar plus the harness
    // listener, which stays idle unless the harness launched the client; the release jar of this mod bundles neither.
    modCfg("github.golem-core:golemcore-fabric:${prop("deps.golemcore")}:$mcVersion-dev")
    modCfg("github.ntm-continuum:hbm-fabric:${prop("deps.hbm")}:$mcVersion")

    // Tough As Nails (required) and its library GlitchCore.
    modCfg("com.github.glitchfiend:ToughAsNails-fabric:${prop("deps.toughasnails")}") {
        // Tough As Nails lists Serene Seasons as a dependency for its optional season support. Keep it out of the
        // dev runs so that seasons don't change the body temperature while testing.
        exclude(group = "com.github.glitchfiend", module = "SereneSeasons-fabric")
    }
    modCfg("com.github.glitchfiend:GlitchCore-fabric:${prop("deps.glitchcore")}")
    // NTM: Space (optional): available to compile against and loaded in dev runs. Code that uses it must first check
    // that the mod is loaded, because players may not have it.
    modCfg("github.ntm-continuum-space:ntmspace-fabric:${prop("deps.ntmspace")}:$mcVersion")

    val jei = propOrNull("deps.jei_mc")
    val jeiVer = propOrNull("deps.jei")
    if (jei != null && jeiVer != null) {
        "compileOnly"("mezz.jei:jei-$jei-common-api:$jeiVer")
        "compileOnly"("mezz.jei:jei-$jei-fabric-api:$jeiVer")
        devRuntime("mezz.jei:jei-$jei-fabric:$jeiVer") { exclude(group = "net.fabricmc", module = "fabric-loader") }
    }

    if (propOrNull("gemrender.run") != "false") {
        propOrNull("deps.gemrender")?.let { devRuntime("curse.maven:gemrender-1735539:$it") }
        propOrNull("deps.gemrender_flywheel")?.let { devRuntime("dev.engine-room.flywheel:flywheel-fabric-$mcVersion:$it") }
    }
    // Sodium, client dev runs only (environment: client); -Psodium.run=false launches without it. On 1.20.1 Sodium 0.5
    // skips Fabric's renderer API (connected textures, HBM's custom models) unless Indium is loaded.
    if (propOrNull("sodium.run") != "false") {
        propOrNull("deps.sodium_fabric")?.let { devRuntime("maven.modrinth:sodium:$it") }
        propOrNull("deps.indium")?.let { devRuntime("maven.modrinth:indium:$it") }
    }
    // HBM's classes carry jspecify annotations; annotations only.
    "compileOnly"("org.jspecify:jspecify:1.0.0")
}

loom {
    runs {
        named("client") {
            client()
            configName = "NTM: Thermalize Fabric Client ($mcVersion)"
            ideConfigGenerated(true)
            runDir = "runs/client"
        }
        named("server") {
            server()
            configName = "NTM: Thermalize Fabric Server ($mcVersion)"
            ideConfigGenerated(true)
            runDir = "runs/server"
        }
        register("gametest") {
            server()
            configName = "NTM: Thermalize Fabric GameTest ($mcVersion)"
            ideConfigGenerated(false)
            runDir = "runs/gametest"
            vmArg("-Dfabric-api.gametest")
            vmArg("-Dfabric-api.gametest.report-file=${project.file("runs/gametest/report.xml").absolutePath}")
            vmArg("-Dgolem.gametest.filter=${providers.gradleProperty("gametest.filter").orElse("$modId:.*").get()}")
        }
    }
}

tasks.matching { it.name == "runGametest" }.configureEach {
    doFirst { delete(layout.projectDirectory.dir("runs/gametest/world")) }
}

val modProps = mapOf(
    "mod_id" to modId,
    "mod_name" to prop("mod.name"),
    "mod_version" to prop("mod.version"),
    "mod_author" to prop("mod.author"),
    "mod_license" to prop("mod.license"),
    "mod_description" to prop("mod.description").replace("\n", "\\n"),
    "minecraft_range" to prop("deps.minecraft_range"),
    "fabric_loader" to prop("deps.fabric_loader"),
    "golemcore_floor" to prop("deps.golemcore_floor"),
    "hbm_floor" to prop("deps.hbm_floor"),
    // deps.toughasnails is <Minecraft version>-<mod version>; the metadata needs only the mod version.
    "toughasnails_floor" to prop("deps.toughasnails").substringAfter('-'),
    "ntmspace_floor" to prop("deps.ntmspace"),
    "java_version" to javaVersion.toString(),
    "pack_format_entry" to if (!obfuscated)
        "\"min_format\": ${prop("pack_format")}, \"max_format\": ${prop("pack_format")}"
    else
        "\"pack_format\": ${prop("pack_format")}",
)

tasks.processResources {
    inputs.properties(modProps)
    filesMatching(listOf("fabric.mod.json", "pack.mcmeta")) { expand(modProps) }
    exclude("META-INF/mods.toml", "META-INF/neoforge.mods.toml", "data/$modId/neoforge/**", "data/$modId/forge/**",
        "data/neoforge/**")
}

tasks.compileJava {
    dependsOn(tasks.named("stonecutterGenerate"))
    options.release = javaVersion
}
tasks.processResources { dependsOn(tasks.named("stonecutterGenerate")) }

extra["mc_version"] = mcVersion
extra["java_version"] = javaVersion.toString()
// Golem Core's shared build scripts; their parameters (golem.build.*) are in gradle.properties.
apply(from = rootProject.file("gradle/golem/run-toolchain.gradle.kts"))
apply(from = rootProject.file("gradle/golem/harness-spec.gradle.kts"))
apply(from = rootProject.file("gradle/golem/legacy-recipes.gradle.kts"))
extra["loader"] = "fabric"
apply(from = rootProject.file("gradle/golem/recipe-conditions.gradle.kts"))
apply(from = rootProject.file("gradle/golem/legacy-biomes.gradle.kts"))
apply(from = rootProject.file("gradle/golem/legacy-dimensions.gradle.kts"))
apply(from = rootProject.file("gradle/golem/legacy-assets.gradle.kts"))
// Dejson fails on an empty class list, so it is applied once gradle/dejson-classes.txt names a class.
if (rootProject.file("gradle/dejson-classes.txt").readLines().any { it.isNotBlank() && !it.trimStart().startsWith("#") }) {
    apply(from = rootProject.file("gradle/golem/dejson.gradle.kts"))
}
apply(from = rootProject.file("gradle/golem/mixin-validation.gradle.kts"))
apply(from = rootProject.file("gradle/golem/release-jar.gradle.kts"))
apply(from = rootProject.file("gradle/golem/mod-metadata.gradle.kts"))
// Modrinth + CurseForge upload of this node's jar; run every node with the root `publishAllMods`.
apply(from = rootProject.file("gradle/golem/publish.gradle.kts"))
