plugins {
    alias(libs.plugins.ktlint) apply false
    id(libs.plugins.bukkript.build.get().pluginId) apply false
    alias(libs.plugins.dependencyGraph)
    id("org.gradle.maven-publish")
    id("org.gradle.java")
}

subprojects {
    apply(plugin = "org.gradle.maven-publish")
    apply(plugin = "org.gradle.java")

    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://raw.githubusercontent.com/KotlinMinecraft/KotlinBukkitAPI-Repository/main")
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
                groupId = "br.com.devsrsouza.bukkript"
                version = "1.0.0-SNAPSHOT"
                afterEvaluate {
                    artifactId = tasks.jar.get().archiveBaseName.get()
                }
            }
        }
    }


}

