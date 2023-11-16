package org.inksnow.ankhinvoke.asm;

import org.inksnow.ankhinvoke.AnkhInvoke;
import org.inksnow.ankhinvoke.comments.InternalName;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Handle;
import org.objectweb.asm.commons.ModuleHashesAttribute;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.*;
import org.slf4j.Logger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ClassRemapperProcess implements ClassNodeProcessor {
  private static final Logger logger = LoggerFactory.getLogger(AnkhInvoke.ANKH_INVOKE_PACKAGE);
  private static final String IGNORE_NOT_FULL_SUPPORT_KEY = AnkhInvoke.ANKH_INVOKE_PACKAGE + ".asm.ignoreNotFullSupport";
  private static final boolean IGNORE_NOT_FULL_SUPPORT = Boolean.getBoolean(IGNORE_NOT_FULL_SUPPORT_KEY);

  protected final @NotNull Remapper remapper;

  public ClassRemapperProcess(@NotNull Remapper remapper) {
    this.remapper = remapper;
  }

  @Override
  public @NotNull ClassNode process(@NotNull ClassNode classNode) {
    String rawClassName = classNode.name;

    processClass(classNode);
    processModule(classNode.module);
    processAnnotationList(classNode.visibleAnnotations);
    processAnnotationList(classNode.invisibleAnnotations);
    processTypeAnnotationList(classNode.visibleTypeAnnotations);
    processTypeAnnotationList(classNode.invisibleTypeAnnotations);
    processAttributeList(classNode.attrs);
    processRecordComponentList(rawClassName, classNode.recordComponents);
    processFieldList(rawClassName, classNode.fields);
    processMethodList(rawClassName, classNode.methods);
    processInnerClassList(classNode.innerClasses);
    processOuterClass(classNode);
    processNestHost(classNode);
    processNestMembers(classNode);
    processPermittedSubclass(classNode);
    return classNode;
  }

  protected void processClass(@NotNull ClassNode classNode) {
    classNode.name = remapper.mapType(classNode.name);
    classNode.signature = remapper.mapSignature(classNode.signature, false);
    classNode.superName = remapper.mapType(classNode.superName);
    classNode.interfaces = remapTypeList(classNode.interfaces);
  }

  // === process module =============================================

  protected void processModule(@Nullable ModuleNode moduleNode) {
    if (moduleNode == null) {
      return;
    }
    moduleNode.name = remapper.mapModuleName(moduleNode.name);
    moduleNode.mainClass = remapper.mapType(moduleNode.mainClass);
    moduleNode.packages = remapPackageList(moduleNode.packages);
    moduleNode.requires = remapModuleRequireList(moduleNode.requires);
    moduleNode.exports = remapModuleExportList(moduleNode.exports);
    moduleNode.opens = remapModuleOpenList(moduleNode.opens);
    moduleNode.uses = remapPackageList(moduleNode.uses);
    moduleNode.provides = remapModuleProvideList(moduleNode.provides);
  }

  @Contract("null -> null; !null -> !null")
  protected List<@NotNull ModuleRequireNode> remapModuleRequireList(List<@NotNull ModuleRequireNode> requires) {
    if (requires == null) {
      return null;
    }
    return requires.stream().map(this::remapModuleRequire).collect(Collectors.toList());
  }

  @Contract("null -> null; !null -> !null")
  protected ModuleRequireNode remapModuleRequire(ModuleRequireNode require) {
    if (require == null) {
      return null;
    }
    return new ModuleRequireNode(remapper.mapModuleName(require.module), require.access, require.version);
  }

  @Contract("null -> null; !null -> !null")
  protected List<@NotNull ModuleExportNode> remapModuleExportList(List<@NotNull ModuleExportNode> exports) {
    if(exports == null) {
      return null;
    }
    return exports.stream().map(this::remapModuleExport).collect(Collectors.toList());
  }

  @Contract("null -> null; !null -> !null")
  protected ModuleExportNode remapModuleExport(ModuleExportNode export) {
    if(export == null) {
      return null;
    }
    return new ModuleExportNode(remapper.mapPackageName(export.packaze), export.access, remapModuleList(export.modules));
  }

  @Contract("null -> null; !null -> !null")
  protected List<@NotNull ModuleOpenNode> remapModuleOpenList(List<@NotNull ModuleOpenNode> opens) {
    if (opens == null) {
      return null;
    }
    return opens.stream().map(this::remapModuleOpen).collect(Collectors.toList());
  }

  @Contract("null -> null; !null -> !null")
  protected ModuleOpenNode remapModuleOpen(ModuleOpenNode open) {
    if(open == null) {
      return null;
    }
    return new ModuleOpenNode(remapper.mapPackageName(open.packaze), open.access, remapModuleList(open.modules));
  }

  @Contract("null -> null; !null -> !null")
  protected List<@NotNull ModuleProvideNode> remapModuleProvideList(List<@NotNull ModuleProvideNode> provides) {
    if(provides == null) {
      return null;
    }
    return provides.stream().map(this::remapModuleProvide).collect(Collectors.toList());
  }

  @Contract("null -> null; !null -> !null")
  protected ModuleProvideNode remapModuleProvide(ModuleProvideNode provide) {
    if(provide == null) {
      return null;
    }
    return new ModuleProvideNode(remapper.mapType(provide.service), remapTypeList(provide.providers));
  }

  // === process annotation =========================================

  protected void processAnnotationListArray(@NotNull List<@NotNull AnnotationNode> @Nullable [] annotationLists) {
    if(annotationLists == null) {
      return;
    }
    for (List<AnnotationNode> annotationList : annotationLists) {
      processAnnotationList(annotationList);
    }
  }

  protected void processAnnotationList(@Nullable List<@NotNull AnnotationNode> annotations) {
    if(annotations == null) {
      return;
    }
    for (AnnotationNode annotation : annotations) {
      processAnnotation(annotation);
    }
  }

  protected void processTypeAnnotationList(@Nullable List<@NotNull TypeAnnotationNode> annotations) {
    if(annotations == null) {
      return;
    }
    for (AnnotationNode annotation : annotations) {
      processAnnotation(annotation);
    }
  }

  protected void processAnnotation(@Nullable AnnotationNode annotation) {
    if (annotation == null) {
      return;
    }
    String rawDesc = annotation.desc;
    annotation.desc = remapper.mapDesc(rawDesc);
    if(annotation.values == null) {
      return;
    }
    for (int i = 0; i < annotation.values.size(); i+=2) {
      String key = (String) annotation.values.get(i);
      Object value = annotation.values.get(i + 1);
      annotation.values.set(i, remapper.mapAnnotationAttributeName(rawDesc, key));
      annotation.values.set(i + 1, remapAnnotationValue(value));
    }
  }

  protected Object remapAnnotationValue(Object value) {
    if(value instanceof List) {
      if(!IGNORE_NOT_FULL_SUPPORT) {
        logger.debug("remap annotation value with list value is not full support. use {} to disable this message", IGNORE_NOT_FULL_SUPPORT_KEY);
      }
      return value;
    } else if(value instanceof String[]) {
      String[] arrayValue = (String[]) value;
      return remapper.mapValue(new String[]{ remapper.mapDesc(arrayValue[0]), arrayValue[1] });
    } else {
      return remapper.mapValue(value);
    }
  }

  // === process attribute ==========================================

  protected void processAttributeList(@Nullable List<@NotNull Attribute> attributes) {
    if (attributes == null) {
      return;
    }
    for (Attribute attribute : attributes) {
      processAttribute(attribute);
    }
  }

  protected void processAttribute(@Nullable Attribute attribute) {
    if (attribute instanceof ModuleHashesAttribute) {
      ((ModuleHashesAttribute) attribute).modules = remapModuleList(((ModuleHashesAttribute) attribute).modules);
    }
  }

  // === process record component ===================================

  protected void processRecordComponentList(@InternalName @NotNull String className, @Nullable List<@NotNull RecordComponentNode> recordComponents) {
    if (recordComponents == null) {
      return;
    }
    for (RecordComponentNode recordComponent : recordComponents) {
      processRecordComponent(className, recordComponent);
    }
  }

  protected void processRecordComponent(@InternalName @NotNull String className, @Nullable RecordComponentNode recordComponent) {
    if (recordComponent == null) {
      return;
    }
    recordComponent.name = remapper.mapRecordComponentName(className, recordComponent.name, recordComponent.descriptor);
    recordComponent.descriptor = remapper.mapDesc(recordComponent.descriptor);
    recordComponent.signature = remapper.mapSignature(recordComponent.signature, true);
    processAnnotationList(recordComponent.visibleAnnotations);
    processAnnotationList(recordComponent.invisibleAnnotations);
    processTypeAnnotationList(recordComponent.visibleTypeAnnotations);
    processTypeAnnotationList(recordComponent.invisibleTypeAnnotations);
  }

  // === process field ==============================================

  protected void processFieldList(@InternalName @NotNull String className, @Nullable List<@NotNull FieldNode> fields) {
    if (fields == null) {
      return;
    }
    for (FieldNode field : fields) {
      processField(className, field);
    }
  }

  protected void processField(@InternalName @NotNull String className, @Nullable FieldNode field) {
    if (field == null) {
      return;
    }
    field.name = remapper.mapFieldName(className, field.name, field.desc);
    field.desc = remapper.mapDesc(field.desc);
    field.signature = remapper.mapSignature(field.signature, true);

    processAnnotationList(field.visibleAnnotations);
    processAnnotationList(field.invisibleAnnotations);
    processTypeAnnotationList(field.visibleTypeAnnotations);
    processTypeAnnotationList(field.invisibleTypeAnnotations);
  }

  // === process method =============================================
  protected void processMethodList(@InternalName @NotNull String className, @Nullable List<@NotNull MethodNode> methodNodes) {
    if (methodNodes == null) {
      return;
    }
    for (MethodNode methodNode : methodNodes) {
      processMethod(className, methodNode);
    }
  }

  protected void processMethod(@InternalName @NotNull String className, @Nullable MethodNode methodNode) {
    if (methodNode == null) {
      return;
    }
    methodNode.name = remapper.mapMethodName(className, methodNode.name, methodNode.desc);
    methodNode.desc = remapper.mapMethodDesc(methodNode.desc);
    methodNode.signature = remapper.mapSignature(methodNode.signature, false);
    methodNode.exceptions = remapTypeList(methodNode.exceptions);

    methodNode.annotationDefault = remapAnnotationValue(methodNode.annotationDefault);
    processAnnotationList(methodNode.visibleAnnotations);
    processAnnotationList(methodNode.invisibleAnnotations);
    processTypeAnnotationList(methodNode.visibleTypeAnnotations);
    processTypeAnnotationList(methodNode.invisibleTypeAnnotations);
    processAnnotationListArray(methodNode.visibleParameterAnnotations);
    processAnnotationListArray(methodNode.invisibleParameterAnnotations);

    processInstructionList(methodNode.instructions);

    processTryCatchBlockList(methodNode.tryCatchBlocks);

    processLocalVariableList(methodNode.localVariables);
    processLocalVariableAnnotationList(methodNode.visibleLocalVariableAnnotations);

    processAttributeList(methodNode.attrs);
  }

  protected void processInstructionList(@Nullable InsnList insnList) {
    if (insnList == null) {
      return;
    }
    for (AbstractInsnNode insn : insnList) {
      processInstruction(insnList, insn);
    }
  }

  protected void processInstruction(@NotNull InsnList insnList, @Nullable AbstractInsnNode insn) {
    if(insn == null) {
      return;
    }

    if (insn instanceof FrameNode) {
      processFrame(insnList, (FrameNode) insn);
    } else if (insn instanceof FieldInsnNode) {
      processFieldInsn(insnList, (FieldInsnNode) insn);
    } else if(insn instanceof MethodInsnNode) {
      processMethodInsn(insnList, (MethodInsnNode) insn);
    } else if(insn instanceof InvokeDynamicInsnNode) {
      processInvokeDynamicInsn(insnList, (InvokeDynamicInsnNode) insn);
    } else if(insn instanceof TypeInsnNode) {
      processTypeInsn(insnList, (TypeInsnNode) insn);
    } else if(insn instanceof LdcInsnNode) {
      processLdcInsn(insnList, (LdcInsnNode) insn);
    } else if(insn instanceof MultiANewArrayInsnNode) {
      processMultiANewArrayInsn(insnList, (MultiANewArrayInsnNode) insn);
    }

    processTypeAnnotationList(insn.visibleTypeAnnotations);
    processTypeAnnotationList(insn.invisibleTypeAnnotations);
  }

  protected void processFrame(@NotNull InsnList insnList, @Nullable FrameNode frame) {
    if (frame == null) {
      return;
    }
    frame.local = remapFrameTypeList(frame.local);
    frame.stack = remapFrameTypeList(frame.stack);
  }

  @Contract("null -> null; !null -> !null")
  protected List<Object> remapFrameTypeList(List<Object> frameTypeList) {
    if (frameTypeList == null) {
      return null;
    }
    return frameTypeList.stream()
        .map(it->(it instanceof String) ? remapper.mapType((String) it) : it)
        .collect(Collectors.toList());
  }

  protected void processFieldInsn(@NotNull InsnList insnList, @Nullable FieldInsnNode fieldInsn) {
    if (fieldInsn == null) {
      return;
    }
    String rawOwner = fieldInsn.owner;
    fieldInsn.owner = remapper.mapType(rawOwner);
    fieldInsn.name = remapper.mapFieldName(rawOwner, fieldInsn.name, fieldInsn.desc);
    fieldInsn.desc = remapper.mapDesc(fieldInsn.desc);
  }

  protected void processMethodInsn(@NotNull InsnList insnList, @Nullable MethodInsnNode methodInsn) {
    if (methodInsn == null) {
      return;
    }
    String rawOwner = methodInsn.owner;
    methodInsn.owner = remapper.mapType(rawOwner);
    methodInsn.name = remapper.mapMethodName(rawOwner, methodInsn.name, methodInsn.desc);
    methodInsn.desc = remapper.mapMethodDesc(methodInsn.desc);
  }

  protected void processInvokeDynamicInsn(@NotNull InsnList insnList, @Nullable InvokeDynamicInsnNode invokeDynamicInsn) {
    if (invokeDynamicInsn == null) {
      return;
    }
    invokeDynamicInsn.name = remapper.mapInvokeDynamicMethodName(invokeDynamicInsn.name, invokeDynamicInsn.desc);
    invokeDynamicInsn.desc = remapper.mapMethodDesc(invokeDynamicInsn.desc);
    invokeDynamicInsn.bsm = (Handle) remapper.mapValue(invokeDynamicInsn.bsm);
    invokeDynamicInsn.bsmArgs = remapValueArray(invokeDynamicInsn.bsmArgs);
  }

  protected void processTypeInsn(@NotNull InsnList insnList, @Nullable TypeInsnNode typeInsn) {
    if (typeInsn == null) {
      return;
    }
    typeInsn.desc = remapper.mapType(typeInsn.desc);
  }

  protected void processLdcInsn(@NotNull InsnList insnList, @Nullable LdcInsnNode ldcInsn) {
    if (ldcInsn == null) {
      return;
    }
    ldcInsn.cst = remapper.mapValue(ldcInsn.cst);
  }

  protected void processMultiANewArrayInsn(@NotNull InsnList insnList, @Nullable MultiANewArrayInsnNode multiANewArrayInsn) {
    if (multiANewArrayInsn == null) {
      return;
    }
    multiANewArrayInsn.desc = remapper.mapDesc(multiANewArrayInsn.desc);
  }

  protected void processTryCatchBlockList(@Nullable List<@NotNull TryCatchBlockNode> tryCatchBlocks) {
    if (tryCatchBlocks == null) {
      return;
    }
    for (TryCatchBlockNode tryCatchBlock : tryCatchBlocks) {
      processTryCatchBlock(tryCatchBlock);
    }
  }

  protected void processTryCatchBlock(@Nullable TryCatchBlockNode tryCatchBlock) {
    if (tryCatchBlock == null) {
      return;
    }
    tryCatchBlock.type = (tryCatchBlock.type == null) ? null : remapper.mapType(tryCatchBlock.type);
    processTypeAnnotationList(tryCatchBlock.visibleTypeAnnotations);
    processTypeAnnotationList(tryCatchBlock.invisibleTypeAnnotations);
  }

  protected void processLocalVariableList(@Nullable List<@NotNull LocalVariableNode> localVariables) {
    if (localVariables == null) {
      return;
    }
    for (LocalVariableNode localVariable : localVariables) {
      processLocalVariable(localVariable);
    }
  }

  protected void processLocalVariable(@Nullable LocalVariableNode localVariable) {
    if (localVariable == null) {
      return;
    }
    localVariable.desc = remapper.mapDesc(localVariable.desc);
    localVariable.signature = remapper.mapSignature(localVariable.signature, true);
  }

  protected void processLocalVariableAnnotationList(@Nullable List<@NotNull LocalVariableAnnotationNode> localVariableAnnotations) {
    if (localVariableAnnotations == null) {
      return;
    }
    for (LocalVariableAnnotationNode localVariableAnnotation : localVariableAnnotations) {
      processLocalVariableAnnotation(localVariableAnnotation);
    }
  }

  protected void processLocalVariableAnnotation(@Nullable LocalVariableAnnotationNode localVariableAnnotation) {
    if (localVariableAnnotation == null) {
      return;
    }
    localVariableAnnotation.desc = remapper.mapDesc(localVariableAnnotation.desc);
    processAnnotation(localVariableAnnotation);
  }

  // === process inner class ========================================
  protected void processInnerClassList(@Nullable List<@NotNull InnerClassNode> innerClassNodes) {
    if (innerClassNodes == null) {
      return;
    }
    for (InnerClassNode innerClassNode : innerClassNodes) {
      processInnerClass(innerClassNode);
    }
  }

  protected void processInnerClass(@Nullable InnerClassNode innerClass) {
    if (innerClass == null) {
      return;
    }
    String rawName = innerClass.name;
    String rawOuterName = innerClass.outerName;
    innerClass.name = remapper.mapType(rawName);
    innerClass.outerName = remapper.mapType(rawOuterName);
    innerClass.innerName = remapper.mapInnerClassName(rawName, rawOuterName, innerClass.innerName);
  }

  // === process outer class ========================================
  protected void processOuterClass(@Nullable ClassNode classNode) {
    if (classNode == null) {
      return;
    }
    String rawOwner = classNode.outerClass;
    String rawName = classNode.outerMethod;
    classNode.outerClass = remapper.mapType(rawOwner);
    classNode.outerMethod = (rawName == null) ? null : remapper.mapMethodName(rawOwner, rawName, classNode.outerMethodDesc);
    classNode.outerMethodDesc = (classNode.outerMethodDesc == null) ? null : remapper.mapMethodDesc(classNode.outerMethodDesc);
  }

  // === process some simple metadata ===============================
  protected void processNestHost(@Nullable ClassNode classNode) {
    if (classNode == null) {
      return;
    }
    classNode.nestHostClass = remapper.mapType(classNode.nestHostClass);
  }

  protected void processNestMembers(@Nullable ClassNode classNode) {
    if (classNode == null) {
      return;
    }
    classNode.nestMembers = remapTypeList(classNode.nestMembers);
  }

  protected void processPermittedSubclass(@Nullable ClassNode classNode) {
    if (classNode == null) {
      return;
    }
    classNode.permittedSubclasses = remapTypeList(classNode.permittedSubclasses);
  }

  // === some utils =================================================

  @Contract("null -> null; !null -> !null")
  protected List<@NotNull String> remapTypeList(List<@NotNull String> types) {
    if (types == null) {
      return null;
    }
    return new ArrayList<>(Arrays.asList(remapper.mapTypes(types.toArray(new String[0]))));
  }

  @Contract("null -> null; !null -> !null")
  protected List<@NotNull String> remapPackageList(List<@NotNull String> packages) {
    if (packages == null) {
      return null;
    }
    return packages.stream()
        .map(remapper::mapPackageName)
        .collect(Collectors.toList());
  }

  @Contract("null -> null; !null -> !null")
  protected List<@NotNull String> remapModuleList(List<@NotNull String> modules) {
    if(modules == null) {
      return null;
    }
    return modules.stream()
        .map(remapper::mapModuleName)
        .collect(Collectors.toList());
  }

  @Contract("null -> null; !null -> !null")
  protected @Nullable Object[] remapValueArray(@Nullable Object[] values) {
    if (values == null) {
      return null;
    }
    Object[] result = new Object[values.length];
    for (int i = 0; i < values.length; i++) {
      result[i] = remapper.mapValue(values[i]);
    }
    return result;
  }
}
