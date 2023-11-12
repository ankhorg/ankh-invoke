package org.inksnow.ankhinvoke.asm;

import org.inksnow.ankhinvoke.codec.util.TimeUtil;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

public class AddProcessedAnnotationClassVisitor extends ClassVisitor {
  public AddProcessedAnnotationClassVisitor(@NotNull ClassVisitor cv) {
    super(Opcodes.ASM9, cv);
  }

  @Override
  public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
    super.visit(version, access, name, signature, superName, interfaces);

    AnnotationVisitor av = cv.visitAnnotation("Lorg/inksnow/ankhinvoke/comments/AnkhInvokeProcessed;", false);
    av.visit("time", TimeUtil.logTime());
    av.visit("by", "ankh-invoke");
    av.visitEnd();
  }
}
