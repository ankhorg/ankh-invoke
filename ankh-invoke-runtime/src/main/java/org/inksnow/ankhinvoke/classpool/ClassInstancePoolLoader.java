package org.inksnow.ankhinvoke.classpool;

import org.inksnow.ankhinvoke.comments.InternalName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ClassInstancePoolLoader implements ClassPoolLoader {
  protected abstract @Nullable Class<?> provide0(@InternalName @NotNull String className);

  @Override
  public @Nullable ClassPoolNode provide(@InternalName @NotNull String className) {
    Class<?> clazz = provide0(className);
    if (clazz == null) {
      return null;
    }
    return ClassPoolNode.fromClassNode(clazz);
  }
}
