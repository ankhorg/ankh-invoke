package org.inksnow.ankhinvoke.asm;

import org.inksnow.ankhinvoke.AnkhInvoke;
import org.inksnow.ankhinvoke.codec.util.TimeUtil;
import org.inksnow.ankhinvoke.comments.InternalName;
import org.inksnow.ankhinvoke.util.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

public class AsmUtil {
  private static final @NotNull String ANKH_INVOKE_PACKAGE_INTERNAL = AnkhInvoke.ANKH_INVOKE_PACKAGE.replace('.', '/');
  private AsmUtil() {
    throw new UnsupportedOperationException();
  }

  public static @NotNull InsnList createException(@InternalName @NotNull String exception, @NotNull String message) {
    InsnList insnList = new InsnList();
    insnList.add(new TypeInsnNode(Opcodes.NEW, exception));
    insnList.add(new InsnNode(Opcodes.DUP));
    insnList.add(new LdcInsnNode(message));
    insnList.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, exception, "<init>", "(Ljava/lang/String;)V", false));
    if (AnkhInvoke.DEBUG) {
      insnList.add(createLogInvokeException("error in invoke"));
    }
    insnList.add(new InsnNode(Opcodes.ATHROW));
    // insnList.add(createSafeThrow());
    return insnList;
  }

  public static @NotNull InsnList createLogInvokeException(@NotNull String message) {
    InsnList insnList = new InsnList();
    // current stack: exception
    insnList.add(new InsnNode(Opcodes.DUP)); // current stack: exception, exception
    insnList.add(new InsnNode(Opcodes.ICONST_1)); // current stack: exception, exception, 1
    insnList.add(new TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/Object")); // current stack: exception, exception, array
    insnList.add(new InsnNode(Opcodes.DUP_X1)); // current stack: exception, array, exception, array
    insnList.add(new InsnNode(Opcodes.SWAP)); // current stack: exception, array, array, exception
    insnList.add(new InsnNode(Opcodes.ICONST_0)); // current stack: exception, array, array, exception, 0
    insnList.add(new InsnNode(Opcodes.SWAP)); // current stack: exception, array, array, 0, exception
    insnList.add(new InsnNode(Opcodes.AASTORE)); // current stack: exception, array
    insnList.add(new LdcInsnNode(message)); // current stack: exception, array, string
    insnList.add(new InsnNode(Opcodes.SWAP)); // current stack: exception, string, array
    insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, ANKH_INVOKE_PACKAGE_INTERNAL + "/spy/$AnkhInvokeSpy$", "$error$", "(Ljava/lang/String;[Ljava/lang/Object;)V", false)); // current stack: exception
    return insnList;
  }

  public static @NotNull InsnList createBootstrap(String name, String descriptor, int opcode, String owner, String describe) {
    InsnList result = new InsnList();
    result.add(new InvokeDynamicInsnNode(name, descriptor, spyBootstrapHandle(), opcode, owner, describe));
    return result;
  }

  public static @NotNull InsnList createVarLoad(Type type, int varIndex) {
    switch (type.getSort()) {
      case Type.BOOLEAN:
      case Type.BYTE:
      case Type.CHAR:
      case Type.SHORT:
      case Type.INT:
        return single(new VarInsnNode(Opcodes.ILOAD, varIndex));
      case Type.FLOAT:
        return single(new VarInsnNode(Opcodes.FLOAD, varIndex));
      case Type.LONG:
        return single(new VarInsnNode(Opcodes.LLOAD, varIndex));
      case Type.DOUBLE:
        return single(new VarInsnNode(Opcodes.DLOAD, varIndex));
      case Type.ARRAY:
      case Type.OBJECT:
        return single(new VarInsnNode(Opcodes.ALOAD, varIndex));
      default:
        throw new UnsupportedOperationException();
    }
  }

  public static @NotNull InsnList createVarStore(Type type, int varIndex) {
    switch (type.getSort()) {
      case Type.BOOLEAN:
      case Type.BYTE:
      case Type.CHAR:
      case Type.SHORT:
      case Type.INT:
        return single(new VarInsnNode(Opcodes.ISTORE, varIndex));
      case Type.FLOAT:
        return single(new VarInsnNode(Opcodes.FSTORE, varIndex));
      case Type.LONG:
        return single(new VarInsnNode(Opcodes.LSTORE, varIndex));
      case Type.DOUBLE:
        return single(new VarInsnNode(Opcodes.DSTORE, varIndex));
      case Type.ARRAY:
      case Type.OBJECT:
        return single(new VarInsnNode(Opcodes.ASTORE, varIndex));
      default:
        throw new UnsupportedOperationException();
    }
  }

  public static @NotNull InsnList createReturn(@NotNull Type type) {
    switch (type.getSort()) {
      case Type.VOID:
        return single(new InsnNode(Opcodes.RETURN));
      case Type.BOOLEAN:
      case Type.BYTE:
      case Type.CHAR:
      case Type.SHORT:
      case Type.INT:
        return single(new InsnNode(Opcodes.IRETURN));
      case Type.FLOAT:
        return single(new InsnNode(Opcodes.FRETURN));
      case Type.LONG:
        return single(new InsnNode(Opcodes.LRETURN));
      case Type.DOUBLE:
        return single(new InsnNode(Opcodes.DRETURN));
      case Type.ARRAY:
      case Type.OBJECT:
        return single(new InsnNode(Opcodes.ARETURN));
      default:
        throw new UnsupportedOperationException();
    }
  }

  public static @NotNull AnnotationNode createProcessedAnnotation(@NotNull String @NotNull ... byArray) {
    AnnotationNode annotationNode = new AnnotationNode("L" + ANKH_INVOKE_PACKAGE_INTERNAL + "/comments/AnkhInvokeProcessed;");
    annotationNode.visit("time", TimeUtil.logTime());
    annotationNode.visit("by", new ArrayList<>(Arrays.asList(byArray)));
    return annotationNode;
  }

  private static @NotNull Handle spyBootstrapHandle() {
    return new Handle(
        Opcodes.H_INVOKESTATIC,
        ANKH_INVOKE_PACKAGE_INTERNAL + "/spy/$AnkhInvokeSpy$",
        "$bootstrap$",
        "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;ILjava/lang/String;Ljava/lang/String;)Ljava/lang/invoke/CallSite;",
        false);
  }

  public static @NotNull InsnList single(@Nullable AbstractInsnNode node) {
    InsnList insnList = new InsnList();
    if (node != null) {
      insnList.add(node);
    }
    return insnList;
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
