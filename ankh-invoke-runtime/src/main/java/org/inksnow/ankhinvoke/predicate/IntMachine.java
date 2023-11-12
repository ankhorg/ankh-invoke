package org.inksnow.ankhinvoke.predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IntMachine extends RangePredicateMachine<Integer> {
  private final int value;

  public IntMachine(int value) {
    this.value = value;
  }

  @Override
  protected @Nullable Integer value() {
    return value;
  }

  @Override
  protected @Nullable Integer parse(@NotNull String expression) {
    return Integer.parseInt(expression);
  }
}
