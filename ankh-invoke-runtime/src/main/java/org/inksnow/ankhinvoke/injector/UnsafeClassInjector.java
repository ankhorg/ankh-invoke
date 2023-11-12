package org.inksnow.ankhinvoke.injector;

import org.inksnow.ankhinvoke.comments.InternalName;
import org.inksnow.ankhinvoke.util.DstUnsafe;
import org.inksnow.ankhinvoke.util.SpyHandle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.ProtectionDomain;

public class UnsafeClassInjector implements ClassInjector {
  private final @NotNull ClassLoader classLoader;
  private volatile boolean spyInjected;

  public UnsafeClassInjector(@NotNull ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  private void ensureSpyInjected() {
    if (!spyInjected) {
      synchronized (this) {
        if (!spyInjected) {
          SpyHandle.injectSpy(classLoader);
          spyInjected = true;
        }
      }
    }
  }

  @Override
  public @NotNull Class<?> inject(@InternalName @NotNull String name, byte @NotNull [] bytes, @Nullable ProtectionDomain protectionDomain) {
    ensureSpyInjected();
    return DstUnsafe.defineClass(name.replace('/', '.'), bytes, 0, bytes.length, classLoader, protectionDomain);
  }
}
