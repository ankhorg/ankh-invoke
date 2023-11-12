package org.inksnow.ankhinvoke.nbt;

import org.inksnow.ankhinvoke.bukkit.util.CraftBukkitVersion;
import org.inksnow.ankhinvoke.nbt.api.NbtEndLike;
import org.inksnow.ankhinvoke.nbt.ref.RefNbtBase;
import org.inksnow.ankhinvoke.nbt.ref.RefNbtTagEnd;

public final class NbtEnd extends Nbt<RefNbtTagEnd> implements NbtEndLike {
  private static final boolean INSTANCE_FIELD_SUPPORT = CraftBukkitVersion.v1_15_R1.isSupport();

  static final NbtEnd INSTANCE = INSTANCE_FIELD_SUPPORT
      ? new NbtEnd(RefNbtTagEnd.INSTANCE)
      : new NbtEnd((RefNbtTagEnd) RefNbtBase.createTag((byte) 0));

  private NbtEnd(RefNbtTagEnd delegate) {
    super(delegate);
  }

  public static NbtEnd instance() {
    return INSTANCE;
  }

  @Override
  public Nbt<RefNbtTagEnd> clone() {
    return this;
  }
}
