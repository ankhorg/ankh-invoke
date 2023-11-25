package org.inksnow.ankhinvoke;

import org.inksnow.ankhinvoke.predicate.PredicateMachine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PredicateService {
  private static final @NotNull Logger logger = LoggerFactory.getLogger(PredicateService.class);
  private final @NotNull @Unmodifiable Map<@NotNull String, @NotNull PredicateMachine> machineRegistry;

  private PredicateService(@NotNull @Unmodifiable Map<@NotNull String, @NotNull PredicateMachine> machineRegistry) {
    this.machineRegistry = machineRegistry;
  }

  public boolean testPredicate(@NotNull String predicate) {
    int keySplit = predicate.indexOf(':');
    String key = (keySplit == -1) ? predicate : predicate.substring(0, keySplit);
    PredicateMachine machine = machineRegistry.get(key);
    if (machine == null) {
      if (AnkhInvoke.DEBUG) {
        logger.warn("predicate machine '{}' not found, used by: {}", key, predicate);
      }
      return false;
    }
    String expression = (keySplit == -1) ? "" : predicate.substring(keySplit + 1);
    boolean result = machine.test(this, expression);
    if (AnkhInvoke.DEBUG) {
      logger.debug("predicate machine '{}' test '{}' result: {}", key, expression, result);
    }
    return result;
  }

  public boolean testPredicate(@NotNull List<@NotNull String> predicates) {
    for (String predicate : predicates) {
      if (!testPredicate(predicate)) {
        return false;
      }
    }
    return true;
  }

  public static class Builder {
    private final AnkhInvoke.@NotNull Builder ankhInvokeBuilder;
    private final @NotNull Map<@NotNull String, @NotNull PredicateMachine> machineRegistry = new HashMap<>();

    public Builder(AnkhInvoke.@NotNull Builder ankhInvokeBuilder) {
      this.ankhInvokeBuilder = ankhInvokeBuilder;

      ServiceLoader.load(PredicateMachine.Factory.class, AnkhInvoke.class.getClassLoader())
          .forEach(factory -> machineRegistry.put(factory.name(), factory.create()));
    }

    public @NotNull Builder register(@NotNull String key, @NotNull PredicateMachine machine) {
      machineRegistry.put(key, machine);
      return this;
    }

    public AnkhInvoke.@NotNull Builder build() {
      return ankhInvokeBuilder;
    }

    /* package-private */
    @NotNull PredicateService buildInternal() {
      return new PredicateService(Collections.unmodifiableMap(new HashMap<>(machineRegistry)));
    }
  }
}
