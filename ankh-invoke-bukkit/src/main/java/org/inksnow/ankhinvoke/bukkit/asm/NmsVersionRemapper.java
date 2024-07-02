package org.inksnow.ankhinvoke.bukkit.asm;

import org.inksnow.ankhinvoke.bukkit.paper.PaperEnvironment;
import org.inksnow.ankhinvoke.bukkit.util.CraftBukkitVersion;
import org.inksnow.ankhinvoke.comments.InternalName;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.commons.Remapper;

public class NmsVersionRemapper extends Remapper {
  @Override
  public @InternalName @NotNull String map(@InternalName @NotNull String name) {
    String matchedPrefix;
    if (name.startsWith("org/bukkit/craftbukkit/")) {
      matchedPrefix = "org/bukkit/craftbukkit/";
    } else if (!CraftBukkitVersion.v1_17_R1.isSupport() && name.startsWith("net/minecraft/server/")) {
      matchedPrefix = "net/minecraft/server/";
    } else {
      return name;
    }
    int splitIndex = name.indexOf('/', matchedPrefix.length());
    if (splitIndex == -1) {
      return name;
    } else if (PaperEnvironment.hasPaperMapping()) {
      return matchedPrefix + name.substring(splitIndex + 1);
    } else {
      return matchedPrefix + CraftBukkitVersion.current() + name.substring(splitIndex);
    }
  }
}
