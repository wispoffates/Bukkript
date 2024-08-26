import java.util.Properties
import java.io.File

plugins {
    id(libs.plugins.bukkript.build.get().pluginId)
    alias(libs.plugins.shadow)
    alias(libs.plugins.pluginYml.bukkit)
    alias(libs.plugins.pluginYml.paper)
}

dependencies {
    compileOnly(libs.spigot.api)
    compileOnly(libs.paper.api)
    implementation(libs.maven.resolver.transportHttp)
    implementation(libs.maven.resolver.connectorBasic)

    implementation(libs.bstats)
    
    implementation(projects.scriptHost)

    implementation(kotlin("stdlib"))

    implementation(libs.kotlinbukkitapi.architecture)
    // this is Called paper libraries but will also be used in Spigot
    // because spigot does not support non maven central repository
    // so the logic here is: we shadow kotlinbukkitapi-architecture only without Kotlin
    // we let kotlin stdlib be downloaded by Spigot and then with Kotlin + Architecture
    // the plugin loaded correctly, and we can the Maven Aether Resolver to fully resolve
    // dependencies from paper dependency file.
    // On Paper side, we just download Kotlin Stdlib because the rest will be avaiable at the
    // final paper dependencies files.
    implementation(libs.coroutines)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
    
    //these excludes are gross but I cannot get shadowjar to exclude them
    //github issues say it might be broken
    implementation(libs.kotlinbukkitapi.coroutines)
    implementation(libs.kotlinbukkitapi.utility) 
    implementation(libs.kotlinbukkitapi.extensions)
    implementation(libs.kotlinbukkitapi.exposed) 
    implementation(libs.kotlinbukkitapi.commandLegacy) 
    implementation(libs.kotlinbukkitapi.menu)
    implementation(libs.kotlinbukkitapi.scoreboardLegacy)

    // scripting dependencies
    implementation(kotlin("scripting-jvm"))
    implementation(kotlin("scripting-common"))
    implementation(kotlin("scripting-dependencies"))
    implementation(kotlin("scripting-dependencies-maven"))
    implementation(kotlin("scripting-jvm"))
    implementation(kotlin("scripting-jvm-host"))
    implementation(kotlin("scripting-compiler-embeddable"))
    implementation(kotlin("compiler-embeddable"))
    implementation(project(":script-definition"))
    implementation(project(":script-host"))
}

tasks {
    shadowJar {
        archiveBaseName.set("Bukkript")
        //archiveClassifier.set("")
        
        relocate("org.bstats", "br.com.devsrsouza.bukkript.bstats")
        //relocate("br.com.devsrsouza.kotlinbukkitapi","br.com.devsrsouza.kotlinbukkitapi")
    }
}

val pluginMain = "br.com.devsrsouza.bukkript.plugin.BukkriptPlugin"

bukkit {
    name = "Bukkript"
    main = pluginMain
    author = "DevSrSouza"
    website = "github.com.br/DevSrSouza"
    apiVersion = "1.21"
    
    description = "Bukkript Scripting."
}

paper {
    name = "Bukkript"
    //loader = "br.com.devsrsouza.bukkript.libraryresolver.PluginLibrariesLoader"
    generateLibrariesJson = true
    main = pluginMain
    description = "Bukkript Scripting."
    apiVersion = "1.21"
    serverDependencies {
        register("ProtocolLib") {
            required = false
        }
        register("PlaceholderAPI") {
            required = false
        }
    }
}

val localProperties = Properties()
    .apply {
        load(File(rootDir, "local.properties").inputStream())
    }

val serverPluginFolder = localProperties["serverPluginsFolder"]?.toString()
if(serverPluginFolder != null) {
    tasks.register<Copy>("copyToServer") {
        dependsOn(tasks.shadowJar)
        doFirst {
            File(serverPluginFolder).listFiles()?.filter { it.startsWith("Bukkript") && it.extension == "jar" }
                ?.forEach { it.delete() }
        }
        this.from(tasks.shadowJar.get().archiveFile.get().asFile)
        this.into(File(serverPluginFolder))
    }
}
