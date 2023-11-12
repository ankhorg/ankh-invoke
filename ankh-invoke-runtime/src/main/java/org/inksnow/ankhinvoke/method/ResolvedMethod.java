package org.inksnow.ankhinvoke.method;

import org.inksnow.ankhinvoke.util.DstUnsafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Arrays;
import java.util.function.Function;

public final class ResolvedMethod extends ParsedMethod {
  private final @NotNull Class<?> ownerClass;
  private final @NotNull Class<?> returnClass;
  private final @NotNull Class<?> @NotNull [] argumentClasses;

  public ResolvedMethod(@NotNull Type owner, @NotNull String name, @NotNull Type type, @NotNull Class<?> ownerClass, @NotNull Class<?> returnClass, @NotNull Class<?> @NotNull [] argumentClasses) {
    super(owner, name, type);

    this.ownerClass = ownerClass;
    this.returnClass = returnClass;
    this.argumentClasses = argumentClasses;
  }

  public static ResolvedMethod parse(@NotNull Function<@NotNull String, @Nullable Class<?>> loaderFunction, @NotNull String reference) {
    return ParsedMethod.parse(reference).resolve(loaderFunction);
  }

  public @NotNull Class<?> ownerClass() {
    return ownerClass;
  }

  public @NotNull Class<?> returnClass() {
    return returnClass;
  }

  public @NotNull Class<?> @NotNull [] argumentClasses() {
    return argumentClasses;
  }

  @Override
  public @NotNull ResolvedMethod resolve(@NotNull Function<@NotNull String, @Nullable Class<?>> loaderFunction) {
    return this;
  }

  public @NotNull MethodHandle lookupStatic(@NotNull MethodHandles.Lookup lookup) {
    try {
      return lookup.findStatic(ownerClass, name(), MethodType.methodType(returnClass, argumentClasses));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw DstUnsafe.throwImpl(e);
    }
  }

  public @NotNull MethodHandle lookupVirtual(@NotNull MethodHandles.Lookup lookup) {
    try {
      return lookup.findVirtual(ownerClass, name(), MethodType.methodType(returnClass, argumentClasses));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw DstUnsafe.throwImpl(e);
    }
  }

  public @NotNull MethodHandle lookupSpecial(@NotNull MethodHandles.Lookup lookup, @Nullable Class<?> specialCaller) {
    try {
      return lookup.findSpecial(ownerClass, name(), MethodType.methodType(returnClass, argumentClasses), specialCaller == null ? ownerClass : specialCaller);
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw DstUnsafe.throwImpl(e);
    }
  }

  public @NotNull MethodHandle lookupConstructor(@NotNull MethodHandles.Lookup lookup) {
    try {
      if ("<init>".equals(name())) {
        throw new IllegalAccessException("try to lookup constructor in invalid method: method name not \"<init>\"");
      }
      if (returnClass == void.class) {
        throw new IllegalAccessException("try to lookup constructor in invalid method: method return type not void");
      }
      return lookup.findConstructor(ownerClass, MethodType.methodType(returnClass, argumentClasses));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw DstUnsafe.throwImpl(e);
    }
  }

  @Override
  public @NotNull String toString() {
    return "ResolvedMethod(" + owner().getDescriptor() + name() + type().getDescriptor() + ")";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    ResolvedMethod that = (ResolvedMethod) o;

    if (!ownerClass.equals(that.ownerClass)) return false;
    if (!returnClass.equals(that.returnClass)) return false;
    // Probably incorrect - comparing Object[] arrays with Arrays.equals
    return Arrays.equals(argumentClasses, that.argumentClasses);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + ownerClass.hashCode();
    result = 31 * result + returnClass.hashCode();
    result = 31 * result + Arrays.hashCode(argumentClasses);
    return result;
  }
}
