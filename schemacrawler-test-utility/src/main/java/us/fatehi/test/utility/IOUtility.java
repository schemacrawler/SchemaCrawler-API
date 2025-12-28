/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.test.utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public final class IOUtility {

  private static final Logger LOGGER = Logger.getLogger(IOUtility.class.getName());

  /**
   * Locates the resource based on the current thread's classloader. Always assumes that resources
   * are absolute. When running on the module path, this method tries multiple strategies to locate
   * the resource.
   *
   * @param classpathResource The classpath resource to locate
   * @return URL for the located resource, or null if not found
   */
  public static URL locateResource(final String classpathResource) {
    if (classpathResource == null || classpathResource.isBlank()) {
      return null;
    }
    final String resolvedClasspathResource;
    if (classpathResource.startsWith("/")) {
      resolvedClasspathResource = classpathResource.substring(1);
    } else {
      resolvedClasspathResource = classpathResource;
    }

    // Try context classloader first (works for classpath-based execution)
    final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    if (contextClassLoader != null) {
      final URL url = contextClassLoader.getResource(resolvedClasspathResource);
      if (url != null) {
        return url;
      }
    }

    // Try the classloader that loaded this class (works for module path)
    final ClassLoader classClassLoader = IOUtility.class.getClassLoader();
    if (classClassLoader != null) {
      final URL url = classClassLoader.getResource(resolvedClasspathResource);
      if (url != null) {
        return url;
      }
    }

    // Try using Class.getResource with absolute path (module-aware)
    return IOUtility.class.getResource("/" + resolvedClasspathResource);
  }

  public static InputStream newResourceInputStream(final String classpathResource) {
    final URL url = locateResource(classpathResource);
    if (url == null) {
      throwForBadResource(classpathResource);
    }

    InputStream inputStream = null;
    try {
      inputStream = url.openStream();
    } catch (final IOException e) {
      throwForBadResource(classpathResource);
    }
    if (inputStream == null) {
      throwForBadResource(classpathResource);
    }
    return inputStream;
  }

  public static BufferedReader newResourceReader(final String classpathResource) {
    final InputStream inputStream = newResourceInputStream(classpathResource);
    final BufferedReader reader =
        new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    return reader;
  }

  private static void throwForBadResource(final String classpathResource) {
    final IllegalArgumentException e =
        new IllegalArgumentException(
            "Cannot read classpath resource, <%s>".formatted(classpathResource));
    throw e;
  }

  private IOUtility() {
    // Prevent instantiation
  }
}
