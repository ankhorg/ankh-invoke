package org.inksnow.ankhinvoke.nbt;

import org.bukkit.plugin.java.JavaPlugin;
import org.inksnow.ankhinvoke.AnkhInvoke;
import org.inksnow.ankhinvoke.bukkit.AnkhInvokeBukkit;
import org.jetbrains.annotations.NotNull;

public class AnkhInvokeNbt extends JavaPlugin {
  public static final @NotNull String ANKH_INVOKE_NBT_PACKAGE = new String("org.inksnow.ankhinvoke.nbt");
  private static final AnkhInvoke ankhInvoke;


  static {
    if (!AnkhInvokeNbt.class.getName().equals(ANKH_INVOKE_NBT_PACKAGE + ".AnkhInvokeNbt")) {
      throw new IllegalStateException("AnkhInvokeNbt class remapped but const not remapped");
    }

    ankhInvoke = AnkhInvokeBukkit.forBukkit(AnkhInvokeNbt.class)
        .reference()
        /**/.appendPackage(ANKH_INVOKE_NBT_PACKAGE + ".ref")
        /**/.build()
        .referenceRemap()
        /**/.setApplyMapRegistry("ankh-invoke-nbt")
        /**/.build()
        .build();


    ankhInvoke.get(ANKH_INVOKE_NBT_PACKAGE + ".NbtFloat");
    ankhInvoke.get(ANKH_INVOKE_NBT_PACKAGE + ".NbtShort");
    ankhInvoke.get(ANKH_INVOKE_NBT_PACKAGE + ".NbtBukkitItemComponent");
    ankhInvoke.get(ANKH_INVOKE_NBT_PACKAGE + ".NbtDouble");
    ankhInvoke.get(ANKH_INVOKE_NBT_PACKAGE + ".NbtString");
    ankhInvoke.get(ANKH_INVOKE_NBT_PACKAGE + ".NbtNumeric");
    ankhInvoke.get(ANKH_INVOKE_NBT_PACKAGE + ".NbtLongArray");
    ankhInvoke.get(ANKH_INVOKE_NBT_PACKAGE + ".NbtCollection");
    ankhInvoke.get(ANKH_INVOKE_NBT_PACKAGE + ".NbtItemStack");
    ankhInvoke.get(ANKH_INVOKE_NBT_PACKAGE + ".NbtCompound");
    ankhInvoke.get(ANKH_INVOKE_NBT_PACKAGE + ".NbtByte");
    ankhInvoke.get(ANKH_INVOKE_NBT_PACKAGE + ".NbtEnd");
    ankhInvoke.get(ANKH_INVOKE_NBT_PACKAGE + ".NbtList");
    ankhInvoke.get(ANKH_INVOKE_NBT_PACKAGE + ".NbtInt");
    ankhInvoke.get(ANKH_INVOKE_NBT_PACKAGE + ".NbtCraftItemComponent");
    ankhInvoke.get(ANKH_INVOKE_NBT_PACKAGE + ".Nbt");
    ankhInvoke.get(ANKH_INVOKE_NBT_PACKAGE + ".NbtIntArray");
    ankhInvoke.get(ANKH_INVOKE_NBT_PACKAGE + ".NbtByteArray");
    ankhInvoke.get(ANKH_INVOKE_NBT_PACKAGE + ".NbtLong");
    ankhInvoke.get(ANKH_INVOKE_NBT_PACKAGE + ".NbtType");
    ankhInvoke.get(ANKH_INVOKE_NBT_PACKAGE + ".NbtUtils");
  }

  @Override
  public void onEnable() {
    new TestTargets().run();
  }
}
