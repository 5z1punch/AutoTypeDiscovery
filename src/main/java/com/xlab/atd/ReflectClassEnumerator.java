package com.xlab.atd;

import com.google.common.reflect.ClassPath;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class ReflectClassEnumerator {
    private final ClassLoader classLoader;

    public ReflectClassEnumerator(ClassLoader classLoader) throws IOException {
        this.classLoader = classLoader;
    }

    public List<Class> getAllClasses() throws IOException {
        List<Class> result = new ArrayList<>(getRuntimeClasses());
        for (ClassPath.ClassInfo classInfo : ClassPath.from(classLoader).getAllClasses()) {
            try {
                result.add(classInfo.load());
            }
            catch (Throwable e){
                // 包里某些类和当前runtime的java版本不一样
            }
        }
        return result;
    }

    public List<Class> getRuntimeClasses() throws IOException {
        // A hacky way to get the current JRE's rt.jar. Depending on the class loader, rt.jar may be in the
        // bootstrap classloader so all the JDK classes will be excluded from classpath scanning with this!
        // However, this only works up to Java 8, since after that Java uses some crazy module magic.
        URL stringClassUrl = Object.class.getResource("String.class");
        URLConnection connection = stringClassUrl.openConnection();
        List<Class> result = new ArrayList<>();
        if (connection instanceof JarURLConnection) {
            URL runtimeUrl = ((JarURLConnection) connection).getJarFileURL();
            URLClassLoader classLoader = new URLClassLoader(new URL[]{runtimeUrl});
            for (ClassPath.ClassInfo classInfo : ClassPath.from(classLoader).getAllClasses()) {
                try {
                    result.add(classInfo.load());
                }
                catch (Throwable e){
                    // 包里某些类和当前runtime的java版本不一样
                }
            }
            return result;
        }
        else{
            throw new IOException("getRuntimeClasses trick got an error, maybe current jdk is not jdk8");
        }
    }
}

