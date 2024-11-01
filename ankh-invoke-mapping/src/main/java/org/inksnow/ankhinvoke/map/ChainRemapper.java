package org.inksnow.ankhinvoke.map;

import org.objectweb.asm.commons.Remapper;

public final class ChainRemapper extends Remapper {
  private final Remapper[] remappers;

  public ChainRemapper(Remapper... remappers) {
    this.remappers = remappers;
  }

  @Override
  public String map(String internalName) {
    for (Remapper remapper : remappers) {
      String result = remapper.map(internalName);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  @Override
  public String mapFieldName(String owner, String name, String descriptor) {
    for (Remapper remapper : remappers) {
      String result = remapper.mapFieldName(owner, name, descriptor);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  @Override
  public String mapMethodName(String owner, String name, String descriptor) {
    for (Remapper remapper : remappers) {
      String result = remapper.mapMethodName(owner, name, descriptor);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  @Override
  public String mapRecordComponentName(String owner, String name, String descriptor) {
    for (Remapper remapper : remappers) {
      String result = remapper.mapRecordComponentName(owner, name, descriptor);
      if (result != null) {
        return result;
      }
    }
    return null;
  }
}
