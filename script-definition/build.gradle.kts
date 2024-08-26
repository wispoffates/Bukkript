plugins {
    id(libs.plugins.bukkript.build.get().pluginId)
    //alias(libs.plugins.maven)
}

dependencies {
    compileOnly(libs.spigot.api)

    implementation(kotlin("scripting-common"))
    implementation(kotlin("scripting-jvm"))
    implementation(kotlin("scripting-dependencies"))
    implementation(kotlin("scripting-dependencies-maven"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")

    compileOnly(libs.coroutines)

    compileOnly(libs.kotlinbukkitapi.coroutines)
    compileOnly(libs.kotlinbukkitapi.utility)
    compileOnly(libs.kotlinbukkitapi.architecture)
    compileOnly(libs.kotlinbukkitapi.extensions)
    compileOnly(libs.kotlinbukkitapi.exposed)
    compileOnly(libs.kotlinbukkitapi.commandLegacy)
    compileOnly(libs.kotlinbukkitapi.menu)
    compileOnly(libs.kotlinbukkitapi.scoreboardLegacy)
    //implementation(libs.kotlinbukkitapi.serialization) TODO: support Ktx Serialization at scripts

    //implementation("com.google.inject:guice:4.2.3")
}