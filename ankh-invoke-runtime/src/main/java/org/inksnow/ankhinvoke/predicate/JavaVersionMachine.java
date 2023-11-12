package org.inksnow.ankhinvoke.predicate;

import org.inksnow.ankhinvoke.util.JavaVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JavaVersionMachine extends RangePredicateMachine<JavaVersion> {
  public static final @NotNull JavaVersionMachine INSTANCE = new JavaVersionMachine();

  private JavaVersionMachine() {
    //
  }

  @Override
  protected @Nullable JavaVersion value() {
    return JavaVersion.current();
  }

  @Override
  protected @Nullable JavaVersion parse(@NotNull String expression) {
    return JavaVersion.valueOf(expression.startsWith("VERSION_")
        ? expression
        : ("VERSION_" + expression.replace('.', '_')));
  }

  public static final class Factory implements PredicateMachine.Factory {
    @Override
    public String name() {
      return "java_version";
    }

    @Override
    public PredicateMachine create() {
      return INSTANCE;
    }
  }
}
