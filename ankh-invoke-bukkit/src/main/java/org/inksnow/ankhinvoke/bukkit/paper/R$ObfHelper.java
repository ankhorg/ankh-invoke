package org.inksnow.ankhinvoke.bukkit.paper;

import org.inksnow.ankhinvoke.util.DstUnsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Map;

public class R$ObfHelper {
  private static final Class<?> obfHelperClass;
  private static final MethodHandle F$INSTANCE;
  private static final MethodHandle M$deobfClassName;
  private static final MethodHandle M$mappingsByObfName;

  private static final Class<?> obfHelperClassMappingClass;
  private static final MethodHandle M$ClassMapping$methodsByObf;
  private static final MethodHandle M$ClassMapping$fieldsByObf;

  static {
    obfHelperClass = createObfHelperClass();
    obfHelperClassMappingClass = createObfHelperClassMappingClass();

    if (obfHelperClass == null || obfHelperClassMappingClass == null) {
      F$INSTANCE = null;
      M$deobfClassName = null;
      M$mappingsByObfName = null;
      M$ClassMapping$methodsByObf = null;
      M$ClassMapping$fieldsByObf = null;
    } else {
      MethodHandles.Lookup lookup = MethodHandles.lookup();
      try {
        F$INSTANCE = lookup.findStaticGetter(
            obfHelperClass, "INSTANCE", obfHelperClass
        );

        M$deobfClassName = lookup.findVirtual(
            obfHelperClass, "deobfClassName", MethodType.methodType(String.class, String.class)
        );

        M$mappingsByObfName = lookup.findVirtual(
            obfHelperClass, "mappingsByObfName", MethodType.methodType(Map.class)
        );

        M$ClassMapping$methodsByObf = lookup.findVirtual(
            obfHelperClassMappingClass, "methodsByObf", MethodType.methodType(Map.class)
        );

        M$ClassMapping$fieldsByObf = lookup.findVirtual(
            obfHelperClassMappingClass, "fieldsByObf", MethodType.methodType(Map.class)
        );
      } catch (Throwable e) {
        throw DstUnsafe.throwImpl(e);
      }
    }
  }

  private R$ObfHelper() {
    throw new UnsupportedOperationException();
  }

  public static Object INSTANCE() {
    if (F$INSTANCE == null) {
      throw new UnsupportedOperationException("ObfHelper not found");
    }
    try {
      return F$INSTANCE.invoke();
    } catch (Throwable e) {
      throw DstUnsafe.throwImpl(e);
    }
  }

  public static String deobfClassName(Object instance, String fullyQualifiedObfName) {
    if (M$deobfClassName == null) {
      throw new UnsupportedOperationException("ObfHelper not found");
    }
    try {
      return (String) M$deobfClassName.invoke(instance, fullyQualifiedObfName);
    } catch (Throwable e) {
      throw DstUnsafe.throwImpl(e);
    }
  }

  private static Class<?> createObfHelperClass() {
    try {
      return Class.forName("io.papermc.paper.util.ObfHelper");
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  private static Class<?> createObfHelperClassMappingClass() {
    try {
      return Class.forName("io.papermc.paper.util.ObfHelper$ClassMapping");
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  public static Map<String, Object> mappingsByObfName(Object instance) {
    if (M$mappingsByObfName == null) {
      throw new UnsupportedOperationException("ObfHelper not found");
    }
    try {
      return (Map<String, Object>) M$mappingsByObfName.invoke(instance);
    } catch (Throwable e) {
      throw DstUnsafe.throwImpl(e);
    }
  }

  public static class R$ClassMapping {
    private R$ClassMapping() {
      throw new UnsupportedOperationException();
    }

    public static Map<String, String> methodsByObf(Object instance) {
      if (M$ClassMapping$methodsByObf == null) {
        throw new UnsupportedOperationException("ObfHelper not found");
      }
      try {
        return (Map<String, String>) M$ClassMapping$methodsByObf.invoke(instance);
      } catch (Throwable e) {
        throw DstUnsafe.throwImpl(e);
      }
    }

    public static Map<String, String> fieldsByObf(Object instance) {
      if (M$ClassMapping$fieldsByObf == null) {
        throw new UnsupportedOperationException("ObfHelper not found");
      }
      try {
        return (Map<String, String>) M$ClassMapping$fieldsByObf.invoke(instance);
      } catch (Throwable e) {
        throw DstUnsafe.throwImpl(e);
      }
    }
  }
}
