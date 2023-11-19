package org.inksnow.ankhinvoke.method;

import org.inksnow.ankhinvoke.util.DstUnsafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.lang.reflect.Array;
import java.util.function.Function;

public class ParsedMethod {
  private final @NotNull Type owner;
  private final @NotNull String name;
  private final @NotNull Type type;

  ParsedMethod(@NotNull Type owner, @NotNull String name, @NotNull Type type) {
    this.owner = owner;
    this.name = name;
    this.type = type;
  }

  private static @NotNull Class<?> typeToClass(@NotNull Function<@NotNull String, @Nullable Class<?>> loaderFunction, @NotNull Type type) {
    switch (type.getSort()) {
      case Type.VOID:
        return void.class;
      case Type.BOOLEAN:
        return boolean.class;
      case Type.CHAR:
        return char.class;
      case Type.BYTE:
        return byte.class;
      case Type.SHORT:
        return short.class;
      case Type.INT:
        return int.class;
      case Type.FLOAT:
        return float.class;
      case Type.LONG:
        return long.class;
      case Type.DOUBLE:
        return double.class;
      case Type.ARRAY: {
        Class<?> currentClass = typeToClass(loaderFunction, type.getElementType());
        for (int i = 0; i < type.getDimensions(); i++) {
          currentClass = Array.newInstance(currentClass, 0).getClass();
        }
        return currentClass;
      }
      case Type.OBJECT:
        Class<?> currentClass = loaderFunction.apply(type.getClassName());
        if (currentClass != null) {
          return currentClass;
        }
      default:
        throw DstUnsafe.throwImpl(new ClassNotFoundException(type.getDescriptor()));
    }
  }

  public static ParsedMethod parse(@NotNull String reference) {
    try {
      int firstSplitIndex = reference.indexOf(';');
      int secondSplitIndex = reference.indexOf('(', firstSplitIndex);
      return new ParsedMethod(
          Type.getObjectType(reference.substring(1, firstSplitIndex)),
          reference.substring(firstSplitIndex + 1, secondSplitIndex),
          Type.getMethodType(reference.substring(secondSplitIndex))
      );
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to parse reference '" + reference + "'", e);
    }
  }

  public final @NotNull Type owner() {
    return owner;
  }

  public final @NotNull String name() {
    return name;
  }

  public final @NotNull Type type() {
    return type;
  }

  public final @NotNull Type returnType() {
    return type.getReturnType();
  }

  public final @NotNull Type[] argunmentType() {
    return type.getArgumentTypes();
  }

  public @NotNull ResolvedMethod resolve(@NotNull Function<@NotNull String, @Nullable Class<?>> loaderFunction) {
    Class<?> ownerClass = typeToClass(loaderFunction, owner);
    Class<?> returnClass = typeToClass(loaderFunction, type.getReturnType());
    Type[] argumentTypes = type.getArgumentTypes();
    Class<?>[] argumentClasses = new Class<?>[argumentTypes.length];
    for (int i = 0; i < argumentTypes.length; i++) {
      argumentClasses[i] = typeToClass(loaderFunction, argumentTypes[i]);
    }
    return new ResolvedMethod(owner, name, type, ownerClass, returnClass, argumentClasses);
  }

  @Override
  public @NotNull String toString() {
    return "ParsedMethod(" + owner.getDescriptor() + name + type.getDescriptor() + ")";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ParsedMethod that = (ParsedMethod) o;

    if (!owner.equals(that.owner)) return false;
    if (!name.equals(that.name)) return false;
    return type.equals(that.type);
  }

  @Override
  public int hashCode() {
    int result = owner.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + type.hashCode();
    return result;
  }
}
