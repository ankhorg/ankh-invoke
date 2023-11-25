package org.inksnow.ankhinvoke.map;

import org.inksnow.ankhinvoke.codec.BlobReference;
import org.inksnow.ankhinvoke.codec.TextMetadata;
import org.inksnow.ankhinvoke.map.asm.AddProcessedAnnotationClassVisitor;
import org.inksnow.ankhinvoke.map.asm.ScanReferenceVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.zip.GZIPOutputStream;

public final class BlobReferenceGenerator {
  private final @NotNull String ankhInvokePackage;
  private final boolean disableClassRename;
  private final @NotNull List<@NotNull String> referencePackages;

  private final TextMetadata.@NotNull Builder metadata = TextMetadata.builder();
  private final @NotNull Map<@NotNull String, @NotNull BlobReference> referenceMap = new HashMap<>();

  private BlobReferenceGenerator(@NotNull String ankhInvokePackage, boolean disableClassRename, @NotNull List<@NotNull String> referencePackages) {
    this.ankhInvokePackage = ankhInvokePackage;
    this.disableClassRename = disableClassRename;
    this.referencePackages = referencePackages;
  }

  public void acceptScan(@NotNull String name, @NotNull InputStream in) throws IOException {
    if(!name.endsWith(".class")) {
      return;
    }
    boolean canBeReferenceClass = canBeReferenceClass(name.substring(0, name.length() - ".class".length()));

    ClassNode classNode = new ClassNode();
    ClassReader classReader = new ClassReader(in);
    classReader.accept(classNode, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

    BlobReference.Builder builder = BlobReference.builder();

    builder.appendSuperClass(classNode.superName);
    for (String interfaceName : classNode.interfaces) {
      builder.appendSuperClass(interfaceName);
    }

    if (canBeReferenceClass) {
      loadAnnotationList(classNode.visibleAnnotations, builder::appendHandle);

      for (MethodNode methodNode : classNode.methods) {
        BlobReference.BlobEntry.Builder entryBuilder = BlobReference.BlobEntry.builder();
        loadAnnotationList(methodNode.visibleAnnotations, entryBuilder::appendHandle);
        BlobReference.BlobEntry blobEntry = entryBuilder.build();
        if (!blobEntry.isEmpty()) {
          builder.appendMethod(methodNode.name, methodNode.desc, blobEntry);
        }
      }

      for (FieldNode fieldNode : classNode.fields) {
        BlobReference.BlobEntry.Builder entryBuilder = BlobReference.BlobEntry.builder();
        loadAnnotationList(fieldNode.visibleAnnotations, entryBuilder::appendHandle);
        BlobReference.BlobEntry blobEntry = entryBuilder.build();
        if (!blobEntry.isEmpty()) {
          builder.appendField(fieldNode.name, fieldNode.desc, blobEntry);
        }
      }
    }

    BlobReference blobReference = builder.build();
    if(!blobReference.isEmpty()) {
      referenceMap.put(classNode.name, blobReference);
    }
  }

  public @NotNull ProcessAction acceptProcess(@NotNull String name, @NotNull InputStream in) throws IOException {
    if((ankhInvokePackage.replace('.', '/') + "/metadata.txt").equals(name)) {
      metadata.load(in);
      return ProcessAction.remove();
    } else if(!name.endsWith(".class")) {
      return ProcessAction.keep();
    } else if(name.endsWith(".ankh-invoke.class")) {
      return ProcessAction.keep();
    }

    System.out.println("name: " + name);

    BlobReference reference;
    if (canBeReferenceClass(name)) {
      reference = referenceMap.get(name.substring(0, name.length() - ".class".length()));
    } else {
      reference = null;
    }

    ScanReferenceVisitor scanReference = new ScanReferenceVisitor(referenceMap);

    ClassWriter cw = new ClassWriter(Opcodes.ASM9);

    ClassVisitor cv = cw;
    cv = new AddProcessedAnnotationClassVisitor("ankh-invoke-userdev", cv);
    cv = scanReference.createClassVisitor(cv);

    new ClassReader(in).accept(cv, 0);

    if (reference != null) {
      metadata.append("scanned reference class at " + name)
          .append("  |- with " + reference.handles() + " handle(s)")
          .append("  |- with " + reference.methodMap().size() + " method(s)")
          .append("  |- with " + reference.fieldMap().size() + " field(s)");
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      try(DataOutputStream out = new DataOutputStream(new GZIPOutputStream(bout))) {
        reference.save(out);
      }
      return new ProcessAction(Action.KEEP, bout.toByteArray(), name.substring(0, name.length() - ".class".length()) + ".ankh-invoke-reference");
    } else if (scanReference.usedClassWithReference()) {
      metadata.append("scanned invoke class at " + name);
      return new ProcessAction(Action.KEEP, cw.toByteArray(),
          disableClassRename ? null : (name.substring(0, name.length() - ".class".length()) + ".ankh-invoke.class"));
    } else {
      return ProcessAction.keep();
    }
  }

  public @NotNull List<@NotNull ProcessAction> collect() throws IOException {
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    try(OutputStream out = bout) {
      metadata.save(out);
    }
    return Collections.singletonList(new ProcessAction(Action.KEEP, bout.toByteArray(),
        ankhInvokePackage.replace('.', '/') + "/metadata.txt"));
  }

  private void loadAnnotationList(@Nullable List<@NotNull AnnotationNode> annotationList, @NotNull Consumer<BlobReference.@NotNull BlobHandle> appendAction) {
    if (annotationList == null) {
      return;
    }
    for (AnnotationNode annotationNode : annotationList) {
      if(annotationNode.desc.equals("L" + ankhInvokePackage.replace('.', '/') + "/comments/HandleBy$List;")) {
        loadAnnotationList((List<AnnotationNode>) annotationNode.values.get(1), appendAction);
      }else if(annotationNode.desc.equals("L" + ankhInvokePackage.replace('.', '/') + "/comments/HandleBy;")) {
        appendAction.accept(loadHandleByAnnotation(annotationNode));
      }
    }
  }

  private static BlobReference.@NotNull BlobHandle loadHandleByAnnotation(@NotNull AnnotationNode annotationNode) {
    BlobReference.BlobHandle.Builder builder = BlobReference.BlobHandle.builder();
    String currentKey = null;
    for (Object value : annotationNode.values) {
      if (currentKey == null) {
        currentKey = (String) value;
        continue;
      }
      switch (currentKey) {
        case "reference": {
          builder.setReference((String) value);
          break;
        }
        case "predicates": {
          for (String predicate : (List<String>) value) {
            builder.appendPredicate(predicate);
          }
          break;
        }
        case "isInterface": {
          builder.isInterface((Boolean) value);
          break;
        }
        case "useAccessor": {
          builder.useAccessor((Boolean) value);
          break;
        }
        default: {
          throw new IllegalStateException("unknown key: " + currentKey);
        }
      }
      currentKey = null;
    }
    return builder.build();
  }

  private boolean canBeReferenceClass(@NotNull String className) {
    for (String referencePackage : referencePackages) {
      if (className.startsWith(referencePackage) && className.charAt(referencePackage.length()) == '/') {
        return true;
      }
    }
    return false;
  }

  public static final class ProcessAction {
    private final @NotNull Action action;
    private final byte @Nullable [] bytes;
    private final @Nullable String newName;

    private ProcessAction(@NotNull Action action, byte @Nullable [] bytes, @Nullable String newName) {
      this.action = action;
      this.bytes = bytes;
      this.newName = newName;
    }

    public byte @Nullable [] getBytes() {
      return bytes;
    }

    public @Nullable String getNewName() {
      return newName;
    }

    public boolean isRemove() {
      return action == Action.REMOVE;
    }

    public boolean isKeep() {
      return action == Action.KEEP;
    }

    public boolean shouldRename() {
      return newName != null;
    }

    public boolean shouldReplace() {
      return bytes != null;
    }

    public static ProcessAction keep() {
      return new ProcessAction(Action.KEEP, null, null);
    }

    public static ProcessAction remove() {
      return new ProcessAction(Action.REMOVE, null, null);
    }
  }

  private enum Action {
    REMOVE, KEEP;
  }

  public static @NotNull Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private @NotNull String ankhInvokePackage = AnkhInvokeMapping.ANKH_INVOKE_PACKAGE;
    private boolean disableClassRename;
    private final @NotNull List<@NotNull String> referencePackages = new ArrayList<>();

    public @NotNull Builder setAnkhInvokePackage(@NotNull String ankhInvokePackage) {
      this.ankhInvokePackage = ankhInvokePackage;
      return this;
    }

    public @NotNull Builder setDisableClassRename(boolean disableClassRename) {
      this.disableClassRename = disableClassRename;
      return this;
    }

    public @NotNull Builder appendReferencePackage(@NotNull String packageName) {
      referencePackages.add(packageName.replace('.', '/'));
      return this;
    }

    public @NotNull Builder appendReferencePackage(@NotNull Iterable<String> packageNames) {
      for (String packageName : packageNames) {
        referencePackages.add(packageName.replace('.', '/'));
      }
      return this;
    }

    public @NotNull Builder appendReferencePackage(@NotNull Iterator<String> packageNames) {
      while (packageNames.hasNext()) {
        referencePackages.add(packageNames.next().replace('.', '/'));
      }
      return this;
    }

    public @NotNull Builder appendReferencePackage(@NotNull String @NotNull ... packageNames) {
      for (String packageName : packageNames) {
        referencePackages.add(packageName.replace('.', '/'));
      }
      return this;
    }

    public @NotNull BlobReferenceGenerator build() {
      return new BlobReferenceGenerator(ankhInvokePackage, disableClassRename, Collections.unmodifiableList(new ArrayList<>(referencePackages)));
    }
  }
}
