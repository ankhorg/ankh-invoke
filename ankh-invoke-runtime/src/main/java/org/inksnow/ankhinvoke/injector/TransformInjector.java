package org.inksnow.ankhinvoke.injector;

import org.inksnow.ankhinvoke.AnkhInvoke;
import org.jetbrains.annotations.NotNull;

public interface TransformInjector extends ClassInjector {
  void registerHandle(@NotNull AnkhInvoke ankhInvoke);
}
