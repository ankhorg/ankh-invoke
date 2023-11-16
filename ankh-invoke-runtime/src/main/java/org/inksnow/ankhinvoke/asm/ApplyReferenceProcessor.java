package org.inksnow.ankhinvoke.asm;

import org.inksnow.ankhinvoke.PredicateService;
import org.inksnow.ankhinvoke.ReferenceService;
import org.inksnow.ankhinvoke.RemapService;
import org.inksnow.ankhinvoke.reference.ReferenceMetadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;

public class ApplyReferenceProcessor extends ClassRemapperProcess {
  private final @NotNull ReferenceService referenceService;
  private final @NotNull PredicateService predicateService;
  private final @NotNull RemapService referenceRemapService;

  public ApplyReferenceProcessor(@NotNull ReferenceService referenceService, @NotNull PredicateService predicateService, @NotNull RemapService referenceRemapService) {
    super(new ClassNameRemapper(referenceService, predicateService, referenceRemapService));
    this.referenceService = referenceService;
    this.predicateService = predicateService;
    this.referenceRemapService = referenceRemapService;
  }

  @Override
  protected void processMethodInsn(@NotNull InsnList insnList, @Nullable MethodInsnNode methodInsn) {
    if (methodInsn == null) {
      return;
    }
    ReferenceMetadata metadata = referenceService.get(methodInsn.owner);
    ReferenceMetadata.Entry entry = metadata.method(methodInsn.name + methodInsn.desc);
    if(entry == null) {
      if (metadata.isEmpty()) {
        super.processMethodInsn(insnList, methodInsn);
      } else {
        insnList.insertBefore(methodInsn, AsmUtil.createException("java/lang/UnsupportedOperationException", "owner '" + methodInsn.owner + "' mapped but method '" + methodInsn.name + methodInsn.desc + "' not found"));
        insnList.remove(methodInsn);
      }
      return;
    }
    ReferenceMetadata.Handle selectedHandle = selectHandle(entry);
    if (selectedHandle == null) {
      insnList.insertBefore(methodInsn, AsmUtil.createException("java/lang/UnsupportedOperationException", "Unsupported operation: reference L" + methodInsn.owner + ";" + methodInsn.name + methodInsn.desc + " not supported"));
      insnList.remove(methodInsn);
      return;
    }
    String fullDescriptor = remapper.mapMethodDesc((methodInsn.getOpcode() == Opcodes.INVOKESTATIC)
        ? methodInsn.desc
        : ("(L" + methodInsn.owner + ";" + methodInsn.desc.substring(1)));
    insnList.insertBefore(methodInsn, createHandle(methodInsn.getOpcode(), fullDescriptor, selectedHandle));
    insnList.remove(methodInsn);
  }

  @Override
  protected void processFieldInsn(@NotNull InsnList insnList, @Nullable FieldInsnNode fieldInsn) {
    if (fieldInsn == null) {
      return;
    }
    ReferenceMetadata metadata = referenceService.get(fieldInsn.owner);
    ReferenceMetadata.Entry entry = metadata.field(fieldInsn.name + ":" + fieldInsn.desc);
    if(entry == null) {
      if (metadata.isEmpty()) {
        super.processFieldInsn(insnList, fieldInsn);
      } else {
        insnList.insertBefore(fieldInsn, AsmUtil.createException("java/lang/UnsupportedOperationException", "owner '" + fieldInsn.owner + "' mapped but field '" + fieldInsn.name + ":" + fieldInsn.desc + "' not found"));
        insnList.remove(fieldInsn);
      }
      return;
    }
    ReferenceMetadata.Handle selectedHandle = selectHandle(entry);
    if (selectedHandle == null) {
      insnList.insertBefore(fieldInsn, AsmUtil.createException("java/lang/UnsupportedOperationException", "Unsupported operation: reference L" + fieldInsn.owner + ";" + fieldInsn.name + ":" + fieldInsn.desc + " not supported"));
      insnList.remove(fieldInsn);
      return;
    }

    boolean isStatic = fieldInsn.getOpcode() == Opcodes.GETSTATIC || fieldInsn.getOpcode() == Opcodes.PUTSTATIC;
    boolean isGet = fieldInsn.getOpcode() == Opcodes.GETSTATIC || fieldInsn.getOpcode() == Opcodes.GETFIELD;

    String fullDescriptor = remapper.mapMethodDesc("(" +
        (isStatic ? "" : ("L" + fieldInsn.owner + ";")) +
        (isGet ? (")" + fieldInsn.desc) : (fieldInsn.desc + ")V")));
    insnList.insertBefore(fieldInsn, createHandle(fieldInsn.getOpcode(), fullDescriptor, selectedHandle));
    insnList.remove(fieldInsn);
  }

  private ReferenceMetadata.@Nullable Handle selectHandle(ReferenceMetadata.@NotNull Entry entry) {
    for (ReferenceMetadata.Handle handle : entry.handles()) {
      if (predicateService.testPredicate(handle.predicates())) {
        return handle;
      }
    }
    return null;
  }

  private @NotNull InsnList createHandle(int opcode, @NotNull String fullDescriptor, ReferenceMetadata.@NotNull Handle handle) {
    InsnList result = new InsnList();
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
      result.add(AsmUtil.createBootstrap(mappedName, mappedFullDescriptor, opcode, mappedOwner, mappedDescribe));
    } else if (isMethod){
      result.add(new MethodInsnNode(handle.isInterface() ? Opcodes.INVOKEINTERFACE : opcode, mappedOwner, mappedName, mappedDescribe, handle.isInterface()));
    } else {
      result.add(new FieldInsnNode(opcode, mappedOwner, mappedName, mappedDescribe));
    }
    return result;
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
