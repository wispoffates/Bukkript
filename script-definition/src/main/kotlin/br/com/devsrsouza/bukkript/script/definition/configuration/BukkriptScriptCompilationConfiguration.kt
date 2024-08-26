package br.com.devsrsouza.bukkript.script.definition.configuration

import br.com.devsrsouza.bukkript.script.definition.annotation.DependPlugin
import br.com.devsrsouza.bukkript.script.definition.annotation.Import
import br.com.devsrsouza.bukkript.script.definition.annotation.Script
import br.com.devsrsouza.bukkript.script.definition.dependencies.baseDependencies
import br.com.devsrsouza.bukkript.script.definition.resolver.resolveScriptAnnotation
import br.com.devsrsouza.bukkript.script.definition.resolver.resolveScriptStaticDependencies
import java.io.File
import kotlin.script.experimental.api.ScriptAcceptedLocation
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.acceptedLocations
import kotlin.script.experimental.api.compilerOptions
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.api.ide
import kotlin.script.experimental.api.refineConfiguration
import kotlin.script.experimental.dependencies.DependsOn
import kotlin.script.experimental.dependencies.Repository
import kotlin.script.experimental.jvm.dependenciesFromClassContext
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvm.updateClasspath
import kotlin.script.experimental.jvm.util.classpathFromClassloader

class BukkriptScriptCompilationConfiguration : ScriptCompilationConfiguration({
    defaultImports(
        Script::class, DependPlugin::class, Import::class, DependsOn::class, Repository::class
    )
    jvm {
        // jdkHome(File("/Users/gabriel/.asdf/installs/java/temurin-11.0.19+7"))
        dependenciesFromCurrentContext(wholeClasspath = true)
        //dependenciesFromClassContext(BukkriptScriptCompilationConfiguration::class, wholeClasspath = true)
        compilerOptions(
            // "-Xopt-in=kotlin.time.ExperimentalTime,kotlin.ExperimentalStdlibApi,kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-Xadd-modules=ALL-MODULE-PATH",
            "-jvm-target",
            "21",
            "-api-version",
            "1.9",
        )
    }
    refineConfiguration {
        beforeCompiling(handler = ::resolveScriptStaticDependencies)
        onAnnotations(
            listOf(Script::class, DependPlugin::class, Import::class, DependsOn::class, Repository::class),
            handler = ::resolveScriptAnnotation,
        )
    }
    ide {
        acceptedLocations(ScriptAcceptedLocation.Everywhere)
    }
})
