package org.inksnow.ankhinvoke.predicate;

import org.inksnow.ankhinvoke.PredicateService;
import org.jetbrains.annotations.NotNull;

public final class TrueMachine implements PredicateMachine {
  public static final @NotNull TrueMachine INSTANCE = new TrueMachine();

  private TrueMachine() {
    //
  }

  @Override
  public boolean test(@NotNull PredicateService service, @NotNull String expression) {
    return false;
  }

  public static final class Factory implements PredicateMachine.Factory {
    @Override
    public String name() {
      return "true";
    }

    @Override
    public PredicateMachine create() {
      return INSTANCE;
    }
  }
}
