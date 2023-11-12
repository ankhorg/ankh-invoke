package org.inksnow.ankhinvoke.bukkit.predicate;

import org.inksnow.ankhinvoke.predicate.LazyConstPredicateMachine;
import org.inksnow.ankhinvoke.predicate.PredicateMachine;
import org.jetbrains.annotations.NotNull;

public final class IsFoliaPredicateMachine extends LazyConstPredicateMachine {
  private static final @NotNull IsFoliaPredicateMachine INSTANCE = new IsFoliaPredicateMachine();

  private IsFoliaPredicateMachine() {
    //
  }

  @Override
  protected boolean value() {
    try {
      Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  public static final class Factory implements PredicateMachine.Factory {
    @Override
    public String name() {
      return "is_folia";
    }

    @Override
    public PredicateMachine create() {
      return INSTANCE;
    }
  }
}
