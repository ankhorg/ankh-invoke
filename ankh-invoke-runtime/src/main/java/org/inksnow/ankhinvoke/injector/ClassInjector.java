package org.inksnow.ankhinvoke.injector;

import org.inksnow.ankhinvoke.comments.InternalName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.ProtectionDomain;

public interface ClassInjector {
  @NotNull Class<?> inject(@InternalName @NotNull String name, byte @NotNull [] bytes, @Nullable ProtectionDomain protectionDomain);
}
