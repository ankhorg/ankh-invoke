package org.inksnow.ankhinvoke.asm;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;

public class AddProcessedAnnotationProcessor implements ClassNodeProcessor {
  @Override
  public @NotNull ClassNode process(@NotNull ClassNode classNode) {
    if (classNode.visibleAnnotations == null) {
      classNode.visibleAnnotations = new ArrayList<>();
    }
    classNode.visibleAnnotations.add(0, AsmUtil.createProcessedAnnotation("ankh-invoke"));
    return classNode;
  }
}
