package org.inksnow.ankhinvoke.reference;

import org.inksnow.ankhinvoke.comments.HandleBy;
import org.inksnow.ankhinvoke.comments.InternalName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ClassReferenceSource implements ReferenceSource {
  private static final HandleBy[] EMPTY_HANDLE_BY_ARRAY = new HandleBy[0];
  private final @NotNull ClassLoader classLoader;

  public ClassReferenceSource(@NotNull ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  private static @NotNull HandleBy @NotNull [] getHandleByArray(@NotNull AnnotatedElement element) {
    HandleBy.List handleByList = element.getAnnotation(HandleBy.List.class);
    if (handleByList != null) {
      return handleByList.value();
    }
    HandleBy handleBy = element.getAnnotation(HandleBy.class);
    return handleBy == null ? EMPTY_HANDLE_BY_ARRAY : new HandleBy[]{handleBy};
  }

  @Override
  public @Nullable ReferenceMetadata load(@InternalName @NotNull String owner) {
    Class<?> referenceClass = null;
    try {
      referenceClass = classLoader.loadClass(owner.replace('/', '.'));
    } catch (ClassNotFoundException e) {
      //
    }
    if (referenceClass == null) {
      return null;
    }
    ReferenceMetadata.Builder builder = ReferenceMetadata.builder();

    builder.appendSuperClass(referenceClass.getSuperclass().getName().replace('.', '/'));
    for (Class<?> classInterface : referenceClass.getInterfaces()) {
      builder.appendSuperClass(classInterface.getName().replace('.', '/'));
    }

    for (HandleBy handleBy : getHandleByArray(referenceClass)) {
      builder.appendHandle(ReferenceMetadata.Handle.fromAnnotation(handleBy));
    }

    for (Field field : referenceClass.getDeclaredFields()) {
      String fieldKey = field.getName() + ":" + Type.getDescriptor(field.getType());
      for (HandleBy handleBy : getHandleByArray(field)) {
        builder.appendField(fieldKey, ReferenceMetadata.Handle.fromAnnotation(handleBy));
      }
    }

    for (Method method : referenceClass.getDeclaredMethods()) {
      String methodKey = method.getName() + Type.getMethodDescriptor(method);
      for (HandleBy handleBy : getHandleByArray(method)) {
        builder.appendMethod(methodKey, ReferenceMetadata.Handle.fromAnnotation(handleBy));
      }
    }

    return builder.build();
  }
}
