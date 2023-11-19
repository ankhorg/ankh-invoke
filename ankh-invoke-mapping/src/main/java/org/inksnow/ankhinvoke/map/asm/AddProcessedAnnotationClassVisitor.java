package org.inksnow.ankhinvoke.map.asm;

import org.inksnow.ankhinvoke.codec.util.TimeUtil;
import org.inksnow.ankhinvoke.map.AnkhInvokeMapping;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Collections;
import java.util.List;

public class AddProcessedAnnotationClassVisitor extends ClassVisitor {
  private final @NotNull List<@NotNull String> by;
  public AddProcessedAnnotationClassVisitor(@NotNull String by, @NotNull ClassVisitor classVisitor) {
    super(Opcodes.ASM9, classVisitor);
    this.by = Collections.singletonList(by);
  }

  @Override
  public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
    super.visit(version, access, name, signature, superName, interfaces);
    AnnotationVisitor av = cv.visitAnnotation("L" + AnkhInvokeMapping.ANKH_INVOKE_PACKAGE.replace('.', '/') + "/comments/AnkhInvokeProcessed;", false);
    av.visit("time", TimeUtil.logTime());
    AnnotationVisitor arrayVisitor = av.visitArray("by");
    for (String byLine : by) {
      arrayVisitor.visit(null, byLine);
    }
    arrayVisitor.visitEnd();
    av.visitEnd();
  }
}
