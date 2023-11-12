package org.inksnow.ankhinvoke.predicate;

import org.inksnow.ankhinvoke.PredicateService;
import org.jetbrains.annotations.NotNull;

public final class FalseMachine implements PredicateMachine {
  public static final @NotNull FalseMachine INSTANCE = new FalseMachine();

  private FalseMachine() {
    //
  }

  @Override
  public boolean test(@NotNull PredicateService service, @NotNull String expression) {
    return false;
  }

  public static final class Factory implements PredicateMachine.Factory {
    @Override
    public String name() {
      return "false";
    }

    @Override
    public PredicateMachine create() {
      return INSTANCE;
    }
  }
}
