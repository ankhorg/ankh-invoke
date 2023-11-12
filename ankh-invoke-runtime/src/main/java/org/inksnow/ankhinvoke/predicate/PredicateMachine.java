package org.inksnow.ankhinvoke.predicate;

import org.inksnow.ankhinvoke.PredicateService;
import org.jetbrains.annotations.NotNull;

public interface PredicateMachine {
  boolean test(@NotNull PredicateService service, @NotNull String expression);

  interface Factory {
    String name();

    PredicateMachine create();
  }
}
