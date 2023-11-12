package org.inksnow.ankhinvoke.map.asm;

import org.inksnow.ankhinvoke.codec.BlobReference;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

import java.util.Map;

public class ScanReferenceVisitor {
  private boolean usedClassWithReference = false;
  private ScanReferenceRemapper remapper;

  public ScanReferenceVisitor(@NotNull Map<@NotNull String, @NotNull BlobReference> blobReferenceMap) {
    remapper = new ScanReferenceRemapper(blobReferenceMap);
  }

  public @NotNull ClassVisitor createClassVisitor(@NotNull ClassVisitor classVisitor) {
    return new ClassRemapper(classVisitor, remapper);
  }

  public boolean usedClassWithReference() {
    return usedClassWithReference;
  }

  private final class ScanReferenceRemapper extends Remapper {
    private final @NotNull Map<@NotNull String, @NotNull BlobReference> blobReferenceMap;

    private ScanReferenceRemapper(@NotNull Map<@NotNull String, @NotNull BlobReference> blobReferenceMap) {
      this.blobReferenceMap = blobReferenceMap;
    }

    @Override
    public String map(String internalName) {
      BlobReference blobReference = blobReferenceMap.get(internalName);
      if(blobReference != null) {
        usedClassWithReference = true;
      }
      return internalName;
    }
  }
}
