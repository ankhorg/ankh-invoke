package org.inksnow.ankhinvoke;

import org.inksnow.ankhinvoke.asm.BlobRemapper;
import org.inksnow.ankhinvoke.codec.MappingRegistry;
import org.inksnow.ankhinvoke.comments.InternalName;
import org.inksnow.ankhinvoke.util.DstUnsafe;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.objectweb.asm.commons.Remapper;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPInputStream;

public final class RemapService extends Remapper {
  private final @NotNull List<@NotNull Remapper> remapperList;

  private RemapService(@NotNull List<@NotNull Remapper> remapperList) {
    this.remapperList = remapperList;
  }

  @Override
  public @NotNull String mapDesc(@NotNull String rawDescriptor) {
    String descriptor = rawDescriptor;
    for (Remapper remapper : remapperList) {
      descriptor = remapper.mapDesc(descriptor);
    }
    return descriptor;
  }

  @Override
  @Contract("null -> null; !null -> !null")
  public @InternalName String mapType(@InternalName String rawInternalName) {
    String internalName = rawInternalName;
    for (Remapper remapper : remapperList) {
      internalName = remapper.mapType(internalName);
    }
    return internalName;
  }

  @Override
  public @InternalName @NotNull String @NotNull [] mapTypes(@InternalName @NotNull String @NotNull [] rawInternalNames) {
    String[] internalNames = rawInternalNames;
    for (Remapper remapper : remapperList) {
      internalNames = remapper.mapTypes(internalNames);
    }
    return internalNames;
  }

  @Override
  public @NotNull String mapMethodDesc(@NotNull String rawMethodDescriptor) {
    String methodDescriptor = rawMethodDescriptor;
    for (Remapper remapper : remapperList) {
      methodDescriptor = remapper.mapMethodDesc(methodDescriptor);
    }
    return methodDescriptor;
  }

  @Override
  public @UnknownNullability Object mapValue(@UnknownNullability Object rawValue) {
    Object value = rawValue;
    for (Remapper remapper : remapperList) {
      value = remapper.mapValue(value);
    }
    return value;
  }

  @Override
  @Contract("null -> null; !null -> !null")
  public String mapSignature(String rawSignature, boolean typeSignature) {
    String signature = rawSignature;
    for (Remapper remapper : remapperList) {
      signature = remapper.mapSignature(signature, typeSignature);
    }
    return signature;
  }

  @Override
  public @NotNull String mapAnnotationAttributeName(@NotNull String rawDescriptor, @NotNull String rawName) {
    String descriptor = rawDescriptor;
    String name = rawName;
    for (Remapper remapper : remapperList) {
      name = remapper.mapAnnotationAttributeName(descriptor, name);
      descriptor = remapper.mapDesc(rawDescriptor);
    }
    return name;
  }

  @Override
  public @InternalName @NotNull String mapInnerClassName(@InternalName @NotNull String rawName, @InternalName @NotNull String rawOwnerName, @InternalName @NotNull String rawInnerName) {
    return super.mapInnerClassName(rawName, rawOwnerName, rawInnerName);
    // String name = rawName;
    // String ownerName = rawOwnerName;
    // String innerName = rawInnerName;
    // for (Remapper remapper : remapperList) {
    //   innerName = remapper.mapInnerClassName(name, ownerName, innerName);
    //   ownerName = remapper.mapType(ownerName);
    //   name = innerName;
    // }
    // return innerName;
  }

  @Override
  public @NotNull String mapMethodName(@InternalName @NotNull String rawOwner, @NotNull String rawName, @NotNull String rawDescriptor) {
    String owner = rawOwner;
    String name = rawName;
    String descriptor = rawDescriptor;
    for (Remapper remapper : remapperList) {
      name = remapper.mapMethodName(owner, name, descriptor);
      owner = remapper.map(owner);
      descriptor = remapper.mapMethodDesc(descriptor);
    }
    return name;
  }

  @Override
  public @NotNull String mapInvokeDynamicMethodName(@InternalName @NotNull String rawName, @NotNull String rawDescriptor) {
    String name = rawName;
    String descriptor = rawDescriptor;
    for (Remapper remapper : remapperList) {
      name = remapper.mapInvokeDynamicMethodName(name, descriptor);
      descriptor = remapper.mapMethodDesc(descriptor);
    }
    return name;
  }

