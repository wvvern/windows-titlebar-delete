// Loads the plugins once so both version subprojects share one Loom classloader.
plugins {
    id("gg.essential.loom") version "0.10.0.+" apply false
    id("dev.architectury.architectury-pack200") version "0.1.3" apply false
}
