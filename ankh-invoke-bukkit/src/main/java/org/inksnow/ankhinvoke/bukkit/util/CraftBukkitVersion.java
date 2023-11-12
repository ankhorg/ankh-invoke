package org.inksnow.ankhinvoke.bukkit.util;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public enum CraftBukkitVersion {
  ALL,
  v1_12_R1,
  v1_13_R1,
  v1_13_R2,
  v1_14_R1,
  v1_15_R1,
  v1_16_R1,
  v1_16_R2,
  v1_16_R3,
  v1_17_R1,
  v1_18_R1,
  v1_18_R2,
  v1_19_R1,
  v1_19_R2,
  v1_19_R3,
  v1_20_R1,
  v1_20_R2;

  private static final @NotNull CraftBukkitVersion CURRENT = valueOf(Bukkit.getServer().getClass().getName().split("\\.")[3]);

  public static @NotNull CraftBukkitVersion current() {
    return CURRENT;
  }

  public boolean isSupport() {
    return current().ordinal() >= ordinal();
  }
}
