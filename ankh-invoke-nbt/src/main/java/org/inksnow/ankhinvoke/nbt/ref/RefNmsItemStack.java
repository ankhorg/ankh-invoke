package org.inksnow.ankhinvoke.nbt.ref;

import org.inksnow.ankhinvoke.comments.HandleBy;

@HandleBy(reference = "net/minecraft/world/item/ItemStack", predicates = "craftbukkit_version:[v1_17_R1,)")
@HandleBy(reference = "net/minecraft/server/v1_12_R1/ItemStack", predicates = "craftbukkit_version:[v1_12_R1,)")
public final class RefNmsItemStack {
  @HandleBy(reference = "Lnet/minecraft/server/v1_12_R1/ItemStack;<init>(Lnet/minecraft/server/v1_12_R1/Item;)V", predicates = "craftbukkit_version:[v1_12_R1,v1_13_R1)")
  public RefNmsItemStack(RefItem item) {
    throw new UnsupportedOperationException();
  }

  @HandleBy(reference = "Lnet/minecraft/world/item/ItemStack;<init>(Lnet/minecraft/world/level/ItemLike;)V", predicates = "craftbukkit_version:[v1_17_R1,)")
  @HandleBy(reference = "Lnet/minecraft/server/v1_13_R2/ItemStack;<init>(Lnet/minecraft/server/v1_13_R2/IMaterial;)V", predicates = "craftbukkit_version:[v1_13_R1,)")
  public RefNmsItemStack(RefIMaterial imaterial) {
    throw new UnsupportedOperationException();
  }

  @HandleBy(reference = "Lnet/minecraft/world/item/ItemStack;hasTag()Z", predicates = "craftbukkit_version:[v1_17_R1,)")
  @HandleBy(reference = "Lnet/minecraft/server/v1_12_R1/ItemStack;hasTag()Z", predicates = "craftbukkit_version:[v1_12_R1,)")
  public native boolean hasTag();

  @HandleBy(reference = "Lnet/minecraft/world/item/ItemStack;getTag()Lnet/minecraft/nbt/CompoundTag;", predicates = "craftbukkit_version:[v1_17_R1,)")
  @HandleBy(reference = "Lnet/minecraft/server/v1_12_R1/ItemStack;getTag()Lnet/minecraft/server/v1_12_R1/NBTTagCompound;", predicates = "craftbukkit_version:[v1_12_R1,)")
  public native RefNbtTagCompound getTag();

  @HandleBy(reference = "Lnet/minecraft/world/item/ItemStack;setTag(Lnet/minecraft/nbt/CompoundTag;)V", predicates = "craftbukkit_version:[v1_17_R1,)")
  @HandleBy(reference = "Lnet/minecraft/server/v1_12_R1/ItemStack;setTag(Lnet/minecraft/server/v1_12_R1/NBTTagCompound;)V", predicates = "craftbukkit_version:[v1_12_R1,)")
  public native void setTag(RefNbtTagCompound nbt);
}
