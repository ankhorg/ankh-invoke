package org.inksnow.ankhinvoke.map;

import org.objectweb.asm.commons.Remapper;

public class FixInnerClassRemapper extends Remapper {
  private final Remapper delegate;

  public FixInnerClassRemapper(Remapper delegate) {
    this.delegate = delegate;
  }

  public String mapAnnotationAttributeName(String descriptor, String name) {
    return delegate.mapAnnotationAttributeName(descriptor, name);
  }

  public String mapMethodName(String owner, String name, String descriptor) {
    return delegate.mapMethodName(owner, name, descriptor);
  }

  public String mapInvokeDynamicMethodName(String name, String descriptor) {
    return delegate.mapInvokeDynamicMethodName(name, descriptor);
  }

  public String mapRecordComponentName(String owner, String name, String descriptor) {
    return delegate.mapRecordComponentName(owner, name, descriptor);
  }

  public String mapFieldName(String owner, String name, String descriptor) {
    return delegate.mapFieldName(owner, name, descriptor);
  }

  public String mapPackageName(String name) {
    return delegate.mapPackageName(name);
  }

  public String mapModuleName(String name) {
    return delegate.mapModuleName(name);
  }

  public String map(String internalName) {
    int index = internalName.lastIndexOf('$');
    if (index != -1) {
      String outer = internalName.substring(0, index);
      String inner = internalName.substring(index + 1);
      return map(outer) + "$" + inner;
    }
    return delegate.map(internalName);
  }
}
