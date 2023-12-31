package org.inksnow.ankhinvoke;

import org.inksnow.ankhinvoke.asm.*;
import org.inksnow.ankhinvoke.classpool.ClassPoolNode;
import org.inksnow.ankhinvoke.comments.InternalName;
import org.inksnow.ankhinvoke.comments.NormalName;
import org.inksnow.ankhinvoke.injector.TransformInjector;
import org.inksnow.ankhinvoke.util.DstUnsafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class AnkhInvoke {
  @SuppressWarnings("StringOperationCanBeSimplified") // for compatibility
  public static final @NotNull String ANKH_INVOKE_PACKAGE = new String("org.inksnow.ankhinvoke");
  public static final @NotNull String RAW_ANKH_INVOKE_PACKAGE = new String(new char[]{'o', 'r', 'g', '.', 'i', 'n', 'k', 's', 'n', 'o', 'w', '.', 'a', 'n', 'k', 'h', 'i', 'n', 'v', 'o', 'k', 'e'});
  public static final boolean DEBUG = Boolean.parseBoolean(System.getProperty(ANKH_INVOKE_PACKAGE + ".debug"))
      || Boolean.parseBoolean(System.getProperty(RAW_ANKH_INVOKE_PACKAGE + ".debug"));
  public static final @NotNull Path DUMP_PATH = Paths.get(System.getProperty(ANKH_INVOKE_PACKAGE + ".dumppath", "ankh-invoke"));
  private static final @NotNull Logger logger = LoggerFactory.getLogger(AnkhInvoke.class);

  private static final @NotNull Object PENDING_PROCESS_OBJECT = new Object();
  private static final @NotNull Object NOT_PROCESS_OBJECT = new Object();

  static {
    if (!AnkhInvoke.class.getName().equals(ANKH_INVOKE_PACKAGE + ".AnkhInvoke")) {
      throw new IllegalStateException("AnkhInvoke class remapped but const not remapped");
    }
  }

  private final @NotNull Object processedClassLock = new Object();
  private final @NotNull Map<@InternalName @NotNull String, @NotNull Object> processedClass = new HashMap<>();
  private final @NotNull ReferenceService referenceService;
  private final @NotNull InjectService injectService;
  private final @NotNull ClassPoolService classPoolService;
  private final @NotNull PredicateService predicateService;
  private final @NotNull RemapService globalRemapService;
  private final @NotNull RemapService referenceRemapService;

  private final @NotNull Consumer<@NotNull String> injectInnerClassMethod = it -> injectInnerClass(it, true);

  private AnkhInvoke(@NotNull ReferenceService referenceService, @NotNull InjectService injectService, @NotNull ClassPoolService classPoolService, @NotNull PredicateService predicateService, @NotNull RemapService globalRemapService, @NotNull RemapService referenceRemapService) {
    this.referenceService = referenceService;
    this.injectService = injectService;
    this.classPoolService = classPoolService;
    this.predicateService = predicateService;
    this.globalRemapService = globalRemapService;
    this.referenceRemapService = referenceRemapService;

    if (injectService.getInjector() instanceof TransformInjector) {
      ((TransformInjector) injectService.getInjector()).registerHandle(this);
    }
  }

  public static @NotNull Builder builder() {
    return new Builder();
  }

  public void get(@NormalName @NotNull String className) {
    injectInnerClass(className.replace('.', '/'), true);
  }

  private void injectInnerClass(@InternalName @NotNull String className, boolean force) {
    synchronized (processedClassLock) {
      Object instance = processedClass.get(className);
      if (instance != null) {
        if (instance == PENDING_PROCESS_OBJECT || instance == NOT_PROCESS_OBJECT) {
          return;
        } else if (instance instanceof Throwable) {
          throw DstUnsafe.throwImpl((Throwable) instance);
        } else if (instance instanceof Class<?>) {
          return;
        } else {
          throw new IllegalStateException("unknown cache vaule: " + instance);
        }
      } else {
        processedClass.put(className, PENDING_PROCESS_OBJECT);
      }
    }

    if (!referenceService.get(className).isEmpty()) {
      processedClass.put(className, NOT_PROCESS_OBJECT);
      return;
    }

    ClassPoolNode classPoolNode = classPoolService.get(className);
    if (classPoolNode != null) {
      if (classPoolNode.superClass() != null) {
        injectInnerClass(classPoolNode.superClass(), false);
      }
      for (String implementClass : classPoolNode.implementClasses()) {
        injectInnerClass(implementClass, false);
      }
    }

    try {
      if (force || fastScanClassImpl(className)) {
        Class<?> result = injectClassImpl(className);
        synchronized (processedClassLock) {
          processedClass.put(className, result == null ? NOT_PROCESS_OBJECT : result);
        }
      } else {
        processedClass.put(className, NOT_PROCESS_OBJECT);
      }
    } catch (Throwable e) {
      synchronized (processedClassLock) {
        processedClass.put(className, e);
      }
      if (DEBUG) {
        logger.error("Failed to process class {}", className, e);
      }
      throw DstUnsafe.throwImpl(e);
    }
  }

  private boolean fastScanClassImpl(@InternalName @NotNull String className) {
    byte[] inputBytes = injectService.provide(className);
    if (inputBytes == null) {
      return false;
    } else {
      return fastScanClassImpl(inputBytes);
    }
  }

  private boolean fastScanClassImpl(byte @NotNull [] inputBytes) {
    try {
      ScanReferenceVisitor scanReferenceVisitor = new ScanReferenceVisitor(referenceService);
      ClassReader cr = new ClassReader(inputBytes);
      cr.accept(scanReferenceVisitor.createClassVisitor(), 0);

      return scanReferenceVisitor.usedClassWithReference();
    } catch (Throwable e) {
      if (AnkhInvoke.DEBUG) {
        logger.error("Failed to fast scan class", e);
      }
      return false;
    }
  }

  private @Nullable Class<?> injectClassImpl(@InternalName @NotNull String className) {
    byte[] inputBytes = injectService.provide(className);
    if (inputBytes == null) {
      return null;
    }

    byte[] classBytes = processClassImpl(inputBytes, true);
    if (DEBUG) {
      try {
        Path dumpClassPath = DUMP_PATH.resolve(className + ".class");
        Files.createDirectories(dumpClassPath.getParent());
        Files.write(dumpClassPath, classBytes);
      } catch (Exception e) {
        logger.error("Failed to write debug dump for {}", className, e);
      }
    }
    return injectService.inject(className, classBytes, null);
  }

  private byte @NotNull [] processClassImpl(byte @NotNull [] inputBytes, boolean scannedInner) {
    if (!fastScanClassImpl(inputBytes)) {
      return inputBytes;
    }

    ClassNode classNode = new ClassNode();
    ClassReader classReader = new ClassReader(inputBytes);
    classReader.accept(classNode, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

    classNode = new ApplyOverrideProcessor(referenceService, predicateService, referenceRemapService, classPoolService).process(classNode);
    classNode = new ApplyReferenceProcessor(referenceService, predicateService, referenceRemapService).process(classNode);
    classNode = new ClassRemapperProcess(globalRemapService).process(classNode);
    classNode = new AddProcessedAnnotationProcessor().process(classNode);
    classNode = new ScannerInnerProcessor(injectInnerClassMethod).process(classNode);

    ClassWriter classWriter = new PooledClassWriter(classPoolService, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
    classNode.accept(classWriter);
    return classWriter.toByteArray();
  }

  public byte @Nullable [] processClass(@InternalName @NotNull String className) {
    byte[] inputBytes = injectService.provide(className);
    if (inputBytes == null) {
      return null;
    }

    byte[] classBytes = processClassImpl(inputBytes, false);
    if (DEBUG) {
      try {
        Path dumpClassPath = DUMP_PATH.resolve(className + ".class");
        Files.createDirectories(dumpClassPath.getParent());
        Files.write(dumpClassPath, classBytes);
      } catch (Exception e) {
        logger.error("Failed to write debug dump for {}", className, e);
      }
    }
    return classBytes;
  }

  public @NotNull ReferenceService referenceService() {
    return referenceService;
  }

  public @NotNull InjectService injectService() {
    return injectService;
  }

  public @NotNull PredicateService predicateService() {
    return predicateService;
  }

  public static final class Builder {
    private final ReferenceService.@NotNull Builder referenceServiceBuilder = new ReferenceService.Builder(this);
    private final InjectService.@NotNull Builder injectServiceBuilder = new InjectService.Builder(this);
    private final ClassPoolService.@NotNull Builder classPoolServiceBuilder = new ClassPoolService.Builder(this);
    private final PredicateService.@NotNull Builder predicateServiceBuilder = new PredicateService.Builder(this);
    private final RemapService.@NotNull Builder globalRemapBuilder = new RemapService.Builder(this);
    private final RemapService.@NotNull Builder referenceRemapBuilder = new RemapService.Builder(this);

    public ReferenceService.@NotNull Builder reference() {
      return referenceServiceBuilder;
    }

    public InjectService.@NotNull Builder inject() {
      return injectServiceBuilder;
    }

    public ClassPoolService.@NotNull Builder classPool() {
      return classPoolServiceBuilder;
    }

    public PredicateService.@NotNull Builder predicate() {
      return predicateServiceBuilder;
    }

    public RemapService.@NotNull Builder globalRemap() {
      return globalRemapBuilder;
    }

    public RemapService.@NotNull Builder referenceRemap() {
      return referenceRemapBuilder;
    }

    public @NotNull AnkhInvoke build() {
      PredicateService predicateService = predicateServiceBuilder.buildInternal();
      ClassPoolService classPoolService = classPoolServiceBuilder.buildInternal();
      ReferenceService referenceService = referenceServiceBuilder.buildInternal();
      InjectService injectService = injectServiceBuilder.buildInternal();
      RemapService globalRemapService = globalRemapBuilder.buildInternal(predicateService);
      RemapService referenceRemapService = referenceRemapBuilder.buildInternal(predicateService);
      return new AnkhInvoke(referenceService, injectService, classPoolService, predicateService, globalRemapService, referenceRemapService);
    }
  }




}