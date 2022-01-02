package com.codersunj.activityCheck.collector

import org.objectweb.asm.*
import org.objectweb.asm.tree.ClassNode

import java.util.jar.JarEntry
import java.util.jar.JarFile

class ActivityCollector {

    Map<String, String> objectSuperMap = new HashMap<>()
    Set<String> activitySet = new HashSet<>()
    Set<String> systemActivitySet = new HashSet<>()

    ActivityCollector() {
        systemActivitySet.add("androidx/appcompat/app/AppCompatActivity")
        systemActivitySet.add("androidx/fragment/app/FragmentActivity")
        systemActivitySet.add("androidx/core/app/ComponentActivity")
        systemActivitySet.add("android/app/Activity")
    }

    def setActivitySet(Set<String> set) {
        set.addAll(activitySet)
    }

    def collectFromDirectoryToMap(File file) {
        if (file == null || !file.exists()) {
            return
        }
        if (file.isFile()) {
            ClassReader cr = new ClassReader(file.getBytes())
            ClassNode cn = new ClassNode()
            cr.accept(cn, ClassReader.SKIP_DEBUG | ClassReader.SKIP_CODE)
            objectSuperMap.put(cn.name, cn.superName)
        } else {
            file.listFiles().each {
                collectFromDirectoryToMap(it)
            }
        }
    }

    def getActivityClzFromDirectory(File file) {
        if (file == null || !file.exists()) {
            return
        }
        if (file.isFile()) {
            ClassReader cr = new ClassReader(file.getBytes())
            ClassNode cn = new ClassNode()
            cr.accept(cn, ClassReader.SKIP_DEBUG | ClassReader.SKIP_CODE)
            if (isAbstract(cn.access) || isSystemActivityClass(cn.name)) {
                return
            }
            collectActivity(cn.name, cn.name)
        } else {
            file.listFiles().each {
                getActivityClzFromDirectory(it)
            }
        }
    }

    def collectFromJarToMap(File file) {
        if (file == null || !file.exists()) {
            return
        }
        JarFile jarFile = new JarFile(file)
        Enumeration enumeration = jarFile.entries()
        while (enumeration.hasMoreElements()) {
            JarEntry jarEntry = (JarEntry) enumeration.nextElement()
            if (jarEntry.name.endsWith(".class")) {
                ClassReader cr = new ClassReader(jarFile.getInputStream(jarEntry))
                ClassNode cn = new ClassNode()
                cr.accept(cn, ClassReader.SKIP_DEBUG | ClassReader.SKIP_CODE)
                objectSuperMap.put(cn.name, cn.superName)
            }
        }
    }

    def getActivityClzFromJar(File file) {
        if (file == null || !file.exists()) {
            return
        }
        JarFile jarFile = new JarFile(file)
        Enumeration enumeration = jarFile.entries()
        while (enumeration.hasMoreElements()) {
            JarEntry jarEntry = (JarEntry) enumeration.nextElement()
            if (jarEntry.name.endsWith(".class")) {
                ClassReader cr = new ClassReader(jarFile.getInputStream(jarEntry))
                ClassNode cn = new ClassNode()
                cr.accept(cn, ClassReader.SKIP_DEBUG | ClassReader.SKIP_CODE)
                if (isAbstract(cn.access) || isSystemActivityClass(cn.name)) {

                } else {
                    collectActivity(cn.name, cn.name)
                }
            }
        }
    }

    def collectActivity(String clzName, String name) {
        while (!isObject(name)) {
            if (isSystemActivityClass(name)) {
                activitySet.add(clzName)
                return
            } else {
                name = objectSuperMap.get(name)
            }
        }
    }

    boolean isAbstract(int access) {
        String accessString = Integer.toBinaryString(access)
        if (accessString != null && accessString.length() > 10 && accessString[10] == '1') {
            return true
        }
        return false
    }

    boolean isObject(String objStr) {
        if (objStr == null || objStr.length() == 0) {
            return true
        }
        return "java/lang/Object".equals(objStr)
    }

    boolean isSystemActivityClass(String activityStr) {
        return systemActivitySet.contains(activityStr)
    }

    @Override
    String toString() {
        return activitySet.toListString()
    }
}