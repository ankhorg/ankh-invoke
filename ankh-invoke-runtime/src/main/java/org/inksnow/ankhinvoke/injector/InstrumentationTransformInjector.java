package org.inksnow.ankhinvoke.injector;

import bot.inker.acj.JvmHacker;
import org.inksnow.ankhinvoke.AnkhInvoke;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class InstrumentationTransformInjector extends UnsafeClassInjector implements TransformInjector, ClassFileTransformer {
  private static final @NotNull Logger logger = LoggerFactory.getLogger(InstrumentationTransformInjector.class);
  private static final @NotNull String ANKH_INVOKE_PACKAGE_INTERNAL = AnkhInvoke.ANKH_INVOKE_PACKAGE.replace('.', '/');

  private final @NotNull List<@NotNull String> applyPrefixes;
  private @Nullable AnkhInvoke ankhInvoke;

  public InstrumentationTransformInjector(@NotNull ClassLoader classLoader, @NotNull List<@NotNull String> applyPrefixes) {
    super(classLoader);
    this.applyPrefixes = applyPrefixes.stream()
        .map(it->it.replace('.', '/'))
        .collect(Collectors.toList());
  }

  public InstrumentationTransformInjector(@NotNull ClassLoader classLoader, @NotNull String @NotNull ... applyPrefixes) {
    this(classLoader, Arrays.asList(applyPrefixes));
  }

  @Override
  public void registerHandle(@NotNull AnkhInvoke ankhInvoke) {
    this.ankhInvoke = ankhInvoke;
    JvmHacker.instrumentation().addTransformer(this);
  }

  @Override
  public byte @Nullable [] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
    if (this.classLoader != loader || ankhInvoke == null) {
      return null;
    }

    if(className.startsWith(ANKH_INVOKE_PACKAGE_INTERNAL) && className.charAt(AnkhInvoke.ANKH_INVOKE_PACKAGE.length()) == '/') {
      boolean isAcceptable = false;
      for (String prefix : applyPrefixes) {
        if (className.startsWith(prefix) && className.charAt(prefix.length()) == '/') {
          isAcceptable = true;
          break;
        }
      }
      if(!isAcceptable) {
        return null;
      }
    }

    logger.debug("transform class: {}", className);
    ensureSpyInjected();
    return ankhInvoke.processClass(className);
  }
}
