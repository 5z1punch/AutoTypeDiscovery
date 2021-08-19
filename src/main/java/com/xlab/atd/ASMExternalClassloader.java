package com.xlab.atd;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.net.URL;
import java.net.URLClassLoader;

// TODO
// 反射解决不了运行时类的问题，还是要靠asm

public class ASMExternalClassloader extends URLClassLoader {
    public ASMExternalClassloader(URL[] urls) {
        super(urls);
    }
    private byte[] createClassBinary(String classdesc) {
        ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, classdesc, null, "java/lang/Object", null);
        {
            // constructor
            MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitMaxs(2, 1);
            mv.visitVarInsn(Opcodes.ALOAD, 0); // push `this` to the operand stack
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(Object.class), "<init>", "()V"); // call the constructor of super class
            mv.visitInsn(Opcodes.RETURN);
            mv.visitEnd();
        }
        cw.visitEnd();
        return cw.toByteArray();
    }
    @Override
    public final Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException
    {
        Class<?> clazz;
        try {
            clazz = super.loadClass(name, resolve);
        }
        catch (NoClassDefFoundError | TypeNotPresentException ncdfe){
            byte[] b = createClassBinary(name);
            clazz = defineClass(name, b, 0, b.length);
        }
        return clazz;
    }
    @Override
    public final Class<?> findClass(final String name)
            throws ClassNotFoundException
    {
        Class<?> clazz;
        try {
            clazz = super.findClass(name);
        }
        catch (NoClassDefFoundError | TypeNotPresentException ncdfe){
            byte[] b = createClassBinary(name);
            clazz = defineClass(name, b, 0, b.length);
        }
        return clazz;
    }
}
