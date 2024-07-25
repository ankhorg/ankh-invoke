package org.inksnow.ankhinvoke.bukkit.paper;

import org.inksnow.ankhinvoke.util.DstUnsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class R$MappingEnvironment {
  private static final Class<?> mappingEnvironmentClass;
  private static final MethodHandle F$LEGACY_CB_VERSION;
  private static final MethodHandle M$reobf;

  static {
    mappingEnvironmentClass = createMappingEnvironmentClass();
    if (mappingEnvironmentClass == null) {
      F$LEGACY_CB_VERSION = null;
      M$reobf = null;
    } else {
      MethodHandles.Lookup lookup = MethodHandles.lookup();
      try {
        F$LEGACY_CB_VERSION = lookup.findStaticGetter(
            mappingEnvironmentClass, "LEGACY_CB_VERSION", String.class
        );

        M$reobf = lookup.findStatic(
            mappingEnvironmentClass, "reobf", MethodType.methodType(boolean.class)
        );
      } catch (Throwable e) {
        throw DstUnsafe.throwImpl(e);
      }
    }
  }

  private static Class<?> createMappingEnvironmentClass() {
    try {
      return Class.forName("io.papermc.paper.util.MappingEnvironment");
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  public static boolean hasPaperMapping() {
    return mappingEnvironmentClass != null;
  }

  public static String legacyCBVersion() {
    if (F$LEGACY_CB_VERSION == null) {
      throw new UnsupportedOperationException("Paper mapping environment not found");
    }
    try {
      return (String) F$LEGACY_CB_VERSION.invoke();
    } catch (Throwable e) {
      throw DstUnsafe.throwImpl(e);
    }
  }

  public static boolean reobf() {
    if (M$reobf == null) {
      throw new UnsupportedOperationException("Paper mapping environment not found");
    }
    try {
      return (boolean) M$reobf.invoke();
    } catch (Throwable e) {
      throw DstUnsafe.throwImpl(e);
    }
  }
}
