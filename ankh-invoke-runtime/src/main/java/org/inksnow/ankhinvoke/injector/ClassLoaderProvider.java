package org.inksnow.ankhinvoke.injector;

import bot.inker.acj.JvmHacker;
import org.inksnow.ankhinvoke.util.DstUnsafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.net.URL;

public class ClassLoaderProvider implements UrlClassProvider {
  private static final @NotNull MethodHandle FIND_RESOURCE_HANDLE = findFindResourceHandle();
  private final @NotNull MethodHandle classLoaderFindResourceHandle;

  public ClassLoaderProvider(@NotNull ClassLoader classLoader) {
    classLoaderFindResourceHandle = FIND_RESOURCE_HANDLE.bindTo(classLoader);
  }

  private static @NotNull MethodHandle findFindResourceHandle() {
    try {
      return JvmHacker.lookup().findVirtual(ClassLoader.class, "findResource", MethodType.methodType(URL.class, String.class));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw DstUnsafe.throwImpl(e);
    }
  }

  private @Nullable URL classLoaderFindResource(@NotNull String name) {
    try {
      return (URL) classLoaderFindResourceHandle.invokeExact(name);
    } catch (Throwable e) {
      throw DstUnsafe.throwImpl(e);
    }
  }

  @Override
  public @Nullable URL provideUrl(@NotNull String name) {
    URL url = classLoaderFindResource(name + ".class");
    if (url == null) {
      url = classLoaderFindResource(name + ".ankh-invoke.class");
    }
    return url;
  }
}
