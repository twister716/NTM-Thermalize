plugins {
    id("java-library")
    id("net.neoforged.moddev.legacyforge") version "2.0.141"
}

fun prop(name: String): String = property(name) as String
fun propOrNull(name: String): String? = (findProperty(name) as String?)?.takeIf { it.isNotBlank() }

val mcVersion = stonecutter.current.version
val javaVersion = prop("java_version").toInt()
val modId = prop("mod.id")

group = prop("mod.group")
version = "${prop("mod.version")}+$mcVersion"
base.archivesName = "$modId-forge"

java { toolchain.languageVersion = JavaLanguageVersion.of(javaVersion) }

sourceSets["main"].java {
    exclude("io/github/twister716/ntmthermalize/platform/fabric/**", "io/github/twister716/ntmthermalize/platform/neoforge/**")
}

legacyForge {
    version = prop("deps.forge")
    runs {
        configureEach {
            systemProperty("forge.enabledGameTestNamespaces", modId)
            ideName = "NTM: Thermalize Forge ${name.replaceFirstChar { it.uppercase() }} ($mcVersion)"
        }
        register("client") {
            client()
            gameDirectory = file("runs/client")
        }
        register("server") {
            server()
            gameDirectory = file("runs/server")
        }
        register("gameTestServer") {
            type = "gameTestServer"
            gameDirectory = file("runs/gametest")
            jvmArgument("-Dgolem.gametest.filter=${providers.gradleProperty("gametest.filter").orElse("$modId:.*").get()}")
        }
    }
    mods {
        register(modId) { sourceSet(sourceSets["main"]) }
    }
}

// Every GameTest run starts from an empty flat world.
tasks.matching { it.name == "runGameTestServer" }.configureEach {
    doFirst {
        delete(layout.projectDirectory.dir("runs/gametest/world"))
        val properties = layout.projectDirectory.file("runs/gametest/server.properties").asFile
        properties.parentFile.mkdirs()
        val lines = if (properties.exists()) properties.readLines().filterNot {
            it.startsWith("level-type=") || it.startsWith("level-seed=") || it.startsWith("max-tick-time=")
        } else emptyList()
        properties.writeText((lines + listOf("level-type=minecraft:flat", "level-seed=0", "max-tick-time=-1"))
            .joinToString(System.lineSeparator()) + System.lineSeparator())
    }
}

repositories {
    mavenCentral()
    maven("https://maven.blamejared.com") { name = "BlameJared" }
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

dependencies {
    // Golem Core and NTM - Continuum from their GitHub releases. Golem Core's -dev jar is the regular jar plus the harness
    // listener, which stays idle unless the harness launched the client; the release jar of this mod bundles neither.
    modImplementation("github.golem-core:golemcore-forge:${prop("deps.golemcore")}:$mcVersion-dev")
    modImplementation("github.ntm-continuum:hbm-forge:${prop("deps.hbm")}:$mcVersion")

    // Tough As Nails (required) and its library GlitchCore.
    modImplementation("com.github.glitchfiend:ToughAsNails-forge:${prop("deps.toughasnails")}") {
        // Tough As Nails lists Serene Seasons as a dependency for its optional season support. Keep it out of the
        // dev runs so that seasons don't change the body temperature while testing.
        exclude(group = "com.github.glitchfiend", module = "SereneSeasons-forge")
    }
    modImplementation("com.github.glitchfiend:GlitchCore-forge:${prop("deps.glitchcore")}")
    // NTM: Space (optional): available to compile against and loaded in dev runs. Code that uses it must first check
    // that the mod is loaded, because players may not have it.
    modImplementation("github.ntm-continuum-space:ntmspace-forge:${prop("deps.ntmspace")}:$mcVersion")

    val jei = propOrNull("deps.jei_mc")
    val jeiVer = propOrNull("deps.jei")
    if (jei != null && jeiVer != null) {
        modCompileOnly("mezz.jei:jei-$jei-common-api:$jeiVer")
        modCompileOnly("mezz.jei:jei-$jei-forge-api:$jeiVer")
        modRuntimeOnly("mezz.jei:jei-$jei-forge:$jeiVer")
    }

    // GemRender, dev runs only; -Pgemrender.run=false launches without it.
    if (propOrNull("gemrender.run") != "false") {
        propOrNull("deps.gemrender")?.let { modRuntimeOnly("curse.maven:gemrender-1735539:$it") }
    }
    // Embeddium, the Forge port of Sodium, client dev runs only; -Psodium.run=false launches without it.
    if (propOrNull("sodium.run") != "false") {
        propOrNull("deps.embeddium")?.let { modRuntimeOnly("maven.modrinth:embeddium:$it") }
    }
    // HBM's classes carry jspecify annotations; annotations only.
    compileOnly("org.jspecify:jspecify:1.0.0")
}

val modProps = mapOf(
    "mod_id" to modId,
    "mod_name" to prop("mod.name"),
    "mod_version" to prop("mod.version"),
    "mod_author" to prop("mod.author"),
    "mod_license" to prop("mod.license"),
    "mod_description" to prop("mod.description"),
    "minecraft_range" to prop("deps.minecraft_range"),
    "forge_loader_range" to prop("deps.forge_loader_range"),
    "golemcore_floor" to prop("deps.golemcore_floor"),
    "hbm_floor" to prop("deps.hbm_floor"),
    // deps.toughasnails is <Minecraft version>-<mod version>; the metadata needs only the mod version.
    "toughasnails_floor" to prop("deps.toughasnails").substringAfter('-'),
    "ntmspace_floor" to prop("deps.ntmspace"),
    "java_version" to javaVersion.toString(),
    "pack_format_entry" to "\"pack_format\": ${prop("pack_format")}",
)

tasks.processResources {
    inputs.properties(modProps)
    filesMatching(listOf("META-INF/mods.toml", "pack.mcmeta")) { expand(modProps) }
    exclude("fabric.mod.json", "META-INF/neoforge.mods.toml", "data/$modId/neoforge/**", "data/neoforge/**")
}

tasks.compileJava {
    dependsOn(tasks.named("stonecutterGenerate"))
    options.release = javaVersion
}
tasks.processResources { dependsOn(tasks.named("stonecutterGenerate")) }

extra["mc_version"] = mcVersion
extra["java_version"] = javaVersion.toString()
// Forge 1.20.1's convention tag namespace is `forge:`, not `c:`.
extra["convention_namespace"] = "forge"
// Golem Core's shared build scripts; their parameters (golem.build.*) are in gradle.properties.
apply(from = rootProject.file("gradle/golem/run-toolchain.gradle.kts"))
apply(from = rootProject.file("gradle/golem/harness-spec.gradle.kts"))
apply(from = rootProject.file("gradle/golem/legacy-recipes.gradle.kts"))
extra["loader"] = "forge"
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
