package org.inksnow.ankhinvoke.bukkit.predicate;

import org.inksnow.ankhinvoke.predicate.LazyConstPredicateMachine;
import org.inksnow.ankhinvoke.predicate.PredicateMachine;
import org.jetbrains.annotations.NotNull;

public final class IsPaperPredicateMachine extends LazyConstPredicateMachine {
  private static final @NotNull IsPaperPredicateMachine INSTANCE = new IsPaperPredicateMachine();

  private IsPaperPredicateMachine() {
    //
  }

  @Override
  protected boolean value() {
    try {
      Class.forName("com.destroystokyo.paper.PaperConfig");
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  public static final class Factory implements PredicateMachine.Factory {
    @Override
    public String name() {
      return "is_paper";
    }

    @Override
    public PredicateMachine create() {
      return INSTANCE;
    }
  }
}
