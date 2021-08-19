package com.xlab.atd;

import org.objectweb.asm.*;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class ASMUtils {
    public static String[] lookupParameterNames(AccessibleObject methodOrCtor) {

        final Class<?>[] types;
        final Class<?> declaringClass;
        final String name;

        if (methodOrCtor instanceof Method) {
            Method method = (Method) methodOrCtor;
            types = method.getParameterTypes();
            name = method.getName();
            declaringClass = method.getDeclaringClass();
        } else {
            Constructor<?> constructor = (Constructor<?>) methodOrCtor;
            types = constructor.getParameterTypes();
            declaringClass = constructor.getDeclaringClass();
            name = "<init>";
        }

        if (types.length == 0) {
            return new String[0];
        }

        ClassLoader classLoader = declaringClass.getClassLoader();
        if (classLoader == null) {
            classLoader = ClassLoader.getSystemClassLoader();
        }

        String className = declaringClass.getName();
        String resourceName = className.replace('.', '/') + ".class";
        InputStream is = classLoader.getResourceAsStream(resourceName);

        if (is == null) {
            return new String[0];
        }

        try {
            ClassReader reader = new ClassReader(is);
            TypeCollector visitor = new TypeCollector(name, types);
            reader.accept(visitor, ClassReader.EXPAND_FRAMES);
            String[] parameterNames = visitor.getParameterNamesForMethod();

            return parameterNames;
        } catch (IOException e) {
            return new String[0];
        } finally {
            close(is);
        }
    }
    public static void close(Closeable x) {
        if (x != null) {
            try {
                x.close();
            } catch (Exception e) {
                // skip
            }
        }
    }
    private static class TypeCollector extends ClassVisitor{
        private final String methodName;

        private final Class<?>[] parameterTypes;
        MethodCollector collector;
        private static final Map<String, String> primitives = new HashMap<String, String>() {
            {
                put("int","I");
                put("boolean","Z");
                put("byte", "B");
                put("char","C");
                put("short","S");
                put("float","F");
                put("long","J");
                put("double","D");
            }
        };
        public TypeCollector(String methodName, Class<?>[] parameterTypes) {
            super(Opcodes.ASM6);
            this.methodName = methodName;
            this.parameterTypes = parameterTypes;
            this.collector = null;
        }
        private boolean correctTypeName(Type type, String paramTypeName) {
            String s = type.getClassName();
            // array notation needs cleanup.
            String braces = "";
            while (s.endsWith("[]")) {
                braces = braces + "[";
                s = s.substring(0, s.length() - 2);
            }
            if (!braces.equals("")) {
                if (primitives.containsKey(s)) {
                    s = braces + primitives.get(s);
                } else {
                    s = braces + "L" + s + ";";
                }
            }
            return s.equals(paramTypeName);
        }
        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions)
        {
            if (collector != null) {
                return null;
            }

            if (!name.equals(methodName)) {
                return null;
            }

            Type[] argTypes = Type.getArgumentTypes(descriptor);
            int longOrDoubleQuantity = 0;
            for (Type t : argTypes) {
                String className = t.getClassName();
                if (className.equals("long") || className.equals("double")) {
                    longOrDoubleQuantity++;
                }
            }

            if (argTypes.length != this.parameterTypes.length) {
                return null;
            }
            for (int i = 0; i < argTypes.length; i++) {
                if (!correctTypeName(argTypes[i], this.parameterTypes[i].getName())) {
                    return null;
                }
            }

            return collector = new MethodCollector(
                    Modifier.isStatic(access) ? 0 : 1,
                    argTypes.length + longOrDoubleQuantity);
        }
        public String[] getParameterNamesForMethod() {
            if (collector == null || !collector.debugInfoPresent) {
                return new String[0];
            }
            return collector.getResult().split(",");
        }
    }
    private static class MethodCollector extends MethodVisitor {
        private final int paramCount;

        private final int ignoreCount;

        private int currentParameter;

        private final StringBuilder result;

        protected boolean debugInfoPresent;

        public MethodCollector(int ignoreCount, int paramCount) {
            super(Opcodes.ASM6);
            this.ignoreCount = ignoreCount;
            this.paramCount = paramCount;
            this.result = new StringBuilder();
            this.currentParameter = 0;
            // if there are 0 parameters, there is no need for debug info
            this.debugInfoPresent = paramCount == 0;
        }
        @Override
        public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index){
            if (index >= ignoreCount && index < ignoreCount + paramCount) {
                if (!name.equals("arg" + currentParameter)) {
                    debugInfoPresent = true;
                }
                result.append(',');
                result.append(name);
                currentParameter++;
            }
        }
        public String getResult() {
            return result.length() != 0 ? result.substring(1) : "";
        }
    }
    public static void main(String[] args) throws Exception {

    }

}
