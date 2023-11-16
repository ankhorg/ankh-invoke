package org.inksnow.ankhinvoke.asm;

import org.inksnow.ankhinvoke.comments.InternalName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InnerClassNode;

import java.util.function.Consumer;

public class ScannerInnerProcessor implements ClassNodeProcessor {
  private final @NotNull Consumer<@InternalName @NotNull String> callback;
  private @Nullable String outerName;

  public ScannerInnerProcessor(@NotNull Consumer<@InternalName @NotNull String> callback) {
    this.callback = callback;
  }

  @Override
  public @NotNull ClassNode process(@NotNull ClassNode classNode) {
    if (classNode.innerClasses != null) {
      for (InnerClassNode innerClass : classNode.innerClasses) {
        if (classNode.name.equals(innerClass.outerName)) {
          callback.accept(innerClass.name);
        }
      }
    }
    return classNode;
  }
}
