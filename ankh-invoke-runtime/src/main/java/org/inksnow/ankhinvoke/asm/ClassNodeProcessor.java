package org.inksnow.ankhinvoke.asm;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;

public interface ClassNodeProcessor {
  @NotNull ClassNode process(@NotNull ClassNode classNode);
}
