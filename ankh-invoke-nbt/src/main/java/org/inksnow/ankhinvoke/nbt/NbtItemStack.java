package org.inksnow.ankhinvoke.nbt;

import org.bukkit.inventory.ItemStack;
import org.inksnow.ankhinvoke.nbt.api.NbtComponentLike;
import org.inksnow.ankhinvoke.nbt.ref.RefBukkitItemStack;
import org.inksnow.ankhinvoke.nbt.ref.RefCraftItemStack;
import org.inksnow.ankhinvoke.nbt.ref.RefCraftMetaItem;
import org.inksnow.ankhinvoke.nbt.ref.RefNbtTagCompound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class NbtItemStack {
  private final ItemStack itemStack;
  private final RefBukkitItemStack bukkitItemStack;
  private final RefCraftItemStack craftItemStack;

  public NbtItemStack(ItemStack itemStack) {
    this.itemStack = itemStack;
    this.bukkitItemStack = (RefBukkitItemStack) (Object) itemStack;
    if ((Object) itemStack instanceof RefCraftItemStack) {
      this.craftItemStack = (RefCraftItemStack) (Object) itemStack;
    } else {
      this.craftItemStack = null;
    }
  }

  public NbtComponentLike getDirectTag() {
    if (craftItemStack == null) {
      return new NbtBukkitItemComponent(itemStack);
    } else {
      return new NbtCraftItemComponent(craftItemStack);
    }
  }

  @Nullable
  public NbtCompound getTag() {
    if (craftItemStack == null) {
      RefCraftMetaItem meta = (RefCraftMetaItem) (Object) bukkitItemStack.meta;
      if (meta == null) {
        return new NbtCompound();
      } else {
        NbtCompound compound = new NbtCompound();
        meta.applyToItem(compound.delegate);
        return compound;
      }
    } else {
      RefNbtTagCompound tag = craftItemStack.handle.getTag();
      return tag == null ? null : new NbtCompound(craftItemStack.handle.getTag());
    }
  }

  public void setTag(@NotNull NbtCompound compound) {
    if (craftItemStack == null) {
      bukkitItemStack.meta = null;
      RefCraftItemStack craftItemStack = RefCraftItemStack.asCraftCopy(itemStack);
      craftItemStack.handle.setTag(compound.delegate);
      bukkitItemStack.meta = ((ItemStack) (Object) craftItemStack).getItemMeta();
    } else {
      craftItemStack.handle.setTag(compound.delegate);
    }
  }

  public @NotNull NbtCompound getOrCreateTag() {
    if (craftItemStack == null) {
      RefCraftMetaItem meta = (RefCraftMetaItem) (Object) bukkitItemStack.meta;
      if (meta == null) {
        return new NbtCompound();
      } else {
        NbtCompound compound = new NbtCompound();
        meta.applyToItem(compound.delegate);
        return compound;
      }
    } else {
      return new NbtCompound(NbtUtils.getOrCreateTag(craftItemStack.handle));
    }
  }

  public ItemStack asBukkitCopy() {
    if (craftItemStack == null) {
      return itemStack.clone();
    } else {
      return RefCraftItemStack.asBukkitCopy(craftItemStack.handle);
    }
  }

  public ItemStack asCraftCopy() {
    if (craftItemStack == null) {
      return (ItemStack) (Object) RefCraftItemStack.asCraftCopy(itemStack);
    } else {
      return itemStack.clone();
    }
  }

  public ItemStack asCopy() {
    return itemStack.clone();
  }

  public boolean isBukkitItemStack() {
    return craftItemStack == null;
  }

  public boolean isCraftItemStack() {
    return craftItemStack != null;
  }

  public ItemStack asItemStack() {
    return itemStack;
  }

  @Override
  public NbtItemStack clone() throws CloneNotSupportedException {
    return new NbtItemStack(itemStack.clone());
  }
}
