package br.com.devsrsouza.bukkript.script.host.compiler

import br.com.devsrsouza.bukkript.script.definition.BukkriptScript
import br.com.devsrsouza.bukkript.script.definition.ScriptDescription
import br.com.devsrsouza.bukkript.script.definition.bukkriptNameRelative
import br.com.devsrsouza.bukkript.script.definition.configuration.info
import br.com.devsrsouza.bukkript.script.host.cache.CachedScript
import br.com.devsrsouza.bukkript.script.host.cache.FileBasedScriptCache
import br.com.devsrsouza.bukkript.script.host.exception.BukkriptCompilationException
import java.io.File
import kotlin.script.experimental.api.CompiledScript
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.refineConfiguration
import kotlin.script.experimental.api.valueOrThrow
import kotlin.script.experimental.api.with
import kotlin.script.experimental.host.FileScriptSource
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.host.configurationDependencies
import kotlin.script.experimental.host.with
import kotlin.script.experimental.jvm.compilationCache
import kotlin.script.experimental.jvm.defaultJvmScriptingHostConfiguration
import kotlin.script.experimental.jvm.impl.KJvmCompiledScript
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.JvmScriptCompiler
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate

class BukkriptScriptCompilerImpl(
    val scriptDir: File,
    val cacheDir: File,
) : BukkriptScriptCompiler {

    override suspend fun retrieveDescriptor(scriptFile: File): ScriptDescription? {
        var scriptDescriptionLoaded: ScriptDescription? = null

        println("Trying to do retrieveDescriptor before actually compiling lol lol lol")
        val customConfiguration =
            createJvmCompilationConfigurationFromTemplate<BukkriptScript>().with {
                refineConfiguration {
                    println("Was the refineConfiguration of comes and executes?")
                    beforeParsing {
                        println("Oh no before parsing was called correctly")
                        return@beforeParsing ResultWithDiagnostics.Success(it.compilationConfiguration)
                    }
                    beforeCompiling { context ->
                        println("beforeCompiling refine mother fucker")
                        val info = context.compilationConfiguration[ScriptCompilationConfiguration.info]!!

                        scriptDescriptionLoaded = info

                        return@beforeCompiling ResultWithDiagnostics.Failure()
                    }
                }
            }

        val source = FileScriptSource(scriptFile)

        runCatching {
            println("Trying to make that compilation!")
            compile(source, customConfiguration).valueOrThrow()
            println("That compilation was OKAY")
        }.onFailure {
            println("That compilation was of food and drinks")
            if (scriptDescriptionLoaded == null) {
                throw it
            }
        }

        return scriptDescriptionLoaded
    }

    override suspend fun compile(scriptFile: File, description: ScriptDescription): BukkriptCompiledScript {
        val source = FileScriptSource(scriptFile)

        val cache = FileBasedScriptCache(scriptDir, cacheDir, description)

        val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<BukkriptScript>()

        val hostConfiguration = defaultJvmScriptingHostConfiguration.with {
            jvm {
                compilationCache(cache)
            }
            this.configurationDependencies
        }

        return runCatching {
            val compiledScript = compile(source, compilationConfiguration, hostConfiguration)
                .valueOrThrow() as KJvmCompiledScript

            BukkriptCompiledScript(
                scriptFile.bukkriptNameRelative(scriptDir),
                source,
                compiledScript,
                description,
            )
        }.getOrElse {
            throw BukkriptCompilationException(it)
        }
    }

    override suspend fun getCachedScript(scriptFile: File): CachedScript? {
        val scriptCache = FileBasedScriptCache(scriptDir, cacheDir, null)

        val source = FileScriptSource(scriptFile)

        return scriptCache.findCacheScript(source)
    }

    private suspend fun compile(
        source: FileScriptSource,
        configuration: ScriptCompilationConfiguration,
        hostConfiguration: ScriptingHostConfiguration = defaultJvmScriptingHostConfiguration,
    ): ResultWithDiagnostics<CompiledScript> {
        val compiler = JvmScriptCompiler(hostConfiguration)

        return compiler(source, configuration)
    }
}
