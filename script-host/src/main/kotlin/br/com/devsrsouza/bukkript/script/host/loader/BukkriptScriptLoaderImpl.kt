package br.com.devsrsouza.bukkript.script.host.loader

import br.com.devsrsouza.bukkript.script.definition.BukkriptScript
import br.com.devsrsouza.bukkript.script.definition.api.LogLevel
import br.com.devsrsouza.bukkript.script.definition.bukkritNameRelative
import br.com.devsrsouza.bukkript.script.host.compiler.BukkriptCompiledScript
import br.com.devsrsouza.bukkript.script.host.loader.classloader.ClassProvider
import br.com.devsrsouza.bukkript.script.host.loader.classloader.ScriptClassloader
import org.bukkit.plugin.Plugin
import java.io.File
import kotlin.script.experimental.api.*
import kotlin.script.experimental.jvm.BasicJvmScriptEvaluator
import kotlin.script.experimental.jvm.baseClassLoader
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvm.loadDependencies

class BukkriptScriptLoaderImpl(
    val plugin: Plugin,
    val scriptDir: File,
    val parentClassloader: ClassLoader,
    val classProvider: ClassProvider,
    val logger: (script: String, LogLevel, message: String) -> Unit,
) : BukkriptScriptLoader {

    override suspend fun load(compiledScript: BukkriptCompiledScript): BukkriptLoadedScript {
        val dataFolder = File(scriptDir, compiledScript.source.bukkritNameRelative(scriptDir))

        val classLoader = ScriptClassloader(
            classProvider,
            parentClassloader,
            compiledScript.description.dependenciesFiles.map { File(it) }.toSet(),
        )

        fun scriptLog(level: LogLevel, message: String) {
            logger(compiledScript.scriptName, level, message)
        }

        val evalConfig = ScriptEvaluationConfiguration {
            constructorArgs(
                plugin,
                compiledScript.description,
                dataFolder,
                compiledScript.scriptName,
                ::scriptLog,
            )
            jvm {
                baseClassLoader(classLoader)
                loadDependencies(false)
            }
        }

        val evaluator = BasicJvmScriptEvaluator()

        val result = evaluator(compiledScript.compiled, evalConfig)

        return when (result) {
            is ResultWithDiagnostics.Success -> {
                when (val it = result.value.returnValue) {
                    is ResultValue.Error -> throw it.error
                    ResultValue.NotEvaluated -> throw error("Script not evaluated?!")
                    else -> {
                        // VALUE and UNIT
                        if (it.scriptClass != null && it.scriptInstance !== null) {
                            BukkriptLoadedScript(
                                it.scriptInstance as BukkriptScript,
                                it.scriptClass!!,
                                classLoader,
                                compiledScript,
                                dataFolder,
                            )
                        } else {
                            println("Something strange happened while loading the script!")
                            result.reports.forEach {
                                println(" : ${it.message}" + if (it.exception == null) "" else ": ${it.exception}")
                            }
                           throw error("Success but no script?")
                        }
                    }
                }
            }
            is ResultWithDiagnostics.Failure -> {
                result.reports.forEach {
                    if (it.severity > ScriptDiagnostic.Severity.DEBUG) {
                        println(" : ${it.message}" + if (it.exception == null) "" else ": ${it.exception}")
                    }
                }
                throw error("Failure loading script!")
            }
        }
    }
}
