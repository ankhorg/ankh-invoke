package org.inksnow.ankhinvoke.asm;

import org.inksnow.ankhinvoke.comments.InternalName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.util.function.Consumer;

public class ScannerInnerClassVisitor extends ClassVisitor {
  private final @NotNull Consumer<@InternalName @NotNull String> callback;
  private @Nullable String outerName;

  public ScannerInnerClassVisitor(@NotNull Consumer<@InternalName @NotNull String> callback, @NotNull ClassVisitor classVisitor) {
    super(Opcodes.ASM9, classVisitor);
    this.callback = callback;
  }

  @Override
  public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
    this.outerName = name;
    super.visit(version, access, name, signature, superName, interfaces);
  }

  @Override
  public void visitInnerClass(String name, String outerName, String innerName, int access) {
    if (this.outerName == null) {
      throw new IllegalStateException("visitInnerClass called before visit");
    }
    if (this.outerName.equals(outerName)) {
      callback.accept(name);
    }
    super.visitInnerClass(name, outerName, innerName, access);
  }

  @Override
  public void visitEnd() {
    this.outerName = null;
    super.visitEnd();
  }
}
