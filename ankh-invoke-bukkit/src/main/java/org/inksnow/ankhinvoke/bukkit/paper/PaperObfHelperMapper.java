package org.inksnow.ankhinvoke.bukkit.paper;

import io.papermc.paper.util.MappingEnvironment;
import io.papermc.paper.util.ObfHelper;
import org.objectweb.asm.commons.Remapper;

public final class PaperObfHelperMapper extends Remapper {
  private final ObfHelper obfHelper = ObfHelper.INSTANCE;

  @Override
  public String map(String internalName) {
    if (MappingEnvironment.reobf()) {
      return internalName;
    }
    return obfHelper.deobfClassName(
        internalName.replace('/', '.')
    ).replace('.', '/');
  }

  @Override
  public String mapMethodName(String owner, String name, String descriptor) {
    if (MappingEnvironment.reobf()) {
      return name;
    }
    ObfHelper.ClassMapping mapping = obfHelper.mappingsByObfName().get(owner.replace('/', '.'));
    if (mapping == null) {
      return name;
    }
    String deobfName = mapping.methodsByObf().get(name + descriptor);
    if (deobfName == null) {
      return name;
    }
    return deobfName;
  }

  @Override
  public String mapFieldName(String owner, String name, String descriptor) {
    if (MappingEnvironment.reobf()) {
      return name;
    }
    ObfHelper.ClassMapping mapping = obfHelper.mappingsByObfName().get(owner.replace('/', '.'));
    if (mapping == null) {
      return name;
    }
    String deobfName = mapping.fieldsByObf().get(name);
    if (deobfName == null) {
      return name;
    }
    return deobfName;
  }
}
