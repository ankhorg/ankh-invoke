package org.inksnow.ankhinvoke.nbt;

import org.inksnow.ankhinvoke.nbt.api.NbtComponentLike;
import org.inksnow.ankhinvoke.nbt.ref.RefCraftItemStack;

public final class NbtCraftItemComponent extends NbtCompound implements NbtComponentLike {
  NbtCraftItemComponent(RefCraftItemStack itemStack) {
    super(NbtUtils.getOrCreateTag(itemStack.handle));
  }
}
