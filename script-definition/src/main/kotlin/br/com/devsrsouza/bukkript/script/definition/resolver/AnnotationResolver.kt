package br.com.devsrsouza.bukkript.script.definition.resolver

import br.com.devsrsouza.bukkript.script.definition.ScriptDescription
import br.com.devsrsouza.bukkript.script.definition.annotation.DependPlugin
import br.com.devsrsouza.bukkript.script.definition.annotation.Script
import br.com.devsrsouza.bukkript.script.definition.api.LogLevel
import br.com.devsrsouza.bukkript.script.definition.configuration.info
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptCollectedData
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.ScriptConfigurationRefinementContext
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.asSuccess
import kotlin.script.experimental.api.dependenciesSources
import kotlin.script.experimental.api.foundAnnotations
import kotlin.script.experimental.api.ide
import kotlin.script.experimental.api.makeFailureResult
import kotlin.script.experimental.api.with
import kotlin.script.experimental.jvm.JvmDependency
import kotlin.script.experimental.jvm.updateClasspath

fun resolveScriptAnnotation(
    ctx: ScriptConfigurationRefinementContext,
): ResultWithDiagnostics<ScriptCompilationConfiguration> {
    println("resolvendo as merdas das anotacoes: resolveScriptAnnotation")
    val annotations = ctx.collectedData?.get(ScriptCollectedData.foundAnnotations)
        ?.takeIf { it.isNotEmpty() } ?: return ctx.compilationConfiguration.asSuccess()

    val reports = mutableListOf<ScriptDiagnostic>()

    val configuration = ctx.compilationConfiguration.with {
        var version = "Unknown"
        var author = "Unknown"
        var website = "None"
        var logLevel = LogLevel.INFO

        val pluginDepends = mutableSetOf<String>()

        for (annotation in annotations) {
            when (annotation) {
                is Script -> {
                    annotation.version.takeIf { it.isNotBlank() }?.also { version = it }
                    annotation.author.takeIf { it.isNotBlank() }?.also { author = it }
                    annotation.website.takeIf { it.isNotBlank() }?.also { website = it }
                    logLevel = annotation.logLevel
                }

                is DependPlugin -> pluginDepends.addAll(annotation.plugin).also { println("Added dependency on plugin ${annotation.plugin[0]}") }
            }
        }

        // Resolving/downloading external dependencies provided with annotations: adding to
        // classpath for compilation and to Description for be usaged in the Classloader.
        val external = resolveExternalDependencies(ctx.script, annotations)
        val dependenciesFiles = external.compiled

        if (dependenciesFiles.isNotEmpty()) {
            updateClasspath(dependenciesFiles)
        }

        info(
            ScriptDescription(
                version,
                author,
                website,
                pluginDepends,
                logLevel,
                dependenciesFiles.map { it.absolutePath }.toSet(),
            ),
        )

        if (external.sources.isNotEmpty()) {
            ide.dependenciesSources.append(JvmDependency(external.sources.toList()))
        }
    }

    return if (reports.isEmpty()) {
        configuration.asSuccess()
    } else {
        makeFailureResult(reports)
    }
}
