package org.inksnow.ankhinvoke.bukkit.paper;

import org.objectweb.asm.commons.Remapper;

public final class PaperEnvironment {
  private PaperEnvironment() {
    throw new UnsupportedOperationException();
  }

  public static boolean hasPaperMapping() {
    return R$MappingEnvironment.hasPaperMapping();
  }

  public static String paperMappingCraftBukkitVersion() {
    return R$MappingEnvironment.legacyCBVersion();
  }

  public static Remapper createRemapper() {
    return new PaperObfHelperMapper();
  }
}
