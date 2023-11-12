package org.inksnow.ankhinvoke.classpool;

import org.inksnow.ankhinvoke.AnkhInvoke;
import org.inksnow.ankhinvoke.comments.InternalName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassLoaderPoolLoader extends ClassInstancePoolLoader {
  private static final Logger logger = LoggerFactory.getLogger(AnkhInvoke.ANKH_INVOKE_PACKAGE);
  private final @NotNull ClassLoader classLoader;

  public ClassLoaderPoolLoader(@NotNull ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  @Override
  protected @Nullable Class<?> provide0(@InternalName @NotNull String className) {
    if (AnkhInvoke.DEBUG) {
      logger.debug("classloader pool provide " + className);
    }
    try {
      return Class.forName(Type.getObjectType(className).getClassName(), false, classLoader);
    } catch (ClassNotFoundException e) {
      return null;
    }
  }
}
