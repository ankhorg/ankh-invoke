package org.inksnow.ankhinvoke.bukkit.paper;

import io.papermc.paper.util.MappingEnvironment;
import org.inksnow.ankhinvoke.util.DstUnsafe;
import org.objectweb.asm.commons.Remapper;

public final class PaperEnvironment {
  private PaperEnvironment() {
    throw new UnsupportedOperationException();
  }

  public static boolean hasPaperMapping() {
    try {
      MappingEnvironment.class.hashCode();
      return true;
    } catch (NoClassDefFoundError e) {
      return false;
    }
  }

  public static String paperMappingCraftBukkitVersion() {
    if (!hasPaperMapping()) {
      throw new IllegalStateException("Paper mapping environment not found");
    }
    try {
      return (String) MappingEnvironment.class
          .getDeclaredField("LEGACY_CB_VERSION")
          .get(null);
    } catch (Exception e) {
      throw DstUnsafe.throwImpl(e);
    }
  }

  public static Remapper createRemapper() {
    if (!hasPaperMapping()) {
      throw new IllegalStateException("Paper mapping environment not found");
    }
    return new PaperObfHelperMapper();
  }
}
