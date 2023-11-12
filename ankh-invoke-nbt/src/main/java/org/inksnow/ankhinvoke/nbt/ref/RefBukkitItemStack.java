package org.inksnow.ankhinvoke.nbt.ref;

import org.bukkit.inventory.meta.ItemMeta;
import org.inksnow.ankhinvoke.comments.HandleBy;

@HandleBy(reference = "org/bukkit/inventory/ItemStack", predicates = "craftbukkit_version:[v1_12_R1,)")
public final class RefBukkitItemStack {
  @HandleBy(reference = "Lorg/bukkit/inventory/ItemStack;meta:Lorg/bukkit/inventory/meta/ItemMeta;", useAccessor = true, predicates = "craftbukkit_version:[v1_12_R1,)")
  public ItemMeta meta;
}
