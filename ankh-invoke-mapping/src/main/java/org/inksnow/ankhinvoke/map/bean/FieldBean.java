package org.inksnow.ankhinvoke.map.bean;

import java.util.Objects;

public final class FieldBean {
  private final String owner;
  private final String name;
  private final String desc;

  public FieldBean(String owner, String name, String desc) {
    Objects.requireNonNull(owner);
    Objects.requireNonNull(name);
    Objects.requireNonNull(desc);

    this.owner = owner;
    this.name = name;
    this.desc = desc;
  }

  public String owner() {
    return owner;
  }

  public String name() {
    return name;
  }

  public String desc() {
    return desc;
  }

  @Override
  public String toString() {
    return name + ":" + desc;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    FieldBean fieldBean = (FieldBean) o;

    if (!owner.equals(fieldBean.owner)) return false;
    if (!name.equals(fieldBean.name)) return false;
    return desc.equals(fieldBean.desc);
  }

  @Override
  public int hashCode() {
    int result = owner.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + desc.hashCode();
    return result;
  }
}
