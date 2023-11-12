package org.inksnow.ankhinvoke.asm;

import org.inksnow.ankhinvoke.AnkhInvoke;
import org.inksnow.ankhinvoke.ClassPoolService;
import org.inksnow.ankhinvoke.classpool.ClassPoolNode;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PooledClassWriter extends ClassWriter {
  private static final Logger logger = LoggerFactory.getLogger(AnkhInvoke.ANKH_INVOKE_PACKAGE);
  private final @NotNull ClassPoolService classPoolService;

  public PooledClassWriter(@NotNull ClassPoolService classPoolService, int flags) {
    super(flags);
    this.classPoolService = classPoolService;
  }

  @Override
  protected String getCommonSuperClass(String type1, String type2) {
    ClassPoolNode class1 = classPoolService.get(type1);
    if (class1 == null) {
      throw new TypeNotPresentException(type1, null);
    }
    ClassPoolNode class2 = classPoolService.get(type2);
    if (class2 == null) {
      throw new TypeNotPresentException(type2, null);
    }
    if (classPoolService.isAssignableFrom(type1, type2)) {
      return type1;
    }
    if (classPoolService.isAssignableFrom(type2, type1)) {
      return type2;
    }
    if (class1.isInterface() || class2.isInterface()) {
      return "java/lang/Object";
    } else {
      do {
        type1 = class1.superClass();
        // only class1 is Object, type1 can be null, but anything instanceof Object is true, so it's safe.
        // noinspection DataFlowIssue
        class1 = classPoolService.get(type1);
        if (class1 == null) {
          throw new TypeNotPresentException(type1, null);
        }
      } while (!classPoolService.isAssignableFrom(type1, type2));
      return type1;
    }
  }

  @Override
  protected ClassLoader getClassLoader() {
    logger.warn("PooledClassWriter#getClassLoader called, it won't return valuable result");
    return super.getClassLoader();
  }
}
