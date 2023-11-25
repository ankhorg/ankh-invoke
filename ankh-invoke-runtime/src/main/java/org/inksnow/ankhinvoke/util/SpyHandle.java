package org.inksnow.ankhinvoke.util;

import bot.inker.acj.JvmHacker;
import org.inksnow.ankhinvoke.AnkhInvoke;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.*;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

public class SpyHandle {
  public static final @NotNull String SPY_CLASS_NAME = AnkhInvoke.ANKH_INVOKE_PACKAGE + ".spy.$AnkhInvokeSpy$";
  public static final @NotNull String SPY_INTERNAL_NAME = SPY_CLASS_NAME.replace('.', '/');
  public static final MethodHandle BOOTSTRAP_HANDLE;
  public static final MethodHandle LOGGER_TRACE;
  public static final MethodHandle LOGGER_DEBUG;
  public static final MethodHandle LOGGER_INFO;
  public static final MethodHandle LOGGER_WARN;
  public static final MethodHandle LOGGER_ERROR;
  private static final Logger logger = LoggerFactory.getLogger(SpyHandle.class);
  private static final MethodHandles.Lookup LOOKUP;

  static {
    try {
      LOOKUP = JvmHacker.lookup();
      BOOTSTRAP_HANDLE = LOOKUP.findStatic(SpyHandle.class, "bootstrap", MethodType.methodType(CallSite.class, MethodHandles.Lookup.class, String.class, MethodType.class, int.class, String.class, String.class));
      LOGGER_TRACE = LOOKUP.findVirtual(Logger.class, "trace", MethodType.methodType(void.class, String.class, Object[].class)).bindTo(logger);
      LOGGER_DEBUG = LOOKUP.findVirtual(Logger.class, "debug", MethodType.methodType(void.class, String.class, Object[].class)).bindTo(logger);
      LOGGER_INFO = LOOKUP.findVirtual(Logger.class, "info", MethodType.methodType(void.class, String.class, Object[].class)).bindTo(logger);
      LOGGER_WARN = LOOKUP.findVirtual(Logger.class, "warn", MethodType.methodType(void.class, String.class, Object[].class)).bindTo(logger);
      LOGGER_ERROR = LOOKUP.findVirtual(Logger.class, "error", MethodType.methodType(void.class, String.class, Object[].class)).bindTo(logger);
    } catch (ReflectiveOperationException e) {
      throw DstUnsafe.throwImpl(e);
    }
  }

  private SpyHandle() {
    throw new UnsupportedOperationException();
  }

  public static void injectSpy(@NotNull ClassLoader classLoader) {
    Class<?> spyClass = null;
    try {
      spyClass = Class.forName(SpyHandle.SPY_CLASS_NAME, false, classLoader);
    } catch (ClassNotFoundException e) {
      //
    }
    if (spyClass == null) {
      URL url = AnkhInvoke.class.getClassLoader().getResource(SpyHandle.SPY_INTERNAL_NAME + ".class");
      if (url != null) {
        byte[] bytes;
        try (InputStream in = url.openStream()) {
          bytes = IOUtils.readAllBytes(in);
        } catch (IOException e) {
          throw DstUnsafe.throwImpl(e);
        }
        DstUnsafe.defineClass(SpyHandle.SPY_CLASS_NAME, bytes, 0, bytes.length, classLoader, null);
      } else {
        throw DstUnsafe.throwImpl(new FileNotFoundException(SpyHandle.SPY_INTERNAL_NAME + " not found in resource"));
      }
      try {
        spyClass = Class.forName(SpyHandle.SPY_CLASS_NAME, false, classLoader);
      } catch (ClassNotFoundException e) {
        throw DstUnsafe.throwImpl(new IllegalStateException("Failed to inject spy class, seems classloader not support, at " + classLoader));
      }
    }
    if (!SPY_CLASS_NAME.equals(spyClass.getName())) {
      throw new IllegalArgumentException("class " + spyClass + "not AnkhInvokeSpy");
    }
    try {
      spyClass.getDeclaredMethod("$initial$", MethodHandle.class, MethodHandle.class, MethodHandle.class, MethodHandle.class, MethodHandle.class, MethodHandle.class)
          .invoke(null, BOOTSTRAP_HANDLE, LOGGER_TRACE, LOGGER_DEBUG, LOGGER_INFO, LOGGER_WARN, LOGGER_ERROR);
    } catch (IllegalAccessException | NoSuchMethodException e) {
      throw DstUnsafe.throwImpl(e);
    } catch (InvocationTargetException e) {
      throw DstUnsafe.throwImpl(e.getCause());
    }
  }


