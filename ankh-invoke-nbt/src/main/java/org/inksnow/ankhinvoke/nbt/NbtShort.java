package org.inksnow.ankhinvoke.nbt;

import org.inksnow.ankhinvoke.bukkit.util.CraftBukkitVersion;
import org.inksnow.ankhinvoke.nbt.api.NbtShortLike;
import org.inksnow.ankhinvoke.nbt.ref.RefNbtTagShort;

public final class NbtShort extends NbtNumeric<RefNbtTagShort> implements NbtShortLike {
  private static final boolean OF_SUPPORTED = CraftBukkitVersion.v1_15_R1.isSupport();
  private static final NbtShort[] instanceCache = buildInstanceCache();

  NbtShort(RefNbtTagShort delegate) {
    super(delegate);
  }

  private static NbtShort[] buildInstanceCache() {
    NbtShort[] result = new NbtShort[1153];
    for (int i = 0; i < result.length; i++) {
      result[i] = new NbtShort(OF_SUPPORTED
          ? RefNbtTagShort.of((short) (i - 128))
          : new RefNbtTagShort((short) (i - 128)));
    }
    return result;
  }

  public static NbtShort valueOf(short value) {
    return (value >= -128 && value <= 1024)
        ? instanceCache[value + 128]
        : new NbtShort(OF_SUPPORTED
        ? RefNbtTagShort.of(value)
        : new RefNbtTagShort(value));
  }

  static NbtShort fromNmsImpl(RefNbtTagShort delegate) {
    short value = delegate.asShort();
    return (value >= -128 && value <= 1024) ? instanceCache[value + 128] : new NbtShort(delegate);
  }

  @Override
  public NbtShort clone() {
    return this;
  }
}
