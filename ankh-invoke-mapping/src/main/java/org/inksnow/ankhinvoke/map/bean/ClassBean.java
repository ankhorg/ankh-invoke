package org.inksnow.ankhinvoke.map.bean;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ClassBean {
  private final String raw;
  private final String remapped;

  private final Map<FieldBean, String> fieldMap;
  private final Map<MethodBean, String> methodMap;

  public ClassBean(String raw, String remapped) {
    this.raw = raw;
    this.remapped = remapped;
    this.fieldMap = new LinkedHashMap<>();
    this.methodMap = new LinkedHashMap<>();
  }

  public String raw() {
    return raw;
  }

  public String remapped() {
    return remapped;
  }

  public Map<FieldBean, String> fieldMap() {
    return fieldMap;
  }

  public Map<MethodBean, String> methodMap() {
    return methodMap;
  }

  @Override
  public String toString() {
    return raw + " -> " + remapped;
  }
}
