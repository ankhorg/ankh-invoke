package org.inksnow.ankhinvoke.classpool;

import org.inksnow.ankhinvoke.asm.AsmUtil;
import org.inksnow.ankhinvoke.comments.InternalName;
import org.inksnow.ankhinvoke.util.DstUnsafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.net.URL;

public class ResourcePoolLoader extends ClassNodePoolLoader {
  private final @NotNull ClassLoader classLoader;

  public ResourcePoolLoader(@NotNull ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  @Override
  protected @Nullable ClassNode provide0(@InternalName @NotNull String className) {
    URL url = classLoader.getResource(className + ".class");
    if (url == null) {
      url = classLoader.getResource(className + ".ankh-invoke.class");
    }
    if (url == null) {
      return null;
    }
    try {
      return AsmUtil.readClass(url, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
    } catch (IOException e) {
      throw DstUnsafe.throwImpl(e);
    }
  }
}
