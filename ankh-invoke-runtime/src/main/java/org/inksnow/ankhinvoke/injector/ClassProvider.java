package org.inksnow.ankhinvoke.injector;

import org.inksnow.ankhinvoke.comments.InternalName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ClassProvider {
  byte @Nullable [] provide(@InternalName @NotNull String name);
}
