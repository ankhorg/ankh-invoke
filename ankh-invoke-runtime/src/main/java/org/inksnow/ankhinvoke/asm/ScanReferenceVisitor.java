package org.inksnow.ankhinvoke.asm;

import org.inksnow.ankhinvoke.ReferenceService;
import org.inksnow.ankhinvoke.reference.ReferenceMetadata;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

import java.util.HashSet;
import java.util.Set;

public class ScanReferenceVisitor {
  private boolean usedClassWithReference = false;
  private ScanReferenceRemapper remapper;

  public ScanReferenceVisitor(@NotNull ReferenceService referenceService) {
    remapper = new ScanReferenceRemapper(referenceService);
  }

  public @NotNull ClassVisitor createClassVisitor() {
    return new ClassRemapper(null, remapper);
  }

  public boolean usedClassWithReference() {
    return usedClassWithReference;
  }

  private final class ScanReferenceRemapper extends Remapper {
    private final @NotNull ReferenceService referenceService;

    private ScanReferenceRemapper(@NotNull ReferenceService referenceService) {
      this.referenceService = referenceService;
    }

    @Override
    public String map(String internalName) {
      if (scanIsReference(internalName, new HashSet<>())) {
        usedClassWithReference = true;
      }
      return internalName;
    }

    private boolean scanIsReference(String owner, Set<String> scannedClass) {
      if (!scannedClass.add(owner)) {
        return false;
      }
      ReferenceMetadata referenceMetadata = referenceService.get(owner);
      if (!referenceMetadata.isEmpty()) {
        return true;
      }
      for (String className : referenceMetadata.superClasses()) {
        if (scanIsReference(className, scannedClass)) {
          return true;
        }
      }
      return false;
    }
  }
}
