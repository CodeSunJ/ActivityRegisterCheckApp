package com.codersunj.activityCheck.transform

import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import com.codersunj.activityCheck.collector.ActivityCollector
import org.apache.commons.io.FileUtils

class CheckActivityRegisterTransform extends Transform {

    Set<String> activitySet = new HashSet<>()

    @Override
    String getName() {
        return "CheckActivityRegisterTransform"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation)
            throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)

        ActivityCollector activityCollector = new ActivityCollector()

        transformInvocation.inputs.each { def input ->
            input.directoryInputs.each { def directoryInput ->
                activityCollector.collectFromDirectoryToMap(directoryInput.file)
            }
            input.jarInputs.each { def jarInput ->
                activityCollector.collectFromJarToMap(jarInput.file)
            }
        }

        transformInvocation.inputs.each { def input ->
            input.directoryInputs.each { def directoryInput ->
                activityCollector.getActivityClzFromDirectory(directoryInput.file)
                def directoryOutput = transformInvocation.outputProvider
                        .getContentLocation(
                                directoryInput.name,
                                directoryInput.contentTypes,
                                directoryInput.scopes,
                                Format.DIRECTORY)
                FileUtils.copyDirectory(directoryInput.file, directoryOutput)
            }
            input.jarInputs.each { def jarInput ->
                activityCollector.getActivityClzFromJar(jarInput.file)
                def jarOutput = transformInvocation.outputProvider
                        .getContentLocation(
                                jarInput.name,
                                jarInput.contentTypes,
                                jarInput.scopes,
                                Format.JAR)
                FileUtils.copyFile(jarInput.file, jarOutput)
            }
        }

        activityCollector.setActivitySet(activitySet)
    }

}