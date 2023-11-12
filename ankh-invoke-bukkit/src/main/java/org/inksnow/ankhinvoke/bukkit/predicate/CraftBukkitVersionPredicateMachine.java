package org.inksnow.ankhinvoke.bukkit.predicate;

import org.inksnow.ankhinvoke.bukkit.util.CraftBukkitVersion;
import org.inksnow.ankhinvoke.predicate.PredicateMachine;
import org.inksnow.ankhinvoke.predicate.RangePredicateMachine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CraftBukkitVersionPredicateMachine extends RangePredicateMachine<CraftBukkitVersion> {
  private static final @NotNull CraftBukkitVersionPredicateMachine INSTANCE = new CraftBukkitVersionPredicateMachine();

  @Override
  protected @Nullable CraftBukkitVersion value() {
    return CraftBukkitVersion.current();
  }

  @Override
  protected @Nullable CraftBukkitVersion parse(@NotNull String expression) {
    return CraftBukkitVersion.valueOf(expression);
  }

  public static final class Factory implements PredicateMachine.Factory {
    @Override
    public String name() {
      return "craftbukkit_version";
    }

    @Override
    public PredicateMachine create() {
      return INSTANCE;
    }
  }
}
