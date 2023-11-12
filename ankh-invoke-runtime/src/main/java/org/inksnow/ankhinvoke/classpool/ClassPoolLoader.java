package org.inksnow.ankhinvoke.classpool;

import org.inksnow.ankhinvoke.comments.InternalName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ClassPoolLoader {
  @Nullable ClassPoolNode provide(@InternalName @NotNull String className);
}
