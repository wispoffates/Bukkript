package br.com.devsrsouza.bukkript.plugin

import br.com.devsrsouza.bukkript.script.definition.BukkriptScript
import br.com.devsrsouza.bukkript.script.host.loader.BukkriptLoadedScript

internal fun BukkriptLoadedScript.disable() {
    script.disable()
}

internal fun BukkriptScript.disable() {
    println("Called script disable. Count=${onDisableListeners.size} Values=${onDisableListeners.joinToString(",")}")
    onDisableListeners.forEach { it() }
}
