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

package io.github.coteji.utils

import com.github.ajalt.clikt.output.TermUi.echo
import io.github.coteji.model.CotejiTest

fun List<CotejiTest>.printWith(message: String) {
    echo("\n$message")
    this.indices.forEach { i ->
        val index = "${i + 1} ".padEnd(7, '-')
        echo("\n$index-----------------------------------------------------------")
        echo(this[i])
    }
}