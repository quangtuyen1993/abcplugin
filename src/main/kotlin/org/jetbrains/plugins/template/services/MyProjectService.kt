package org.jetbrains.plugins.template.services

import com.intellij.openapi.project.Project
import io.flutter.sdk.FlutterSdkUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.jetbrains.plugins.template.MyBundle
import org.jetbrains.plugins.template.utils.Utils

class MyProjectService(private val project: Project) {
    private var job: Job? = null

    init {
        println(MyBundle.message("projectService", project.name))
    }

    fun updateMason() {
        val isFlutter = FlutterSdkUtil.hasFlutterModules()
        if (!isFlutter) return
        job?.cancel()
        job = CoroutineScope(Dispatchers.IO).launch {
            Utils.createCommand(project, "mason add ddd")
        }
    }
}

