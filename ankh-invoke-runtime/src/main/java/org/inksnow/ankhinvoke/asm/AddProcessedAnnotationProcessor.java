package org.inksnow.ankhinvoke.asm;

import org.inksnow.ankhinvoke.codec.util.TimeUtil;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;

public class AddProcessedAnnotationProcessor implements ClassNodeProcessor {
  @Override
  public @NotNull ClassNode process(@NotNull ClassNode classNode) {
    AnnotationNode annotationNode = new AnnotationNode("Lorg/inksnow/ankhinvoke/comments/AnkhInvokeProcessed;");
    annotationNode.values = new ArrayList<>();
    annotationNode.values.add("time");
    annotationNode.values.add(TimeUtil.logTime());
    annotationNode.values.add("by");
    annotationNode.values.add("ankh-invoke");

    if (classNode.visibleAnnotations == null) {
      classNode.visibleAnnotations = new ArrayList<>();
    }
    classNode.visibleAnnotations.add(0, annotationNode);
    return classNode;
  }
}
