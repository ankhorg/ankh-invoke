package org.inksnow.ankhinvoke.classpool;

import bot.inker.acj.JvmHacker;
import org.inksnow.ankhinvoke.AnkhInvoke;
import org.inksnow.ankhinvoke.comments.InternalName;
import org.inksnow.ankhinvoke.util.DstUnsafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

public class LoadedClassPoolLoader extends ClassInstancePoolLoader {
  private static final Logger logger = LoggerFactory.getLogger(LoadedClassPoolLoader.class);
  private static final @NotNull MethodHandle findLoadedMethodHandle = bootstrapFindLoadedMethodHandle();
  private final @NotNull ClassLoader classLoader;

  public LoadedClassPoolLoader(@NotNull ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  private static @NotNull MethodHandle bootstrapFindLoadedMethodHandle() {
    try {
      return JvmHacker.lookup().findVirtual(ClassLoader.class, "findLoadedClass", MethodType.methodType(Class.class, String.class));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw DstUnsafe.throwImpl(e);
    }
  }

  @Override
  protected @Nullable Class<?> provide0(@InternalName @NotNull String className) {
    if (AnkhInvoke.DEBUG) {
      logger.debug("classloader pool provide " + className);
    }
    try {
      return (Class<?>) findLoadedMethodHandle.invokeExact(classLoader, className);
    } catch (Throwable e) {
      throw DstUnsafe.throwImpl(e);
    }
  }
}
