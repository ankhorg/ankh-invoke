package org.inksnow.ankhinvoke.classpool;

import org.inksnow.ankhinvoke.comments.InternalName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.ClassNode;

public abstract class ClassNodePoolLoader implements ClassPoolLoader {
  protected abstract @Nullable ClassNode provide0(@InternalName @NotNull String className);

  @Override
  public final @Nullable ClassPoolNode provide(@InternalName @NotNull String className) {
    ClassNode classNode = provide0(className);
    if (classNode == null) {
      return null;
    }
    return ClassPoolNode.fromClassNode(classNode);
  }
}
