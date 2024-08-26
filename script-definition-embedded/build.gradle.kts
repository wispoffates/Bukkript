plugins {
    id(libs.plugins.bukkript.build.get().pluginId)
    id("org.gradle.maven-publish")
}

dependencies {
    implementation(libs.spigot.api)

    implementation(projects.scriptDefinition)

    implementation(kotlin("scripting-common"))
    implementation(kotlin("scripting-jvm"))
    implementation(kotlin("scripting-dependencies"))
    implementation(kotlin("scripting-dependencies-maven"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")


    implementation(libs.coroutines)

    implementation(libs.kotlinbukkitapi.coroutines)
    implementation(libs.kotlinbukkitapi.utility)
    implementation(libs.kotlinbukkitapi.architecture)
    implementation(libs.kotlinbukkitapi.extensions)
    implementation(libs.kotlinbukkitapi.exposed)
    implementation(libs.kotlinbukkitapi.commandLegacy)
    implementation(libs.kotlinbukkitapi.menu)
    implementation(libs.kotlinbukkitapi.scoreboardLegacy)
}