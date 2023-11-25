package org.inksnow.ankhinvoke.reference;

import org.inksnow.ankhinvoke.asm.AsmUtil;
import org.inksnow.ankhinvoke.codec.BlobReference;
import org.inksnow.ankhinvoke.comments.HandleBy;
import org.inksnow.ankhinvoke.comments.InternalName;
import org.inksnow.ankhinvoke.util.DstUnsafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class ResourceReferenceSource implements ReferenceSource {
  private static final AnnotationNode[] EMPTY_ANNOTATION_NODE_ARRAY = new AnnotationNode[0];
  private static final String HANDLE_BY_DESCRIPTOR = Type.getDescriptor(HandleBy.class);
  private static final String HANDLE_BY_LIST_DESCRIPTOR = Type.getDescriptor(HandleBy.List.class);
  private final @NotNull ClassLoader classLoader;

  public ResourceReferenceSource(@NotNull ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  @SuppressWarnings("unchecked") // never happened
  private static @NotNull AnnotationNode @NotNull [] getHandleByArray(@Nullable List<@NotNull AnnotationNode> elementList) {
    if (elementList == null) {
      return EMPTY_ANNOTATION_NODE_ARRAY;
    }
    for (AnnotationNode annotationNode : elementList) {
      if (HANDLE_BY_DESCRIPTOR.equals(annotationNode.desc)) {
        return new AnnotationNode[]{annotationNode};
      } else if (HANDLE_BY_LIST_DESCRIPTOR.equals(annotationNode.desc) && annotationNode.values != null && annotationNode.values.size() > 1) {
        return ((List<AnnotationNode>) annotationNode.values.get(1)).toArray(EMPTY_ANNOTATION_NODE_ARRAY);
      }
    }
    return EMPTY_ANNOTATION_NODE_ARRAY;
  }

  @Override
  public @Nullable ReferenceMetadata load(@InternalName @NotNull String name) {

    URL url = loadFirst(name + ".class", name + ".ankh-invoke.class", name + ".ankh-invoke-reference.class");
    if(url != null) {
      return loadClassMetadata(url);
    }
    url = classLoader.getResource(name + ".ankh-invoke-reference");
    if(url != null) {
      return loadBlobMetadata(url);
    }
    return null;
  }

  private @Nullable URL loadFirst(@NotNull String @NotNull ... names) {
    for (String name : names) {
      URL url = classLoader.getResource(name);
      if(url != null) {
        return url;
      }
    }
    return null;
  }

  private @NotNull ReferenceMetadata loadClassMetadata(@NotNull URL url) {
    ClassNode classNode;
    try {
      classNode = AsmUtil.readClass(url, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
    } catch (IOException e) {
      throw DstUnsafe.throwImpl(e);
    }

    ReferenceMetadata.Builder builder = ReferenceMetadata.builder();

    builder.appendSuperClass(classNode.superName);
    for (String interfaceName : classNode.interfaces) {
      builder.appendSuperClass(interfaceName);
    }

    for (AnnotationNode annotationNode : getHandleByArray(classNode.visibleAnnotations)) {
      builder.appendHandle(ReferenceMetadata.Handle.fromAnnotationNode(annotationNode));
    }

    for (FieldNode field : classNode.fields) {
      String fieldKey = field.name + ":" + field.desc;
      for (AnnotationNode annotationNode : getHandleByArray(field.visibleAnnotations)) {
        builder.appendField(fieldKey, ReferenceMetadata.Handle.fromAnnotationNode(annotationNode));
      }
    }

    for (MethodNode method : classNode.methods) {
      String methodKey = method.name + method.desc;
      for (AnnotationNode annotationNode : getHandleByArray(method.visibleAnnotations)) {
        builder.appendMethod(methodKey, ReferenceMetadata.Handle.fromAnnotationNode(annotationNode));
      }
    }

    return builder.build();
  }

  private @NotNull ReferenceMetadata loadBlobMetadata(@NotNull URL url) {
    try(DataInputStream in = new DataInputStream(new GZIPInputStream(url.openStream()))) {
      return ReferenceMetadata.fromBlob(BlobReference.load(in));
    } catch (IOException e) {
      throw DstUnsafe.throwImpl(e);
    }
  }
}
