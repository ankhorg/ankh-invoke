package org.inksnow.ankhinvoke;

import org.inksnow.ankhinvoke.classpool.ClassPoolLoader;
import org.inksnow.ankhinvoke.classpool.ClassPoolNode;
import org.inksnow.ankhinvoke.comments.InternalName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Function;

public final class ClassPoolService {
  private static final @NotNull Logger logger = LoggerFactory.getLogger(ClassPoolService.class);
  private static final String OBJECT_INTERNAL_NAME = "java/lang/Object";
  private final @NotNull Map<@InternalName @NotNull String, @NotNull ClassPoolNode> classPoolNodeMap = new ConcurrentSkipListMap<>();
  private final @NotNull @Unmodifiable List<@NotNull ClassPoolLoader> poolLoaderList;
  private final @NotNull Function<@InternalName @NotNull String, @Nullable ClassPoolNode> loadFunction = this::load;

  public ClassPoolService(@NotNull @Unmodifiable List<@NotNull ClassPoolLoader> poolLoaderList) {
    this.poolLoaderList = poolLoaderList;
  }

  public @Nullable ClassPoolNode get(@InternalName @NotNull String className) {
    return classPoolNodeMap.computeIfAbsent(className, loadFunction);
  }

  public @NotNull Set<@InternalName @NotNull String> collectParents(@InternalName @NotNull String className) {
    Set<String> parents = new LinkedHashSet<>();
    collectImpl(className, parents);
    return Collections.unmodifiableSet(parents);
  }

  private void collectImpl(@InternalName @NotNull String className, @NotNull Set<@InternalName @NotNull String> parents) {
    if (parents.contains(className)) {
      return;
    }
    parents.add(className);
    ClassPoolNode node = get(className);
    if (node == null) {
      return;
    }
    if (node.superClass() != null) {
      collectImpl(node.superClass(), parents);
    }
    for (String implementClass : node.implementClasses()) {
      collectImpl(implementClass, parents);
    }
  }

  private @Nullable ClassPoolNode load(@InternalName @NotNull String className) {
    for (ClassPoolLoader loader : poolLoaderList) {
      ClassPoolNode node = loader.provide(className);
      if (node != null) {
        return node;
      }
    }
    return null;
  }

  private boolean instanceOfImpl(@InternalName @Nullable String classAName, @InternalName @NotNull String classBName) {
    // null instanceOf anything is false
    if (classAName == null) {
      return false;
    }

    // anything not null instanceOf object is true
    if (OBJECT_INTERNAL_NAME.equals(classBName)) {
      return true;
    }

    ClassPoolNode classBNode = get(classBName);

    // classB is not accessible, use simple one
    if (classBNode == null) {
      return classAName.equals(classBName);
    }

    ClassPoolNode classANode = get(classAName);

    // classA is not accessible, use simple one
    if (classANode == null) {
      return false;
    }

    if (classBNode.isInterface()) {
      // scan interface
      Set<String> scannedClass = new HashSet<>();
      Stack<ClassPoolNode> pendingToScan = new Stack<>();
      pendingToScan.push(classANode);
      scannedClass.add(classAName);
      while (pendingToScan.isEmpty()) {
        ClassPoolNode currentNode = pendingToScan.pop();
        for (String implementClass : currentNode.implementClasses()) {
          if (classBName.equals(implementClass)) {
            return true;
          }
          if (scannedClass.contains(implementClass)) {
            continue;
          }
          ClassPoolNode scanNode = get(implementClass);
          if (scanNode != null) {
            pendingToScan.add(scanNode);
            scannedClass.add(implementClass);
          }
        }
        if (!currentNode.isInterface()) {
          String superClass = currentNode.superClass();
          if (superClass == null) {
            continue;
          }
          ClassPoolNode superNode = get(superClass);
          if (superNode == null) {
            continue;
          }
          pendingToScan.push(superNode);
          scannedClass.add(superClass);
        }
      }
    } else {
      String currentSuperClass = classANode.superClass();
      while (currentSuperClass != null) {
        if (classBName.equals(currentSuperClass)) {
          return true;
        }
        ClassPoolNode currentClass = get(currentSuperClass);
        currentSuperClass = (currentClass == null || currentSuperClass.equals(currentClass.superClass())) ? null : currentClass.superClass();
      }
    }
    return false;
  }

  public boolean instanceOf(@InternalName @Nullable String classAName, @InternalName @NotNull String classBName) {
    boolean result = instanceOfImpl(classAName, classBName);
    logger.debug("instanceOf: {} instanceof {} = {}", classAName, classBName, result);
    return result;
  }

  public boolean isAssignableFrom(@InternalName @NotNull String classAName, @InternalName @Nullable String classBName) {
    boolean result = instanceOfImpl(classBName, classAName);
    logger.debug("instanceOf: {} instanceof {} = {}", classBName, classAName, result);
    return result;
  }

  public static final class Builder {
    private final AnkhInvoke.@NotNull Builder ankhInvokeBuilder;
    private final @NotNull List<@NotNull ClassPoolLoader> poolLoaderList = new ArrayList<>();

    /* package-private */ Builder(AnkhInvoke.@NotNull Builder ankhInvokeBuilder) {
      this.ankhInvokeBuilder = ankhInvokeBuilder;
    }

    public @NotNull Builder appendLoader(@NotNull ClassPoolLoader loader) {
      poolLoaderList.add(loader);
      return this;
    }

    public AnkhInvoke.@NotNull Builder build() {
      return ankhInvokeBuilder;
    }

    /* package-private */
    @NotNull ClassPoolService buildInternal() {
      return new ClassPoolService(Collections.unmodifiableList(new ArrayList<>(this.poolLoaderList)));
    }
  }
}
