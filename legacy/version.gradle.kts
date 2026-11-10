import java.util.Properties

plugins {
    java
    id("gg.essential.loom")
    id("dev.architectury.architectury-pack200")
}

val modid: String by project
val mcVersion: String by project
val mappingsVersion: String by project
val forgeVersion: String by project
// The version of the mod is -PmodVersion when CI gives it, and mod_version of the modern build if not.
val modVersion = providers.gradleProperty("modVersion").orNull
    ?: Properties().apply { rootProject.file("../modern/gradle.properties").inputStream().use { load(it) } }.getProperty("mod_version")

version = "$modVersion+$mcVersion"
base.archivesName.set("$modid-forge")

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

// Writes BuildInfo.java so that the @Mod annotation gets the mod version and the Minecraft version from the build.
val generateBuildInfo = tasks.register("generateBuildInfo") {
    val outputDir = layout.buildDirectory.dir("generated/buildinfo")
    inputs.property("modVersion", modVersion)
    inputs.property("mcVersion", mcVersion)
    outputs.dir(outputDir)
    doLast {
        val file = outputDir.get().file("wvv/windowstitlebardelete/legacy/BuildInfo.java").asFile
        file.parentFile.mkdirs()
        file.writeText(
            """
            |package wvv.windowstitlebardelete.legacy;
            |
            |final class BuildInfo {
            |    static final String VERSION = "$modVersion";
            |    static final String MINECRAFT_RANGE = "[$mcVersion]";
            |
            |    private BuildInfo() {
            |    }
            |}
            |""".trimMargin()
        )
    }
}

sourceSets.main {
    java.srcDir(generateBuildInfo)
    java.srcDir(rootProject.file("src/main/java"))
    java.srcDir(rootProject.file("../core/src/main/java"))
    resources.srcDir(rootProject.file("src/main/resources"))
    resources.srcDir(rootProject.file("../core/src/main/resources"))
    // Forge in dev finds mcmod.info and assets only next to the classes.
    output.setResourcesDir(sourceSets.main.flatMap { it.java.classesDirectory })
}

loom.forge.pack200Provider.set(dev.architectury.pack200.java.Pack200Adapter())

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings(mappingsVersion)
    forge(forgeVersion)
}

tasks.withType(JavaCompile::class) {
    options.encoding = "UTF-8"
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("mcversion", mcVersion)
    inputs.property("modid", modid)
    filesMatching("mcmod.info") {
        expand(inputs.properties)
    }
}

tasks.jar {
    archiveClassifier.set("dev")
    destinationDirectory.set(layout.buildDirectory.dir("intermediates"))
}

tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
    archiveClassifier.set("")
}

// Loom 0.10's own runClient trips Gradle 8's task validation, so this runs the same launch as a plain JavaExec.
tasks.register<JavaExec>("runClientDev") {
    group = "minecraft"
    description = "Starts the $mcVersion Forge dev client."
    // downloadAssets also extracts the LWJGL natives, which Loom does in no other task that this run uses.
    dependsOn("classes", "downloadAssets")
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("net.fabricmc.devlaunchinjector.Main")
    workingDir = file("run")
    doFirst { workingDir.mkdirs() }
    javaLauncher.set(javaToolchains.launcherFor { languageVersion.set(JavaLanguageVersion.of(8)) })
    jvmArgs(
        "-Dfabric.dli.config=${file(".gradle/loom-cache/launch.cfg").absolutePath}",
        "-Dfabric.dli.env=client",
        "-Dfabric.dli.main=net.minecraft.launchwrapper.Launch",
    )
    // -Pharness=<dir> starts the test harness of the core (core/dev/Harness).
    if (project.hasProperty("harness")) {
        systemProperty("wtd.harness", project.property("harness")!!)
    }
}
