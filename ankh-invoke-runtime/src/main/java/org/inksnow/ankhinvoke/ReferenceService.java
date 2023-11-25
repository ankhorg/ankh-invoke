package org.inksnow.ankhinvoke;

import org.inksnow.ankhinvoke.comments.InternalName;
import org.inksnow.ankhinvoke.comments.NormalName;
import org.inksnow.ankhinvoke.reference.ReferenceMetadata;
import org.inksnow.ankhinvoke.reference.ReferenceSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Function;

public class ReferenceService {
  private final @NotNull @Unmodifiable List<@InternalName @NotNull String> packageList;
  private final @NotNull @Unmodifiable List<@NotNull ReferenceSource> sourceList;
  private final @NotNull Map<@InternalName @NotNull String, @NotNull ReferenceMetadata> metadataCache = new ConcurrentSkipListMap<>();
  private final @NotNull Function<@InternalName @NotNull String, @NotNull ReferenceMetadata> metadataLoad = this::load;

  public ReferenceService(
      @NotNull @Unmodifiable List<@NotNull String> packageList,
      @NotNull @Unmodifiable List<@NotNull ReferenceSource> sourceList) {
    this.packageList = packageList;
    this.sourceList = sourceList;
  }

  private boolean canBeReferenceClass(@InternalName @NotNull String owner) {
    for (String packageName : packageList) {
      if (owner.startsWith(packageName) && owner.charAt(packageName.length()) == '/') {
        return true;
      }
    }
    return false;
  }

  public @NotNull ReferenceMetadata get(@InternalName @NotNull String owner) {
    return metadataCache.computeIfAbsent(owner, metadataLoad);
  }

  private @NotNull ReferenceMetadata load(@InternalName @NotNull String owner) {
    ReferenceMetadata.Builder builder = ReferenceMetadata.builder();
    loadImpl(owner, builder, true);
    return builder.build();
  }

  private void loadImpl(@InternalName @NotNull String owner, ReferenceMetadata.@NotNull Builder builder, boolean withRoot) {
    boolean withSelf = canBeReferenceClass(owner);

    List<ReferenceMetadata> metadataList = new ArrayList<>(sourceList.size());
    for (ReferenceSource source : sourceList) {
      ReferenceMetadata metadata = source.load(owner);
      if (metadata != null) {
        metadataList.add(metadata);
      }
    }

    Set<String> scannedClass = new HashSet<>();
    for (ReferenceMetadata metadata : metadataList) {
      for (String className : metadata.superClasses()) {
        if (scannedClass.add(className)) {
          loadImpl(className, builder, false);
        }
      }
    }

    for (ReferenceMetadata metadata : metadataList) {
      if (withSelf) {
        builder.append(metadata, withRoot);
      } else {
        builder.appendSuperClass(metadata.superClasses());
      }
    }
  }

  public static final class Builder {
    private final AnkhInvoke.@NotNull Builder ankhInvokeBuilder;
    private final @NotNull List<@InternalName @NotNull String> packageList = new ArrayList<>();
    private final @NotNull List<@NotNull ReferenceSource> sourceList = new ArrayList<>();

    /* package-private */ Builder(AnkhInvoke.@NotNull Builder ankhInvokeBuilder) {
      this.ankhInvokeBuilder = ankhInvokeBuilder;
    }

    public @NotNull Builder appendPackage(@NormalName @NotNull String packageName) {
      packageList.add(packageName.replace('.', '/'));
      return this;
    }

    public @NotNull Builder clearPackage() {
      packageList.clear();
      return this;
    }

    public @NotNull Builder appendSource(@NotNull ReferenceSource source) {
      this.sourceList.add(source);
      return this;
    }

    public @NotNull Builder clearSource() {
      this.sourceList.clear();
      return this;
    }

    public AnkhInvoke.@NotNull Builder build() {
      return ankhInvokeBuilder;
    }

    /* package-private */
    @NotNull ReferenceService buildInternal() {
      return new ReferenceService(
          Collections.unmodifiableList(new ArrayList<>(this.packageList)),
          Collections.unmodifiableList(new ArrayList<>(this.sourceList)));
    }
  }
}
