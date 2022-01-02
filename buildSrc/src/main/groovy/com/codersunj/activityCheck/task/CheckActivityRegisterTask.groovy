package com.codersunj.activityCheck.task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class CheckActivityRegisterTask extends DefaultTask {

    File manifestDir
    Set<String> activityNotRegisteredSet = new HashSet<>()
    Set<String> activityHasRemovedSet = new HashSet<>()
    Set<String> activitySet = new HashSet<>()
    String activityCheckDir

    CheckActivityRegisterTask() {
        group = 'checkActivity'
    }

    @TaskAction
    def checkActivity() {
        if (manifestDir == null || !manifestDir.exists()) {
            return
        }
        File manifestFile = new File(manifestDir, "AndroidManifest.xml")
        if (!manifestFile.exists()) {
            return
        }

        XmlParser xmlParser = new XmlParser()
        def manifestXml = xmlParser.parse(manifestFile)

        manifestXml.depthFirst().each { Node node ->
            if (node.name().equals("activity")) {
                def keyActivityName = node.attributes().keySet().find { def key ->
                    return key.toString().endsWith("name")
                }
                String activityName = node.attribute(keyActivityName)
                if (activityName != null) {
                    activityName = activityName.replace(".", "/")
                    if (activitySet.contains(activityName)) {
                        activitySet.remove(activityName)
                    } else {
                        activityHasRemovedSet.add(activityName)
                    }
                }
            }
        }
        activityNotRegisteredSet.addAll(activitySet)

        File dirFile = new File(activityCheckDir)
        if (!dirFile.exists()) {
            dirFile.mkdirs()
        }
        File activityCheckFile = new File(dirFile, "activityCheck.txt")
        if (activityCheckFile.exists()) {
            activityCheckFile.delete()
        }
        activityCheckFile.withWriter { def writer ->
            writer.write("未注册的Activity\n")
            activityNotRegisteredSet.each {
                writer.write(" ${it}\n")
            }
            writer.write("\n")

            writer.write("已删除仍注册的Activity\n")
            activityHasRemovedSet.each {
                writer.write(" ${it}\n")
            }
            writer.write("\n")
        }
    }
}