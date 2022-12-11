package org.jetbrains.plugins.template.action

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import io.flutter.sdk.FlutterSdkUtil


class DefaultDDDAction :DefaultActionGroup() {
    override fun update(e: AnActionEvent) {
        super.update(e)
        val isFlutter = FlutterSdkUtil.hasFlutterModules()
        e.presentation.isEnabled = isFlutter;
    }
}