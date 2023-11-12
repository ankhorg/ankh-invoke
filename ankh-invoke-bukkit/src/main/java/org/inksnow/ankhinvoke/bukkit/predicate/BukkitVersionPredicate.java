package org.inksnow.ankhinvoke.bukkit.predicate;

import org.bukkit.Bukkit;
import org.inksnow.ankhinvoke.predicate.PredicateMachine;
import org.inksnow.ankhinvoke.predicate.VersionMachine;
import org.jetbrains.annotations.NotNull;

public class BukkitVersionPredicate extends VersionMachine {
  private static final @NotNull BukkitVersionPredicate INSTANCE = new BukkitVersionPredicate();

  private BukkitVersionPredicate() {
    //
  }

  @Override
  protected String value() {
    return Bukkit.getBukkitVersion();
  }

  public static final class Factory implements PredicateMachine.Factory {
    @Override
    public String name() {
      return "bukkit_version";
    }

    @Override
    public PredicateMachine create() {
      return INSTANCE;
    }
  }
}
