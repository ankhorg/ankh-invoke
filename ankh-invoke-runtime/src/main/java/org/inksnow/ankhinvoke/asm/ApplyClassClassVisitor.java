package org.inksnow.ankhinvoke.asm;

import org.inksnow.ankhinvoke.AnkhInvoke;
import org.inksnow.ankhinvoke.PredicateService;
import org.inksnow.ankhinvoke.ReferenceService;
import org.inksnow.ankhinvoke.RemapService;
import org.inksnow.ankhinvoke.reference.ReferenceMetadata;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.MethodRemapper;
import org.objectweb.asm.commons.Remapper;

public class ApplyClassClassVisitor extends ClassRemapper {
  private final @NotNull ReferenceService referenceService;
  private final @NotNull PredicateService predicateService;
  private final @NotNull RemapService referenceRemapService;

  public ApplyClassClassVisitor(@NotNull ReferenceService referenceService, @NotNull PredicateService predicateService, @NotNull RemapService referenceRemapService, @NotNull ClassVisitor classVisitor) {
    super(classVisitor, new ClassNameRemapper(referenceService, predicateService, referenceRemapService));
    this.referenceService = referenceService;
    this.predicateService = predicateService;
    this.referenceRemapService = referenceRemapService;
  }

  protected MethodVisitor createMethodRemapper(final MethodVisitor methodVisitor) {
    return new SkipInvokeMethodRemapper(referenceService, predicateService, referenceRemapService, methodVisitor, remapper);
  }

  @Override
  public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
    super.visit(version, access, name, signature, superName, interfaces);
  }

  private static final class SkipInvokeMethodRemapper extends MethodRemapper {
    private final @NotNull ReferenceService referenceService;
    private final @NotNull PredicateService predicateService;
    private final @NotNull RemapService referenceRemapService;

    private SkipInvokeMethodRemapper(@NotNull ReferenceService referenceService, @NotNull PredicateService predicateService, @NotNull RemapService referenceRemapService, MethodVisitor methodVisitor, Remapper remapper) {
      super(Opcodes.ASM9, methodVisitor, remapper);
      this.referenceService = referenceService;
      this.predicateService = predicateService;
      this.referenceRemapService = referenceRemapService;
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
      ReferenceMetadata metadata = referenceService.get(owner);
      ReferenceMetadata.Entry entry = metadata.method(name + descriptor);
      if (entry == null) {
        if (metadata.isEmpty()) {
          super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        } else {
          AsmUtil.visitException(mv, "java/lang/UnsupportedOperationException", "owner '" + owner + "' mapped but method '" + name + descriptor + "' not found");
        }
        return;
      }
      ReferenceMetadata.Handle selectedHandle = null;

      for (ReferenceMetadata.Handle handle : entry.handles()) {
        if (predicateService.testPredicate(handle.predicates())) {
          selectedHandle = handle;
          break;
        }
      }

      if (selectedHandle == null) {
        AsmUtil.visitException(mv, "java/lang/UnsupportedOperationException", "Unsupported operation: reference L" + owner + ";" + name + descriptor + " not supported");
        return;
      }

      String fullDescriptor = remapper.mapMethodDesc((opcode == Opcodes.INVOKESTATIC)
          ? descriptor
          : ("(L" + owner + ";" + descriptor.substring(1)));
      visitHandle(opcode, fullDescriptor, selectedHandle);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
      ReferenceMetadata metadata = referenceService.get(owner);
      ReferenceMetadata.Entry entry = metadata.field(name + ":" + descriptor);
      if (entry == null) {
        if(metadata.isEmpty()) {
          super.visitFieldInsn(opcode, owner, name, descriptor);
        }else{
          AsmUtil.visitException(mv, "java/lang/UnsupportedOperationException", "owner '" + owner + "' mapped but field '" + name + ":" + descriptor + "' not found");
        }
        return;
      }
      ReferenceMetadata.Handle selectedHandle = null;

      for (ReferenceMetadata.Handle handle : entry.handles()) {
        if (predicateService.testPredicate(handle.predicates())) {
          selectedHandle = handle;
          break;
        }
      }

      if (selectedHandle == null) {
        AsmUtil.visitException(mv, "java/lang/UnsupportedOperationException", "Unsupported operation: reference L" + owner + ";" + name + ":" + descriptor + " not supported");
        return;
      }

      boolean isStatic = opcode == Opcodes.GETSTATIC || opcode == Opcodes.PUTSTATIC;
      boolean isGet = opcode == Opcodes.GETSTATIC || opcode == Opcodes.GETFIELD;

      String fullDescriptor = remapper.mapMethodDesc("(" +
          (isStatic ? "" : ("L" + owner + ";")) +
          (isGet ? (")" + descriptor) : (descriptor + ")V")));
      visitHandle(opcode, fullDescriptor, selectedHandle);
    }

    private void visitHandle(int opcode, String fullDescriptor, ReferenceMetadata.@NotNull Handle handle) {
      boolean isMethod = handle.describe().startsWith("(");
      String mappedFullDescriptor = remapper.mapMethodDesc(fullDescriptor);
      String mappedOwner = referenceRemapService.map(handle.owner());
      String mappedName;
      String mappedDescribe;
      if(isMethod) {
        mappedName = referenceRemapService.mapMethodName(handle.owner(), handle.name(), handle.describe());
        mappedDescribe = referenceRemapService.mapDesc(handle.describe());
      }else{
        mappedName = referenceRemapService.mapFieldName(handle.owner(), handle.name(), handle.describe());
        mappedDescribe = referenceRemapService.mapDesc(handle.describe());
      }
      if (handle.useAccessor()) {
        AsmUtil.visitBootstrap(mv, mappedName, mappedFullDescriptor, opcode, mappedOwner, mappedDescribe);
      } else if (isMethod){
        mv.visitMethodInsn(handle.isInterface() ? Opcodes.INVOKEINTERFACE : opcode, mappedOwner, mappedName, mappedDescribe, handle.isInterface());
      } else {
        mv.visitFieldInsn(opcode, mappedOwner, mappedName, mappedDescribe);
      }
    }
  }

  private static final class ClassNameRemapper extends Remapper {
    private final @NotNull ReferenceService referenceService;
    private final @NotNull PredicateService predicateService;
    private final @NotNull RemapService referenceRemapService;

    private ClassNameRemapper(@NotNull ReferenceService referenceService, @NotNull PredicateService predicateService, @NotNull RemapService referenceRemapService) {
      this.referenceService = referenceService;
      this.predicateService = predicateService;
      this.referenceRemapService = referenceRemapService;
    }

    @Override
    public @NotNull String map(@NotNull String internalName) {
      for (ReferenceMetadata.Handle handle : referenceService.get(internalName).handles()) {
        if (predicateService.testPredicate(handle.predicates())) {
          return referenceRemapService.map(handle.owner());
        }
      }
      return internalName;
    }
  }
}
