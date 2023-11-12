package org.inksnow.ankhinvoke.predicate;

import org.inksnow.ankhinvoke.PredicateService;
import org.jetbrains.annotations.NotNull;

public final class NotMachine implements PredicateMachine {
  public static final @NotNull NotMachine INSTANCE = new NotMachine();

  private NotMachine() {
    //
  }

  @Override
  public boolean test(@NotNull PredicateService service, @NotNull String expression) {
    return !service.testPredicate(expression);
  }

  public static final class Factory implements PredicateMachine.Factory {
    @Override
    public String name() {
      return "not";
    }

    @Override
    public PredicateMachine create() {
      return INSTANCE;
    }
  }
}
