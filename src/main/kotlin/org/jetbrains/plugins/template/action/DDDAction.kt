package org.jetbrains.plugins.template.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.InputValidatorEx
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiDirectory
import io.flutter.NotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.plugins.template.utils.Utils
import java.nio.file.Path


class DDDAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project: Project = e.project!!
        val name = Messages.showInputDialog(
            "Make DDD Template Folder",
            "Module name",
            null,
            null,
            SimpleClassNameInputValidator()
        )
        if (name.isNullOrBlank()) return
        val target = e.getData(LangDataKeys.PSI_ELEMENT) as? PsiDirectory
        val output = target?.virtualFile?.canonicalPath ?: ""
        runCommand(project, "mason make ddd --name $name -o $output") {
            val file = VfsUtil.findFile(Path.of(output), true)
            if (file != null) {
                VfsUtil.markDirtyAndRefresh(true, true, true, *arrayOf(file))
                NotificationManager.showInfo("Manson", "success", null, true)
            } else {
                NotificationManager.showError("Mason", "create file failed", null, true)
            }
        }

    }

    private fun runCommand(project: Project, command: String, onDone: () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            Utils.createCommand(project, command) {
                onDone.invoke()
            }
        }
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
        if (input.matches(Regex("[a-z_]*"))) {
            return null
        }
        return "Must only contain lowercase characters and underscores"

    }
}

