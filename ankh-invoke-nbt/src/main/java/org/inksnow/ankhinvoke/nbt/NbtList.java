package org.inksnow.ankhinvoke.nbt;

import org.inksnow.ankhinvoke.bukkit.util.CraftBukkitVersion;
import org.inksnow.ankhinvoke.nbt.api.NbtListLike;
import org.inksnow.ankhinvoke.nbt.ref.RefNbtBase;
import org.inksnow.ankhinvoke.nbt.ref.RefNbtTagEnd;
import org.inksnow.ankhinvoke.nbt.ref.RefNbtTagList;

public final class NbtList extends NbtCollection<RefNbtTagList, Nbt<?>> implements NbtListLike {
  private final static boolean THROW_INDEX_SUPPORT = CraftBukkitVersion.v1_13_R1.isSupport();
  private final static boolean RETURN_SET_SUPPORT = CraftBukkitVersion.v1_13_R1.isSupport();
  private final static boolean BOOLEAN_ADD_SUPPORT = CraftBukkitVersion.v1_13_R1.isSupport();
  private final static boolean INDEX_ADD_SUPPORT = CraftBukkitVersion.v1_14_R1.isSupport();

  NbtList(RefNbtTagList delegate) {
    super(delegate);
  }

  public NbtList() {
    super(new RefNbtTagList());
  }

  private static Nbt<?> nms2nbt(RefNbtBase nms) {
    return Nbt.fromNms(nms);
  }

  private static RefNbtBase nbt2nms(Nbt<?> nbt) {
    return nbt == null ? null : nbt.delegate;
  }

  @Override
  public Nbt<?> get(int index) {
    RefNbtBase result = delegate.get(index);
    if (THROW_INDEX_SUPPORT && result instanceof RefNbtTagEnd && (index < 0 || index >= delegate.size())) {
      throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + delegate.size());
    }
    return nms2nbt(result);
  }

  @Override
  public int size() {
    return delegate.size();
  }

  @Override
  public Nbt<?> set(int index, Nbt<?> element) {
    if (RETURN_SET_SUPPORT) {
      return nms2nbt(delegate.set1(index, nbt2nms(element)));
    } else if (index >= 0 && index < delegate.size()) {
      Nbt<?> previous = nms2nbt(delegate.get(index));
      delegate.set0(index, nbt2nms(element));
      return previous;
    } else {
      throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + delegate.size());
    }
  }

  @Override
  public void add(int index, Nbt<?> element) {
    if (INDEX_ADD_SUPPORT) {
      delegate.add2(index, nbt2nms(element));
    } else {
      int addedIndex = delegate.size();
      if (index > addedIndex) {
        throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + delegate.size());
      }
      RefNbtBase nms = nbt2nms(element);
      if (BOOLEAN_ADD_SUPPORT) {
        delegate.add1(nms);
      } else {
        delegate.add0(nms);
      }
      if (index == addedIndex) {
        return;
      }
      if (RETURN_SET_SUPPORT) {
        for (int i = index; i < addedIndex; i++) {
          nms = delegate.set1(i, nms);
        }
        delegate.set1(addedIndex, nms);
      } else {
        for (int i = index; i < addedIndex; i++) {
          RefNbtBase oldValue = delegate.get(i);
          delegate.set0(i, nms);
          nms = oldValue;
        }
        delegate.set0(addedIndex, nms);
      }
    }
  }

  @Override
  public Nbt<?> remove(int index) {
    return nms2nbt(delegate.remove(index));
  }

  @Override
  public NbtList clone() {
    return new NbtList(cloneNms());
  }
}
