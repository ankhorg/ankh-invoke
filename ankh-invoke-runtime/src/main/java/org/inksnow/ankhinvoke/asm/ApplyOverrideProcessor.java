package org.inksnow.ankhinvoke.asm;

import org.inksnow.ankhinvoke.*;
import org.inksnow.ankhinvoke.method.ParsedMethod;
import org.inksnow.ankhinvoke.reference.ReferenceMetadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;

public class ApplyOverrideProcessor implements ClassNodeProcessor {
  private static final @NotNull Logger logger = LoggerFactory.getLogger(AnkhInvoke.ANKH_INVOKE_PACKAGE);
  private static final @NotNull Function<@NotNull String, @NotNull List<@NotNull MethodNode>> METHOD_NODE_LIST_FUNCTION = it->new ArrayList<>();
  private final @NotNull ReferenceService referenceService;
  private final @NotNull PredicateService predicateService;
  private final @NotNull RemapService referenceRemapService;
  private final @NotNull ClassPoolService classPoolService;

  public ApplyOverrideProcessor(@NotNull ReferenceService referenceService, @NotNull PredicateService predicateService, @NotNull RemapService referenceRemapService, @NotNull ClassPoolService classPoolService) {
    this.referenceService = referenceService;
    this.predicateService = predicateService;
    this.referenceRemapService = referenceRemapService;
    this.classPoolService = classPoolService;
  }

  @Override
  public @NotNull ClassNode process(@NotNull ClassNode classNode) {
    Map<String, List<MethodNode>> methodMap = prepareMethodMap(classNode);
    Set<String> parents = classPoolService.collectParents(classNode.name);
    Set<String> generatedOverride = new HashSet<>();

    for (String parent : parents) {
      ReferenceMetadata metadata = referenceService.get(parent);
      for (Map.Entry<String, ReferenceMetadata.Entry> entry : metadata.methodMap().entrySet()) {
        ParsedMethod parsedMethod = ParsedMethod.parse("L" + parent + ";" + entry.getKey());
        if ("<init>".equals(parsedMethod.name())) {
          continue;
        }
        List<MethodNode> methodNodeList = methodMap.get(parsedMethod.name());
        if(methodNodeList == null) {
          continue;
        }
        for (MethodNode methodNode : methodNodeList) {
          if((methodNode.access & Opcodes.ACC_STATIC) != 0) {
            continue;
          }
          if(!matchMethod(methodNode, parsedMethod)) {
            continue;
          }
          ReferenceMetadata.Handle selectHandle = selectHandle(entry.getValue());
          if (selectHandle == null) {
            logger.debug("no handle found for L{};{}, override will not active", parent, entry.getKey());
            continue;
          }
          if (!selectHandle.describe().startsWith("(")) {
            logger.warn("try to override reference L{};{}, but handle L{};{}:{} is field", parent, entry.getKey(), selectHandle.owner(), selectHandle.name(), selectHandle.describe());
            continue;
          }
          String remappedName = referenceRemapService.mapMethodName(selectHandle.owner(), selectHandle.name(), selectHandle.describe());
          MethodNode generatedMethodNode = createMethodNode(classNode, methodNode, remappedName);
          tagProcessed(generatedMethodNode, "L" + parent + ";" + entry.getKey(), selectHandle);
          if (!generatedOverride.add(generatedMethodNode.name + generatedMethodNode.desc)) {
            continue;
          }
          classNode.methods.add(generatedMethodNode);
        }
      }
    }
    return classNode;
  }

  private @NotNull MethodNode createMethodNode(@NotNull ClassNode classNode, @NotNull MethodNode targetMethodNode, @NotNull String name) {
    Type methodType = Type.getMethodType(targetMethodNode.desc);
    Type[] argunmentTypes = methodType.getArgumentTypes();
    Type returnType = methodType.getReturnType();

    MethodNode result = new MethodNode();
    result.name = name;
    result.access = targetMethodNode.access;
    result.desc = targetMethodNode.desc;

    InsnList insn = result.instructions;
    insn.add(new VarInsnNode(Opcodes.ALOAD, 0));
    int currentVarIndex = 1;
    for (Type argunmentType : argunmentTypes) {
      insn.add(AsmUtil.createVarLoad(argunmentType, currentVarIndex));
      currentVarIndex += argunmentType.getSize();
    }
    boolean isInterface = (classNode.access & Opcodes.ACC_INTERFACE) != 0;
    insn.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, classNode.name, targetMethodNode.name, targetMethodNode.desc, isInterface));
    insn.add(AsmUtil.createReturn(returnType));


    return result;
  }

  private void tagProcessed(@NotNull MethodNode methodNode, @NotNull String reference, ReferenceMetadata.@NotNull Handle selectHandle) {
    if (methodNode.visibleAnnotations == null) {
      methodNode.visibleAnnotations = new ArrayList<>();
    }
    methodNode.visibleAnnotations.add(AsmUtil.createProcessedAnnotation(
        "apply-overrice-processor: " + reference,
        "use handle: L" + selectHandle.owner() + ";" + selectHandle.name() + (selectHandle.describe().startsWith("(") ? "" : ":") + selectHandle.describe()
    ));
  }

  private boolean matchMethod(@NotNull MethodNode methodNode, @NotNull ParsedMethod referenceMethod) {
    ParsedMethod overrideMethod = ParsedMethod.parse(referenceMethod.owner().getDescriptor() + methodNode.name + methodNode.desc);

    if (referenceMethod.argunmentType().length != overrideMethod.argunmentType().length) {
      return false;
    }

    for (int i = 0; i < referenceMethod.argunmentType().length; i++) {
      Type referenceArgunment = referenceMethod.argunmentType()[i];
      Type overrideArgunment = overrideMethod.argunmentType()[i];

      if (referenceArgunment.getSort() != overrideArgunment.getSort()) {
        return false;
      }

      while (referenceArgunment.getSort() == Type.ARRAY) {
        if(referenceArgunment.getDimensions() != overrideArgunment.getDimensions()) {
          return false;
        }
        referenceArgunment = referenceArgunment.getElementType();
        overrideArgunment = overrideArgunment.getElementType();

        if (referenceArgunment.getSort() != overrideArgunment.getSort()) {
          return false;
        }
      }

      if (referenceArgunment.getSort() == Type.OBJECT) {
        if (!classPoolService.instanceOf(referenceArgunment.getInternalName(), overrideArgunment.getInternalName())) {
          return false;
        }
      }
    }
    return true;
  }

  private ReferenceMetadata.@Nullable Handle selectHandle(ReferenceMetadata.@NotNull Entry entry) {
    for (ReferenceMetadata.Handle handle : entry.handles()) {
      if (predicateService.testPredicate(handle.predicates())) {
        return handle;
      }
    }
    return null;
  }

  private static @NotNull Map<@NotNull String, @NotNull List<@NotNull MethodNode>> prepareMethodMap(@NotNull ClassNode classNode) {
    Map<String, List<MethodNode>> methodMap = new HashMap<>();
    for (MethodNode method : classNode.methods) {
      methodMap.computeIfAbsent(method.name, METHOD_NODE_LIST_FUNCTION).add(method);
    }
    return methodMap;
  }
}
