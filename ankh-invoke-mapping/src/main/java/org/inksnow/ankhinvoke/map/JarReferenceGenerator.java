package org.inksnow.ankhinvoke.map;

import org.inksnow.ankhinvoke.map.util.SafeIoUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

public class JarReferenceGenerator {
  private static final @NotNull Logger logger = LoggerFactory.getLogger(AnkhInvokeMapping.ANKH_INVOKE_PACKAGE);
  private final @NotNull BlobReferenceGenerator blobReferenceGenerator;

  public JarReferenceGenerator(@NotNull BlobReferenceGenerator blobReferenceGenerator) {
    this.blobReferenceGenerator = blobReferenceGenerator;
  }

  public void execute(List<File> inputFiles, File outputFile) throws IOException {
    // accept scan
    for (File inputFile : inputFiles) {
      try(JarInputStream jarIn = new JarInputStream(new FileInputStream(inputFile))) {
        JarEntry entry;
        while ((entry = jarIn.getNextJarEntry()) != null) {
          blobReferenceGenerator.acceptScan(entry.getName(), jarIn);
        }
      }
    }

    try(JarOutputStream jarOut = new JarOutputStream(new FileOutputStream(outputFile))) {
      for (File inputFile : inputFiles) {
        try (JarInputStream jarIn = new JarInputStream(new FileInputStream(inputFile))) {
          handle(jarIn, jarOut);
        }
      }

      for (BlobReferenceGenerator.ProcessAction processAction : blobReferenceGenerator.collect()) {
        if(processAction.isKeep() && processAction.shouldRename() && processAction.shouldReplace()) {
          jarOut.putNextEntry(new JarEntry(processAction.getNewName()));
          jarOut.write(processAction.getBytes());
        } else if (processAction.isRemove() && processAction.shouldRename()){
          logger.warn("jar generator not support remove in collect scope", new UnsupportedOperationException());
        }
      }
    }
  }

  private void handle(@NotNull JarInputStream jarIn, @NotNull JarOutputStream jarOut) throws IOException {
    JarEntry entry;
    while ((entry = jarIn.getNextJarEntry()) != null) {
      byte[] bytes = SafeIoUtils.readAllBytes(jarIn);
      BlobReferenceGenerator.ProcessAction processAction;
      try(InputStream in = new ByteArrayInputStream(bytes)) {
        processAction = blobReferenceGenerator.acceptProcess(entry.getName(), in);
      }
      if (processAction.isRemove()) {
        logger.info("remove {}", entry.getName());
      } else if (processAction.isKeep()) {
        jarOut.putNextEntry(copyEntry(entry, processAction.shouldRename() ? processAction.getNewName() : null));
        jarOut.write(processAction.shouldReplace() ? processAction.getBytes() : bytes);
      }
    }
  }

  @Contract("null -> null; !null -> !null")
  private JarEntry copyEntry(JarEntry entry) {
    return copyEntry(entry, null);
  }

  @Contract("null, _ -> null; !null, _ -> !null")
  private JarEntry copyEntry(JarEntry entry, @Nullable String name) {
    if (entry == null) {
      return null;
    }
    JarEntry newEntry = new JarEntry(name == null ? entry.getName() : name);
    newEntry.setTime(entry.getTime());
    newEntry.setComment(entry.getComment());
    newEntry.setExtra(entry.getExtra());
    if (entry.getCreationTime() != null) {
      newEntry.setCreationTime(entry.getCreationTime());
    }
    if (entry.getLastAccessTime() != null) {
      newEntry.setLastAccessTime(entry.getLastAccessTime());
    }
    if (entry.getLastModifiedTime() != null) {
      newEntry.setLastModifiedTime(entry.getLastModifiedTime());
    }
    newEntry.setMethod(entry.getMethod());
    return newEntry;
  }
}
