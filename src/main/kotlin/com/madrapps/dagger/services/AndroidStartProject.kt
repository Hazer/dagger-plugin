package com.madrapps.dagger.services

import com.android.tools.idea.gradle.project.build.GradleBuildContext
import com.android.tools.idea.project.AndroidProjectBuildNotifications
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity


class AndroidStartProject : StartupActivity {

    override fun runActivity(project: Project) {
        project.log("AndroidStartProject", "Android project opened")
        project.service

        AndroidProjectBuildNotifications.subscribe(project) { context ->
            project.log("AndroidStartProject", context::class.java.toString())
            if (context is GradleBuildContext) {
                val build = context.buildResult
                project.log("AndroidStartProject", "Build - $build")
                if (build.isBuildSuccessful) {
                    Thread{
                        project.log("AndroidStartProject", "Sleeping...")
                        Thread.sleep(5000)
                        ApplicationManager.getApplication().runReadAction {
                            project.log("AndroidStartProject", "Build Successful")
                            project.service.process(project)
                        }
                    }.start()
                }
            }
        }
    }
}