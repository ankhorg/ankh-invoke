package org.inksnow.ankhinvoke.asm;

import org.inksnow.ankhinvoke.AnkhInvoke;
import org.inksnow.ankhinvoke.codec.util.TimeUtil;
import org.inksnow.ankhinvoke.comments.InternalName;
import org.inksnow.ankhinvoke.util.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AsmUtil {
  private AsmUtil() {
    throw new UnsupportedOperationException();
  }

  public static void visitException(@Nullable MethodVisitor mv, @InternalName @NotNull String exception, @NotNull String message) {
    if (mv == null) {
      return;
    }
    mv.visitTypeInsn(Opcodes.NEW, exception);
    mv.visitInsn(Opcodes.DUP);
    mv.visitLdcInsn(message);
    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, exception, "<init>", "(Ljava/lang/String;)V", false);
    if(AnkhInvoke.DEBUG) {
      AsmUtil.visitLogInvokeException(mv, "error in invoke");
    }
    mv.visitInsn(Opcodes.ATHROW);
  }

  public static void visitLogInvokeException(@Nullable MethodVisitor mv, @NotNull String message) {
    if (mv == null) {
      return;
    }
    // current stack: exception
    mv.visitInsn(Opcodes.DUP); // current stack: exception, exception
    mv.visitInsn(Opcodes.ICONST_1); // current stack: exception, exception, 1
    mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object"); // current stack: exception, exception, array
    mv.visitInsn(Opcodes.DUP_X1); // current stack: exception, array, exception, array
    mv.visitInsn(Opcodes.SWAP); // current stack: exception, array, array, exception
    mv.visitInsn(Opcodes.ICONST_0); // current stack: exception, array, array, exception, 0
    mv.visitInsn(Opcodes.SWAP); // current stack: exception, array, array, 0, exception
    mv.visitInsn(Opcodes.AASTORE); // current stack: exception, array
    mv.visitLdcInsn(message); // current stack: exception, array, string
    mv.visitInsn(Opcodes.SWAP); // current stack: exception, string, array
    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "org/inksnow/ankhinvoke/spy/$AnkhInvokeSpy$", "$error$", "(Ljava/lang/String;[Ljava/lang/Object;)V", false); // current stack: exception
  }

  public static void visitBootstrap(@Nullable MethodVisitor mv, String name, String descriptor, int opcode, String owner, String describe) {
    if (mv == null) {
      return;
    }
    mv.visitInvokeDynamicInsn(name, descriptor, spyBootstrapHandle(), opcode, owner, describe);
  }

  private static @NotNull Handle spyBootstrapHandle() {
    return new Handle(
        Opcodes.H_INVOKESTATIC,
        AnkhInvoke.ANKH_INVOKE_PACKAGE.replace('.', '/') + "/spy/$AnkhInvokeSpy$",
        "$bootstrap$",
        "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;ILjava/lang/String;Ljava/lang/String;)Ljava/lang/invoke/CallSite;",
        false);
  }

  public static @NotNull ClassNode readClass(@NotNull URL url) throws IOException {
    try (InputStream in = url.openStream()) {
      return readClass(IOUtils.readAllBytes(in), 0);
    }
  }

  public static @NotNull ClassNode readClass(@NotNull URL url, int parsingOptions) throws IOException {
    try (InputStream in = url.openStream()) {
      return readClass(IOUtils.readAllBytes(in), parsingOptions);
    }
  }

  public static @NotNull ClassNode readClass(byte @NotNull [] bytes) {
    return readClass(bytes, 0);
  }

  public static @NotNull ClassNode readClass(byte @NotNull [] bytes, int parsingOptions) {
    ClassNode classNode = new ClassNode();
    ClassReader classReader = new ClassReader(bytes);
    classReader.accept(classNode, parsingOptions);
    return classNode;
  }
}
