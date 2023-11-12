package org.inksnow.ankhinvoke.map.asm;

import org.inksnow.ankhinvoke.codec.BlobReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Map;

public class CutHandleByVisitor {
  private final @NotNull Map<@NotNull String, @NotNull BlobReference> blobReferenceMap;
  private BlobReference reference;
  private boolean processed = false;

  public CutHandleByVisitor(@NotNull Map<@NotNull String, @NotNull BlobReference> blobReferenceMap) {
    this.blobReferenceMap = blobReferenceMap;
  }

  public @NotNull ClassVisitor createClassVisitor(@NotNull ClassVisitor classVisitor) {
    return new ClassVisitorImpl(classVisitor);
  }

  public boolean isReferenceClass() {
    return reference != null;
  }

  public boolean getProcessed() {
    return processed;
  }

  public @Nullable BlobReference getReference() {
    return reference;
  }

  private class ClassVisitorImpl extends ClassVisitor {
    private ClassVisitorImpl(ClassVisitor classVisitor) {
      super(Opcodes.ASM9, classVisitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
      reference = blobReferenceMap.get(name);
      if (reference != null && !reference.handles().isEmpty()) {
        processed = true;
      }
      super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
      if(reference != null && reference.methodMap().get(name + desc) != null) {
        processed = false;
        return null;
      }
      return super.visitMethod(access, name, desc, signature, exceptions);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
      if(reference != null && reference.fieldMap().get(name + ":" + desc) != null) {
        processed = false;
        return null;
      }
      return super.visitField(access, name, desc, signature, value);
    }
  }
}
