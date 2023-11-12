package org.inksnow.ankhinvoke.util;

import bot.inker.acj.JvmHacker;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.security.ProtectionDomain;

public class DstUnsafe {
  private static final MethodHandle defineClassHandle = findDefineClassHandle();
  private static final MethodHandle shouldBeInitializedHandle = findShouldBeInitializedHandle();

  private DstUnsafe() {
    throw new UnsupportedOperationException();
  }

  @SuppressWarnings("unchecked") // i know what i do
  public static <E extends Throwable, R extends RuntimeException> @NotNull R throwImpl(@NotNull Throwable e) throws E {
    throw (E) e;
  }

  private static MethodHandle findDefineClassHandle() {
    try {
      if (JavaVersion.current().isJava11Compatible()) {
        Class<?> jdkUnsafeClass = Class.forName("jdk.internal.misc.Unsafe", false, null);
        Object jdkUnsafe = JvmHacker.lookup()
            .findStaticGetter(jdkUnsafeClass, "theUnsafe", jdkUnsafeClass)
            .invoke();
        return JvmHacker.lookup()
            .findVirtual(jdkUnsafeClass, "defineClass", MethodType.methodType(Class.class, String.class, byte[].class, int.class, int.class, ClassLoader.class, ProtectionDomain.class))
            .bindTo(jdkUnsafe);
      } else {
        Class<?> sunUnsafeClass = Class.forName("sun.misc.Unsafe", false, null);
        return JvmHacker.lookup()
            .findVirtual(sunUnsafeClass, "defineClass", MethodType.methodType(Class.class, String.class, byte[].class, int.class, int.class, ClassLoader.class, ProtectionDomain.class))
            .bindTo(JvmHacker.unsafe());
      }
    } catch (Throwable e) {
      throw throwImpl(e);
    }
  }

  public static Class<?> defineClass(String name, byte[] b, int off, int len, ClassLoader classLoader, ProtectionDomain protectionDomain) {
    try {
      return (Class<?>) defineClassHandle.invokeExact(name, b, off, len, classLoader, protectionDomain);
    } catch (Throwable e) {
      throw throwImpl(e);
    }
  }

  private static MethodHandle findShouldBeInitializedHandle() {
    try {
      Class<?> sunUnsafeClass = Class.forName("sun.misc.Unsafe", false, null);
      return JvmHacker.lookup()
          .findVirtual(sunUnsafeClass, "shouldBeInitialized", MethodType.methodType(boolean.class, Class.class))
          .bindTo(JvmHacker.unsafe());
    } catch (Throwable e) {
      throw throwImpl(e);
    }
  }

  public static boolean shouldBeInitialized(Class<?> clazz) {
    try {
      return (boolean) defineClassHandle.invokeExact(clazz);
    } catch (Throwable e) {
      throw throwImpl(e);
    }
  }
}
