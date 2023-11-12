package org.inksnow.ankhinvoke.bukkit.predicate;

import org.inksnow.ankhinvoke.predicate.LazyConstPredicateMachine;
import org.inksnow.ankhinvoke.predicate.PredicateMachine;
import org.jetbrains.annotations.NotNull;

public final class IsSpigotPredicateMachine extends LazyConstPredicateMachine {
  private static final @NotNull IsSpigotPredicateMachine INSTANCE = new IsSpigotPredicateMachine();

  private IsSpigotPredicateMachine() {
    //
  }

  @Override
  protected boolean value() {
    try {
      Class.forName("org.spigotmc.SpigotConfig");
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  public static final class Factory implements PredicateMachine.Factory {
    @Override
    public String name() {
      return "is_spigot";
    }

    @Override
    public PredicateMachine create() {
      return INSTANCE;
    }
  }
}
