package org.inksnow.ankhinvoke.classpool;

import org.inksnow.ankhinvoke.comments.InternalName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.util.*;

public final class ClassPoolNode {
  private final int access;
  private final @InternalName @Nullable String superClass;
  private final @NotNull List<@InternalName @NotNull String> implementClasses;

  private ClassPoolNode(int access, @InternalName @Nullable String superClass, @NotNull List<@InternalName @NotNull String> implementClasses) {
    this.access = access;
    this.superClass = superClass;
    this.implementClasses = implementClasses;
  }

  public static @NotNull Builder builder() {
    return new Builder();
  }

  public static @NotNull ClassPoolNode fromClassNode(@NotNull ClassNode classNode) {
    return ClassPoolNode.builder()
        .access(classNode.access)
        .superClass(classNode.superName)
        .implementClasses(classNode.interfaces)
        .build();
  }

  public static @NotNull ClassPoolNode fromClassNode(@NotNull Class<?> clazz) {
    Class<?>[] interfaceClasses = clazz.getInterfaces();
    String[] interfaceClassNames = new String[interfaceClasses.length];
    for (int i = 0; i < interfaceClasses.length; i++) {
      interfaceClassNames[i] = Type.getInternalName(interfaceClasses[i]);
    }
    Class<?> superClass = clazz.getSuperclass();
    return ClassPoolNode.builder()
        .access(clazz.getModifiers())
        .superClass(superClass == null ? null : Type.getInternalName(superClass))
        .implementClasses(interfaceClassNames)
        .build();
  }

  public int access() {
    return access;
  }

  public boolean isInterface() {
    return (access & Opcodes.ACC_INTERFACE) != 0;
  }

  public boolean isAbstract() {
    return (access & Opcodes.ACC_ABSTRACT) != 0;
  }

  public boolean isAnnotation() {
    return (access & Opcodes.ACC_ANNOTATION) != 0;
  }

  public @InternalName @Nullable String superClass() {
    return superClass;
  }

  public @NotNull List<@InternalName @NotNull String> implementClasses() {
    return implementClasses;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ClassPoolNode that = (ClassPoolNode) o;

    if (access != that.access) return false;
    if (!Objects.equals(superClass, that.superClass)) return false;
    return implementClasses.equals(that.implementClasses);
  }

  @Override
  public int hashCode() {
    int result = access;
    result = 31 * result + (superClass != null ? superClass.hashCode() : 0);
    result = 31 * result + implementClasses.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "ClassPoolNode{" +
        "access=" + access +
        ", superClass='" + superClass + '\'' +
        ", implementClasses=" + implementClasses +
        '}';
  }

  public static final class Builder {
    private final @NotNull List<@InternalName @NotNull String> implementClasses = new ArrayList<>();
    private @InternalName @Nullable Integer access;
    private @InternalName @Nullable String superClass;

    public @NotNull Builder access(int access) {
      this.access = access;
      return this;
    }

    public @NotNull Builder superClass(@InternalName @Nullable String superClass) {
      this.superClass = superClass;
      return this;
    }

    public @NotNull Builder implementClasses(@InternalName @NotNull String implementClass) {
      this.implementClasses.add(implementClass);
      return this;
    }

    public @NotNull Builder implementClasses(@InternalName @NotNull String @NotNull ... implementClasses) {
      this.implementClasses.addAll(Arrays.asList(implementClasses));
      return this;
    }

    public @NotNull Builder implementClasses(@NotNull Collection<@InternalName @NotNull String> implementClasses) {
      this.implementClasses.addAll(implementClasses);
      return this;
    }

    public @NotNull Builder clearImplementClasses() {
      this.implementClasses.clear();
      return this;
    }

    public @NotNull ClassPoolNode build() {
      if (access == null) {
        throw new IllegalArgumentException("ClassNodePool must special a access");
      }
      return new ClassPoolNode(access, superClass, Collections.unmodifiableList(new ArrayList<>(implementClasses)));
    }
  }
}
