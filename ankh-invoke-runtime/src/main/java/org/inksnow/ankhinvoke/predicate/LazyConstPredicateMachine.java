package org.inksnow.ankhinvoke.predicate;

import org.inksnow.ankhinvoke.PredicateService;
import org.jetbrains.annotations.NotNull;

public abstract class LazyConstPredicateMachine implements PredicateMachine {
  private boolean initialized;
  private boolean value;

  @Override
  public final boolean test(@NotNull PredicateService service, @NotNull String expression) {
    if (initialized) {
      return value;
    }
    synchronized (this) {
      if (initialized) {
        return value;
      }
      value = value();
      initialized = true;
      return value;
    }
  }

  protected abstract boolean value();
}