  @Override
  public @NotNull String mapRecordComponentName(@InternalName @NotNull String rawOwner, @NotNull String rawName, @NotNull String rawDescriptor) {
    String owner = rawOwner;
    String name = rawName;
    String descriptor = rawDescriptor;
    for (Remapper remapper : remapperList) {
      name = remapper.mapRecordComponentName(owner, name, descriptor);
      owner = remapper.map(owner);
      descriptor = remapper.mapDesc(descriptor);
    }
    return name;
  }

  @Override
  public @NotNull String mapFieldName(@InternalName @NotNull String rawOwner, @NotNull String rawName, @NotNull String rawDescriptor) {
    String owner = rawOwner;
    String name = rawName;
    String descriptor = rawDescriptor;
    for (Remapper remapper : remapperList) {
      name = remapper.mapFieldName(owner, name, descriptor);
      owner = remapper.map(owner);
      descriptor = remapper.mapDesc(descriptor);
    }
    return name;
  }

  @Override
  public @InternalName @NotNull String mapPackageName(@InternalName @NotNull String rawName) {
    String name = rawName;
    for (Remapper remapper : remapperList) {
      name = remapper.mapPackageName(name);
    }
    return name;
  }

  @Override
  public @InternalName @NotNull String mapModuleName(@InternalName @NotNull String rawName) {
    String name = rawName;
    for (Remapper remapper : remapperList) {
      name = remapper.mapModuleName(name);
    }
    return name;
  }

  @Override
  public @InternalName @NotNull String map(@InternalName @NotNull String internalName) {
    String name = internalName;
    for (Remapper remapper : remapperList) {
      name = remapper.map(name);
    }
    return name;
  }

  public static class Builder {
    private final AnkhInvoke.@NotNull Builder ankhInvokeBuilder;
    private final @NotNull List<@NotNull Remapper> remapperList = new ArrayList<>();
    private @Nullable String applyMapRegistry = null;

    /* package-private */ Builder(AnkhInvoke.@NotNull Builder ankhInvokeBuilder) {
      this.ankhInvokeBuilder = ankhInvokeBuilder;
    }

    public @NotNull Builder setApplyMapRegistry(@Nullable String applyMapRegistry) {
      this.applyMapRegistry = applyMapRegistry;
      return this;
    }

    public @NotNull Builder append(@NotNull Remapper remapper) {
      remapperList.add(remapper);
      return this;
    }

    public AnkhInvoke.@NotNull Builder build() {
      return ankhInvokeBuilder;
    }

    /* package-private */
    @NotNull RemapService buildInternal(@NotNull PredicateService predicateService) {
      List<Remapper> remapperList = new ArrayList<>(this.remapperList);
      if (applyMapRegistry != null) {
        try {
          String registryDirectory = AnkhInvoke.ANKH_INVOKE_PACKAGE.replace('.', '/') + "/" + applyMapRegistry + "/";
          String registryFile = registryDirectory + "map-registry";
          URL registryUrl = AnkhInvoke.class.getClassLoader().getResource(registryFile);
          if (registryUrl == null) {
            throw new FileNotFoundException("apply registry file " + registryFile + " not found in resource");
          }

          MappingRegistry registry;
          try (DataInputStream in = new DataInputStream(new GZIPInputStream(new BufferedInputStream(registryUrl.openStream())))) {
            registry = MappingRegistry.load(in);
          }

          for (MappingRegistry.MappingSet set : registry.sets()) {
            boolean applied = false;
            for (MappingRegistry.MappingEntry entry : set.entries()) {
              if (predicateService.testPredicate(entry.predicates())) {
                String mappingFile = registryDirectory + "mappings/" + entry.name();
                URL mappingFileUrl = AnkhInvoke.class.getClassLoader().getResource(mappingFile);
                if (mappingFileUrl == null) {
                  throw new FileNotFoundException("apply mapping file " + mappingFile + " not found in resource");
                }
                BlobRemapper blobRemapper;
                try (DataInputStream in = new DataInputStream(new GZIPInputStream(new BufferedInputStream(mappingFileUrl.openStream())))) {
                  blobRemapper = BlobRemapper.load(in);
                }
                remapperList.add(blobRemapper);
                applied = true;
                break;
              }
            }
            if (!applied && set.isRequired()) {
              throw new IllegalStateException("required mapping registry " + set.name() + " no set applied");
            }
          }

        } catch (IOException e) {
          throw DstUnsafe.throwImpl(e);
        }
      }
      return new RemapService(Collections.unmodifiableList(remapperList));
    }
  }
}
