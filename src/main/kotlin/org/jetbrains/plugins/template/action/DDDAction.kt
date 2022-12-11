package org.jetbrains.plugins.template.action

import com.android.tools.build.jetifier.core.utils.Log
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.process.*
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.InputValidatorEx
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiDirectory
import io.flutter.pub.PubRoot
import io.flutter.sdk.FlutterSdk
import org.jetbrains.plugins.template.utils.Utils.FILE_FOLDER
import org.jetbrains.plugins.template.utils.Utils.FILE_NAME
import java.nio.charset.StandardCharsets
import java.util.concurrent.ExecutionException


class DDDAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project: Project = e.project!!
        val name = Messages.showInputDialog(
            "DDD Folder",
            "Module name",
            null,
            null,
            SimpleClassNameInputValidator()
        )

        if (name.isNullOrBlank()) return

        val desPath = "./${FILE_FOLDER}/${FILE_NAME}"
        val target = e.getData(LangDataKeys.PSI_ELEMENT) as? PsiDirectory
        val projectPath = project.basePath
        val output = target
            ?.virtualFile
            ?.path
            ?.replace("PsiDirectory:", "")
            ?.replace("$projectPath/", "")

        runBackgroundableTask("Creating new module", project, false, task = { show ->
            if (!show.isRunning) show.start()
            val processHandler= createCommand(e, "dart run $desPath -d $name -o $output")
            ProcessTerminatedListener.attach(processHandler!!)
            val console = TextConsoleBuilderFactory.getInstance().createBuilder(project).console
            console.attachToProcess(processHandler)
            target?.virtualFile?.refresh(false, true)
            runFlutterCommand(project, onDone = {show.stop()})
        })
    }

    private fun runFlutterCommand(
        project: Project,
        command: String = "run build_runner build --delete-conflicting-outputs",
        onDone:(ProcessOutput)->Unit,
    ) {
        val result = FlutterSdk.getFlutterSdk(project)?.flutterPub(PubRoot.forDirectory(project.baseDir!!), *command.split(" ").toTypedArray())
        result?.start({ t ->
            onDone.invoke(t)
            Log.e("tag", " ${t.stderrLines} ${t.stdoutLines}") }, null)
    }

    private fun createCommand(e: AnActionEvent, messages: String): ProcessHandler? {
        val project: Project = e.project!!
        val cmds = messages.split(" ").toTypedArray()
        val commandLine = GeneralCommandLine(*cmds).also {
            it.charset = StandardCharsets.UTF_8
            it.setWorkDirectory(project.basePath)
        }
        var processHandler: ProcessHandler? = null
        try {
            processHandler = OSProcessHandler(commandLine)
        } catch (exception: ExecutionException) {
            exception.printStackTrace()
        }
        assert(processHandler != null) { "Process Handler should not be null" }
        processHandler?.startNotify()
        processHandler?.waitFor()
        return processHandler
    }
}

class SimpleClassNameInputValidator : InputValidatorEx {
    override fun checkInput(input: String): Boolean {
        return getErrorText(input) == null
    }

    override fun canClose(p0: String?): Boolean {
        return true
    }

    override fun getErrorText(input: String): String? {
        if (input.contains(".") || input.contains(" ") || input.lowercase() != input) {
            return "Must only contain lowercase characters and underscores"
        }
        return null
    }
}