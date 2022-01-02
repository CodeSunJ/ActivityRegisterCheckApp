package com.codersunj.activityCheck

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.codersunj.activityCheck.extension.ActivityCheckExtension
import com.codersunj.activityCheck.task.CheckActivityRegisterTask
import com.codersunj.activityCheck.transform.CheckActivityRegisterTransform
import com.codersunj.activityCheck.utils.Utils
import org.gradle.api.Plugin
import org.gradle.api.Project

class CheckActivityRegisterPlugin implements Plugin<Project> {

    Project mProject

    @Override
    void apply(Project project) {
        mProject = project

        if (mProject.plugins.hasPlugin(AppPlugin)) {
            AppExtension appExtension = mProject.extensions.getByType(AppExtension)
            CheckActivityRegisterTransform checkActivityRegisterTransform = new CheckActivityRegisterTransform()
            appExtension.registerTransform(checkActivityRegisterTransform)

            mProject.extensions.create("activityCheck", ActivityCheckExtension.class)

            mProject.afterEvaluate {

                ActivityCheckExtension activityCheckExtension = mProject.extensions.findByName("activityCheck")
                def activityCheckDir = mProject.rootProject.projectDir.absolutePath + File.separator + "activityCheck"
                if (activityCheckExtension.dir != null && activityCheckExtension.dir.length() > 0) {
                    activityCheckDir = activityCheckExtension.dir
                }
                def android = mProject.extensions.getByName("android")
                android.applicationVariants.all { def variant ->
                    def variantName = variant.name.capitalize()
                    def processManifestTask = Utils.getProcessManifestTask(mProject, variantName)
                    def transformWithCheckActivityRegisterTask =
                            Utils.getTransformClassesWithCheckActivityRegisterTransformTask(mProject, variantName)
                    def packageTask = Utils.getPackageTask(mProject, variantName)

                    def checkActivityRegisterTask = mProject.tasks.create("checkActivityRegisterFor${variantName}",
                            CheckActivityRegisterTask.class)
                    checkActivityRegisterTask.manifestDir = processManifestTask.getManifestOutputDirectory().get().getAsFile()
                    checkActivityRegisterTask.mustRunAfter(transformWithCheckActivityRegisterTask)
                    packageTask.dependsOn(checkActivityRegisterTask)
                    checkActivityRegisterTask.doFirst {
                        checkActivityRegisterTask.activitySet.addAll(checkActivityRegisterTransform.activitySet)
                    }
                    checkActivityRegisterTask.activityCheckDir = activityCheckDir
                }
            }
        }
    }
}