package org.inksnow.ankhinvoke.bukkit.paper;

import org.objectweb.asm.commons.Remapper;

public final class PaperObfHelperMapper extends Remapper {
  private final boolean reobf = R$MappingEnvironment.reobf();
  private final Object obfHelper = R$ObfHelper.INSTANCE();

  @Override
  public String map(String internalName) {
    if (reobf) {
      return internalName;
    }
    return R$ObfHelper.deobfClassName(
        obfHelper,
        internalName.replace('/', '.')
    ).replace('.', '/');
  }

  @Override
  public String mapMethodName(String owner, String name, String descriptor) {
    if (reobf) {
      return name;
    }
    Object mapping = R$ObfHelper.mappingsByObfName(obfHelper)
        .get(owner.replace('/', '.'));
    if (mapping == null) {
      return name;
    }
    String deobfName = R$ObfHelper.R$ClassMapping.methodsByObf(mapping).get(name + descriptor);
    if (deobfName == null) {
      return name;
    }
    return deobfName;
  }

  @Override
  public String mapFieldName(String owner, String name, String descriptor) {
    if (reobf) {
      return name;
    }
    Object mapping = R$ObfHelper.mappingsByObfName(obfHelper).get(owner.replace('/', '.'));
    if (mapping == null) {
      return name;
    }
    String deobfName = R$ObfHelper.R$ClassMapping.fieldsByObf(mapping).get(name);
    if (deobfName == null) {
      return name;
    }
    return deobfName;
  }
}
