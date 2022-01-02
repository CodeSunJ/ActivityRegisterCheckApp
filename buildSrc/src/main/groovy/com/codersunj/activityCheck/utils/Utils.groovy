package com.codersunj.activityCheck.utils

import org.gradle.api.Project

class Utils {

    static def getTransformClassesWithCheckActivityRegisterTransformTask(Project project, String variantName) {
        return project.tasks.getByName("transformClassesWithCheckActivityRegisterTransformFor${variantName}")
    }

    static def getPackageTask(Project project, String variantName) {
        return project.tasks.getByName("package${variantName}")
    }

    static def getProcessManifestTask(Project project, String variantName) {
        return project.tasks.getByName("process${variantName}Manifest")
    }
}