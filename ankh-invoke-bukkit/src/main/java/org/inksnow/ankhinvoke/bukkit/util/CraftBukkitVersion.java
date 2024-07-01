package org.inksnow.ankhinvoke.bukkit.util;

import org.bukkit.Bukkit;
import org.inksnow.ankhinvoke.bukkit.paper.PaperEnvironment;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  v1_20_R2,
  v1_20_R3,
  v1_20_R4;

  private static final @NotNull Logger logger = LoggerFactory.getLogger(CraftBukkitVersion.class);
  private static final @NotNull CraftBukkitVersion CURRENT = createCurrent();

  public static @NotNull CraftBukkitVersion current() {
    return CURRENT;
  }

  public boolean isSupport() {
    return current().ordinal() >= ordinal();
  }

  private static @NotNull CraftBukkitVersion createCurrent() {
    String version;

    if (PaperEnvironment.hasPaperMapping()) {
      version = PaperEnvironment.paperMappingCraftBukkitVersion();
    } else {
      version = Bukkit.getServer().getClass().getName().split("\\.")[3];
    }

    try {
      return valueOf(version);
    } catch (IllegalArgumentException e) {
      logger.warn("Unknown CraftBukkit version: {}, use fallback ALL", version);
      return ALL;
    }
  }
}
