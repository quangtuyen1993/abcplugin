package org.jetbrains.plugins.template.utils

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.project.Project
import kotlinx.coroutines.coroutineScope
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets


class Utils {
    companion object {
        suspend fun createCommand(
            project: Project,
            messages: String,
            onDone: () -> Unit = {}
        ) = coroutineScope {
            val cmds = messages.split(" ").toTypedArray()
            val commandLine = GeneralCommandLine(*cmds).also {
                it.charset = StandardCharsets.UTF_8
                it.setWorkDirectory(project.basePath)
            }
            val process = commandLine.createProcess()
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            try {
                while (reader.readLine().also { line = it } != null) {
                    println(line)
                }
                onDone.invoke()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            process.waitFor()
            return@coroutineScope
        }

    }
}
