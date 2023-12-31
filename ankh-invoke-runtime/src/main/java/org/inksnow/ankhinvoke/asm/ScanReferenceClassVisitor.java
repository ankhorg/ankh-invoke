package org.inksnow.ankhinvoke.asm;

import org.inksnow.ankhinvoke.ReferenceService;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;

public final class ScanReferenceClassVisitor extends ClassRemapper {
  public ScanReferenceClassVisitor(@NotNull ReferenceService referenceService) {
    super(Opcodes.ASM9, new ClassNode(), new ScanRemapper(referenceService));
  }

  private static final class ScanRemapper extends Remapper {
    private final @NotNull ReferenceService referenceService;

    public ScanRemapper(@NotNull ReferenceService referenceService) {
      this.referenceService = referenceService;
    }

    @Override
    public String map(String internalName) {
      if (!referenceService.get(internalName).isEmpty()) {
        throw new FoundedException();
      }
      return internalName;
    }
  }

  public static final class FoundedException extends RuntimeException {
    public FoundedException() {
      super("found", null, false, false);
    }
  }
}
