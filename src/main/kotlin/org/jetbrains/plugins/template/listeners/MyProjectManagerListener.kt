package org.jetbrains.plugins.template.listeners


import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.startup.StartupActivity
import io.flutter.sdk.FlutterSdkUtil
import org.jetbrains.plugins.template.services.MyProjectService


internal class OpenStartUp : StartupActivity {

    override fun runActivity(project: Project) {

    }
}

internal class MyProjectManagerListener : ProjectManagerListener {
    override fun projectOpened(project: Project) {
        project.service<MyProjectService>().updateMason()
    }
}
