package org.inksnow.ankhinvoke.nbt.ref;

import org.inksnow.ankhinvoke.comments.HandleBy;

import java.util.Map;
import java.util.Set;

@HandleBy(reference = "org/bukkit/inventory/meta/ItemMeta", predicates = "craftbukkit_version:[v1_12_R1,)")
public final class RefCraftMetaItem {
  @HandleBy(reference = "Lorg/bukkit/craftbukkit/v1_12_R1/inventory/CraftMetaItem;HANDLED_TAGS:Ljava/util/Set;", useAccessor = true, predicates = "craftbukkit_version:[v1_12_R1,)")
  public static Set<String> HANDLED_TAGS;
  @HandleBy(reference = "Lorg/bukkit/craftbukkit/v1_12_R1/inventory/CraftMetaItem;unhandledTags:Ljava/util/Map;", useAccessor = true, predicates = "craftbukkit_version:[v1_12_R1,)")
  public Map<String, RefNbtBase> unhandledTags;

  @HandleBy(reference = "Lorg/bukkit/craftbukkit/v1_17_R1/inventory/CraftMetaItem;applyToItem(Lnet/minecraft/nbt/CompoundTag;)V", useAccessor = true, predicates = "craftbukkit_version:[v1_17_R1,)")
  @HandleBy(reference = "Lorg/bukkit/craftbukkit/v1_12_R1/inventory/CraftMetaItem;applyToItem(Lnet/minecraft/server/v1_12_R1/NBTTagCompound;)V", useAccessor = true, predicates = "craftbukkit_version:[v1_12_R1,)")
  public native void applyToItem(RefNbtTagCompound itemTag);
}
