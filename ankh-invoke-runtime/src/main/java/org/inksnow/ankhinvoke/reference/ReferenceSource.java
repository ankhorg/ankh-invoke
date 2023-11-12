package org.inksnow.ankhinvoke.reference;

import org.inksnow.ankhinvoke.comments.InternalName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ReferenceSource {
  @Nullable ReferenceMetadata load(@InternalName @NotNull String owner);
}
