package org.inksnow.ankhinvoke.asm;

import org.inksnow.ankhinvoke.codec.BlobMap;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.commons.Remapper;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public class BlobRemapper extends Remapper {
  private final @NotNull BlobMap blobMap;

  private BlobRemapper(@NotNull BlobMap blobMap) {
    this.blobMap = blobMap;
  }

  public static @NotNull BlobRemapper load(@NotNull DataInputStream in) throws IOException {
    return new BlobRemapper(BlobMap.load(in));
  }

  @Override
  public @NotNull String map(@NotNull String internalName) {
    String blobResult = blobMap.mapClass(internalName);
    return blobResult == null ? internalName : blobResult;
  }

  @Override
  public @NotNull String mapMethodName(@NotNull String owner, @NotNull String name, @NotNull String descriptor) {
    String blobResult = blobMap.mapMethodName(owner, name, descriptor);
    return blobResult == null ? name : blobResult;
  }

  @Override
  public @NotNull String mapFieldName(@NotNull String owner, @NotNull String name, @NotNull String descriptor) {
    String blobResult = blobMap.mapFieldName(owner, name, descriptor);
    return blobResult == null ? name : blobResult;
  }
}
