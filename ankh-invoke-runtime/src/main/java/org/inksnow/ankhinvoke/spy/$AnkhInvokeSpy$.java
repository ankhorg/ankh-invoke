package org.inksnow.ankhinvoke.spy;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

@ApiStatus.Internal
@SuppressWarnings({"NotNullFieldNotInitialized", "ConstantValue"}) // for generated code call
public class $AnkhInvokeSpy$ {
  private static @NotNull MethodHandle BOOTSTRAP;
  private static @NotNull MethodHandle LOGGER_TRACE;
  private static @NotNull MethodHandle LOGGER_DEBUG;
  private static @NotNull MethodHandle LOGGER_INFO;
  private static @NotNull MethodHandle LOGGER_WARN;
  private static @NotNull MethodHandle LOGGER_ERROR;

  @ApiStatus.Internal
  private $AnkhInvokeSpy$() {
    throw new UnsupportedOperationException();
  }

  @ApiStatus.Internal
  public static @NotNull CallSite $bootstrap$(MethodHandles.@NotNull Lookup callerLookup, @NotNull String name, @NotNull MethodType type, int opcode, @NotNull String owner, @NotNull String describe) throws Throwable {
    return (CallSite) BOOTSTRAP.invokeExact(callerLookup, name, type, opcode, owner, describe);
  }

  @ApiStatus.Internal
  public static void $trace$(@NotNull String format, @Nullable Object @NotNull ... arguments) throws Throwable {
    LOGGER_TRACE.invokeExact(format, arguments);
  }

  @ApiStatus.Internal
  public static void $debug$(@NotNull String format, @Nullable Object @NotNull ... arguments) throws Throwable {
    LOGGER_DEBUG.invokeExact(format, arguments);
  }

  @ApiStatus.Internal
  public static void $info$(@NotNull String format, @Nullable Object @NotNull ... arguments) throws Throwable {
    LOGGER_INFO.invokeExact(format, arguments);
  }

  @ApiStatus.Internal
  public static void $warn$(@NotNull String format, @Nullable Object @NotNull ... arguments) throws Throwable {
    LOGGER_WARN.invokeExact(format, arguments);
  }

  @ApiStatus.Internal
  public static void $error$(@NotNull String format, @Nullable Object @NotNull ... arguments) throws Throwable {
    LOGGER_ERROR.invokeExact(format, arguments);
  }

  @ApiStatus.Internal
  public static void $initial$(@NotNull MethodHandle bootstrap, @NotNull MethodHandle trace, @NotNull MethodHandle debug, @NotNull MethodHandle info, @NotNull MethodHandle warn, @NotNull MethodHandle error) {
    if (BOOTSTRAP == null) {
      BOOTSTRAP = bootstrap;
    }
    if (LOGGER_TRACE == null) {
      LOGGER_TRACE = trace;
    }
    if (LOGGER_DEBUG == null) {
      LOGGER_DEBUG = debug;
    }
    if (LOGGER_INFO == null) {
      LOGGER_INFO = info;
    }
    if (LOGGER_WARN == null) {
      LOGGER_WARN = warn;
    }
    if (LOGGER_ERROR == null) {
      LOGGER_ERROR = error;
    }
  }
}
