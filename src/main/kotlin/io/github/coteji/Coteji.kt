/*
 *    Copyright (c) 2020 - 2021 Coteji AUTHORS.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package io.github.coteji

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import io.github.coteji.config.ConfigCotejiScript
import io.github.coteji.core.Coteji
import io.github.coteji.core.IdUpdateMode
import io.github.coteji.exceptions.TestSourceException
import io.github.coteji.model.SyncResult
import io.github.coteji.utils.printWith
import java.io.File
import javax.script.ScriptException
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.implicitReceivers
import kotlin.script.experimental.api.onFailure
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate

val coteji: Coteji by lazy { Coteji() }

class CotejiCommand : CliktCommand() {
    val configFilePath by option(
        "--config",
        help = "path to the script configuration (*.coteji.kts) file. " +
                "Default value: 'config.coteji.kts' (current directory)"
    ).default("config.coteji.kts")

    override fun run() {
        val source = File(configFilePath).toScriptSource()
        val configuration = createJvmCompilationConfigurationFromTemplate<ConfigCotejiScript>()

        BasicJvmScriptingHost().eval(source, configuration, ScriptEvaluationConfiguration {
            implicitReceivers(coteji)
        }).onFailure { result ->
            result.reports.subList(0, result.reports.size - 1).forEach { echo(it) }
            val error = result.reports.last()
            val location = error.location?.start
            throw ScriptException("${error.message} (${error.sourcePath}:${location?.line}:${location?.col})")
        }
    }
}

class SyncAll : CliktCommand(
    help = "Pushes all the tests found in the Source, to the Target. " +
            "Deletes all the tests in the Target that are not present in the Source (match by id)."
) {
    val force by option(
        "--force",
        "-f",
        help = "if provided, all the tests found both in Source and Target (by ID), will be updated from the Source"
    ).flag()
    val idUpdateModeStr by option(
        "--mode",
        "--id-update-mode",
        help = "defines what to do with tests without IDs in the Source: " +
                "update (default) - adds the generated ID to the test; " +
                "warning - does nothing, but prints a warning message for each such test; " +
                "error - fails the run, if any such test is found."
    )
        .choice("update", "warning", "error").default("update")

    override fun run() {
        val idUpdateMode = IdUpdateMode.valueOf(idUpdateModeStr.uppercase())
        val result = coteji.syncAll(force, idUpdateMode == IdUpdateMode.UPDATE)
        handleIdUpdateMode(idUpdateMode, result)
        echo(result)
    }
}

class SyncOnly : CliktCommand(help = "Pushes selected by QUERY tests in the Source, to the Target") {
    val query by argument(help = "query to filter the tests in the Source")
    val force by option(
        "--force",
        "-f",
        help = "if provided, all the tests found both in Source and Target (by ID), will be updated from the Source"
    ).flag()
    val idUpdateModeStr by option(
        "--mode",
        "--id-update-mode",
        help = "defines what to do with tests without IDs in the Source: " +
                "update (default) - adds the generated ID to the test; " +
                "warning - does nothing, but prints a warning message for each such test; " +
                "error - fails the run, if any such test is found."
    )
        .choice("update", "warning", "error").default("update")

    override fun run() {
        val idUpdateMode = IdUpdateMode.valueOf(idUpdateModeStr.uppercase())
        val result = coteji.syncOnly(query, force, idUpdateMode == IdUpdateMode.UPDATE)
        handleIdUpdateMode(idUpdateMode, result)
        echo(result)
    }
}

class PushNew : CliktCommand(help = "Finds all tests in the Source without IDs and pushes them to the Target") {
    override fun run() {
        val result = coteji.pushNew()
        echo(result)
    }
}

class DryRun : CliktCommand(
    help = "Emulates the result of syncAll action without actually doing anything, just logs the results to the console."
) {
    val force by option(
        "--force",
        "-f",
        help = "if provided, all the tests found both in Source and Target (by ID), will be updated from the Source"
    ).flag()

    override fun run() {
        val result = coteji.dryRun(force)
        echo(result)
    }
}

class TryQuery : CliktCommand(help = "Prints the list of tests from the Source found by query") {
    val query by argument(help = "query to filter the tests in the Source")

    override fun run() {
        val tests = coteji.tryQuery(query)
        tests.printWith("Found tests:")
    }
}

private fun handleIdUpdateMode(
    idUpdateMode: IdUpdateMode,
    result: SyncResult
) {
    when (idUpdateMode) {
        IdUpdateMode.WARNING -> {
            if (result.testsWithoutId.isNotEmpty()) {
                result.testsWithoutId.printWith("WARN: Tests without ID:")
            }
            if (result.pushResult.testsWithNonExistingId.isNotEmpty()) {
                result.pushResult.testsWithNonExistingId.printWith("WARN: Tests with non-existing ID:")
            }
        }
        IdUpdateMode.ERROR -> {
            if (result.testsWithoutId.isNotEmpty()) {
                result.testsWithoutId.printWith("ERROR: Tests without ID:")
            }
            if (result.pushResult.testsWithNonExistingId.isNotEmpty()) {
                result.pushResult.testsWithNonExistingId.printWith("ERROR: Tests with non-existing ID:")
            }
            if (result.testsWithoutId.isNotEmpty() || result.pushResult.testsWithNonExistingId.isNotEmpty()) {
                throw TestSourceException("There tests without ID or with non-existing ID. See the list above.")
            }
        }
        else -> {
            // it's OK
        }
    }
}

fun main(args: Array<String>) = CotejiCommand()
    .subcommands(SyncAll(), SyncOnly(), PushNew(), DryRun(), TryQuery())
    .main(args)