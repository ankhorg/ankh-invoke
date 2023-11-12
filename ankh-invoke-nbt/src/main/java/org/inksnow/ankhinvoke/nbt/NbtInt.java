package org.inksnow.ankhinvoke.nbt;

import org.inksnow.ankhinvoke.bukkit.util.CraftBukkitVersion;
import org.inksnow.ankhinvoke.nbt.api.NbtIntLike;
import org.inksnow.ankhinvoke.nbt.ref.RefNbtTagInt;

public final class NbtInt extends NbtNumeric<RefNbtTagInt> implements NbtIntLike {
  private static final boolean OF_SUPPORTED = CraftBukkitVersion.v1_15_R1.isSupport();
  private static final NbtInt[] instanceCache = buildInstanceCache();

  NbtInt(RefNbtTagInt delegate) {
    super(delegate);
  }

  private static NbtInt[] buildInstanceCache() {
    NbtInt[] result = new NbtInt[1153];
    for (int i = 0; i < result.length; i++) {
      result[i] = new NbtInt(OF_SUPPORTED
          ? RefNbtTagInt.of(i - 128)
          : new RefNbtTagInt(i - 128));
    }
    return result;
  }

  public static NbtInt valueOf(int value) {
    return (value >= -128 && value <= 1024)
        ? instanceCache[value + 128]
        : new NbtInt(OF_SUPPORTED
        ? RefNbtTagInt.of(value)
        : new RefNbtTagInt(value));
  }

  static NbtInt fromNmsImpl(RefNbtTagInt delegate) {
    int value = delegate.asInt();
    return (value >= -128 && value <= 1024) ? instanceCache[value + 128] : new NbtInt(delegate);
  }

  @Override
  public NbtInt clone() {
    return this;
  }
}