  private static @NotNull CallSite bootstrap(
      MethodHandles.@NotNull Lookup callerLookup,
      @NotNull String name,
      @NotNull MethodType type,
      int opcode,
      @NotNull String owner,
      @NotNull String describe) throws Exception {
    try {
      Class<?> caller = callerLookup.lookupClass();
      ClassLoader classLoader = caller.getClassLoader();
      Class<?> ownerClass = Class.forName(owner.replace('/', '.'), false, classLoader);
      if (opcode == Opcodes.INVOKEVIRTUAL
          || opcode == Opcodes.INVOKESPECIAL
          || opcode == Opcodes.INVOKESTATIC
          || opcode == Opcodes.INVOKEINTERFACE) {
        return bootstrapMethod(caller, type, classLoader, ownerClass, name, describe, opcode);
      } else if (opcode == Opcodes.GETSTATIC
          || opcode == Opcodes.PUTSTATIC
          || opcode == Opcodes.GETFIELD
          || opcode == Opcodes.PUTFIELD) {
        return bootstrapField(caller, type, classLoader, ownerClass, name, describe, opcode);
      } else {
        throw new IllegalStateException("Unsupported bootstrap opcode: " + opcode);
      }
    } catch (Exception e) {
      logger.error("failed to bootstrap method(owner={}, name={}, type={})", owner, name, type, e);
      throw e;
    }
  }

  private static @NotNull CallSite bootstrapMethod(
      @NotNull Class<?> caller,
      @NotNull MethodType type,
      @NotNull ClassLoader classLoader,
      @NotNull Class<?> ownerClass,
      @NotNull String name,
      @NotNull String describe,
      int opcode) throws ReflectiveOperationException {
    Type targetType = Type.getMethodType(describe);
    Type[] targetArgumentTypes = targetType.getArgumentTypes();
    Class<?>[] targetArgumentClasses = new Class[targetArgumentTypes.length];
    for (int i = 0; i < targetArgumentTypes.length; i++) {
      targetArgumentClasses[i] = typeToClass(classLoader, targetArgumentTypes[i]);
    }
    MethodType targetMethodType = MethodType.methodType(
        typeToClass(classLoader, targetType.getReturnType()),
        targetArgumentClasses
    );
    MethodHandle handle;
    if (opcode == Opcodes.INVOKESTATIC) {
      handle = LOOKUP.findStatic(ownerClass, name, targetMethodType).asType(type);
    } else if (opcode == Opcodes.INVOKESPECIAL) {
      handle = LOOKUP.findSpecial(ownerClass, name, targetMethodType, caller).asType(type);
    } else if (opcode == Opcodes.INVOKEVIRTUAL || opcode == Opcodes.INVOKEINTERFACE) {
      handle = LOOKUP.findVirtual(ownerClass, name, targetMethodType).asType(type);
    } else {
      throw new IllegalStateException("Unsupported invokeMethod opcode: " + opcode);
    }
    return new ConstantCallSite(handle);
  }

  private static CallSite bootstrapField(
      @NotNull Class<?> caller,
      @NotNull MethodType type,
      @NotNull ClassLoader classLoader,
      @NotNull Class<?> ownerClass,
      @NotNull String name,
      @NotNull String describe,
      int opcode) throws ReflectiveOperationException {
    Class<?> targetClass = typeToClass(classLoader, Type.getType(describe));
    MethodHandle handle;
    if (opcode == Opcodes.GETSTATIC) {
      handle = LOOKUP.findStaticGetter(ownerClass, name, targetClass).asType(type);
    } else if (opcode == Opcodes.PUTSTATIC) {
      handle = LOOKUP.findStaticSetter(ownerClass, name, targetClass).asType(type);
    } else if (opcode == Opcodes.GETFIELD) {
      handle = LOOKUP.findGetter(ownerClass, name, targetClass).asType(type);
    } else if (opcode == Opcodes.PUTFIELD) {
      handle = LOOKUP.findSetter(ownerClass, name, targetClass).asType(type);
    } else {
      throw new IllegalStateException("Unsupported invokeField opcode: " + opcode);
    }
    return new ConstantCallSite(handle);
  }

  private static Class<?> typeToClass(ClassLoader classLoader, Type type) throws ClassNotFoundException {
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
        Class<?> currentClass = typeToClass(classLoader, type.getElementType());
        for (int i = 0; i < type.getDimensions(); i++) {
          currentClass = Array.newInstance(currentClass, 0).getClass();
        }
        return currentClass;
      }
      case Type.OBJECT:
        return Class.forName(type.getClassName(), false, classLoader);
      default:
        throw new IllegalArgumentException("Unsupported class type: " + type);
    }
  }
}
