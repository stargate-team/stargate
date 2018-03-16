/*
 * Copyright 2017 The Tsinghua University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tsinghua.stargate.util;

import static org.apache.commons.io.FileUtils.isSymlink;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tsinghua.stargate.conf.Configuration;
import tsinghua.stargate.exception.StarGateException;

public class PathUtils implements Serializable {

  private static final Logger LOG = LoggerFactory.getLogger(PathUtils.class);

  public final static String SEPARATOR = "/";

  public static String root() {
    String basePath = rootStr();
    return Paths.get(basePath).toUri().toString();
  }

  public static String rootStr() {
    String project_root = null;
    try {
      project_root = System.getenv("STARGATE_HOME").replace("\\", SEPARATOR);
    } catch (NullPointerException e) {
      e.printStackTrace();
    }
    return project_root;
  }

  public static String exampleJar() {
    return root() + "lib_managed" + SEPARATOR + "jar" + SEPARATOR
        + "stargate-examples-0.1.0.jar";
  }

  public static List<String> listFiles(Path path) throws IOException {
    Deque<Path> stack = new ArrayDeque<>();
    final List<String> files = new LinkedList<>();

    stack.push(path);

    while (!stack.isEmpty()) {
      DirectoryStream<Path> stream = Files.newDirectoryStream(stack.pop());
      for (Path entry : stream) {
        if (Files.isDirectory(entry)) {
          stack.push(entry);
        } else {
          files.add(entry.toString());
        }
      }
      stream.close();
    }

    return files;
  }

  /**
   * Return a well-formed URI for the file described by a user input string.
   *
   * If the supplied path does not contain a scheme, or is a relative path, it
   * will be converted into an absolute path with a file:// scheme.
   */
  static URI resolveURI(String path)
      throws URISyntaxException, UnsupportedEncodingException {
    URI uri;
    uri = new URI(path);
    if (uri.getScheme() != null) {
      return uri;
    }
    // Make sure to handle if the path has a fragment (applies to yarn
    // distributed cache)
    if (uri.getFragment() != null) {
      URI absoluteURI = new File(uri.getPath()).getAbsoluteFile().toURI();
      return new URI(absoluteURI.getScheme(), absoluteURI.getHost(),
          absoluteURI.getPath(), uri.getFragment());
    }
    return new File(path).getAbsoluteFile().toURI();
  }

  public static String getFilename(String absolutePath) {
    return Paths.get(absolutePath).getFileName().toString();
  }

  private static String[] getConfiguredLocalDirs(Configuration conf) {
    return conf.get("stargate.local.dir", System.getProperty("java.io.tmpdir"))
        .split(",", -1);
  }

  /**
   * Create a directory inside the given parent directory. The directory is
   * guaranteed to be newly created, and is not marked for automatic deletion.
   */
  private static File createDirectory(String root, String namePrefix)
      throws IOException {
    int attempts = 0;
    int maxAttempts = 10;
    File dir = null;
    while (dir == null) {
      attempts += 1;
      if (attempts > maxAttempts) {
        throw new IOException("Failed to create a temp directory (under " + root
            + ") after " + maxAttempts + " attempts!");
      }

      dir = new File(root, namePrefix + "-" + UUID.randomUUID());
      if (dir.exists() || !dir.mkdirs()) {
        dir = null;
      }
    }
    return dir.getCanonicalFile();
  }

  public static List<File> createLocalDirs(Configuration conf)
      throws IOException {
    List<File> files = new ArrayList<File>();
    for (String s : getConfiguredLocalDirs(conf)) {
      files.add(createDirectory(s, "stargateBlock"));
    }
    return files;
  }

  // Handles with hash (add more as required)
  public static Integer nonNegativeHash(Object obj) {
    // Required ?
    if (obj == null)
      return 0;
    int hash = obj.hashCode();
    // math.abs fails for Int.MinValue

    if (hash != Integer.MIN_VALUE) {
      hash = Math.abs(hash);
    } else {
      hash = 0;
    }
    // Nothing else to guard against ?
    return hash;
  }

  public static String getOutputName(String appId) {
    return SEPARATOR + "block-" + appId;
  }

  public static String getPathFromURI(String uri) throws StarGateException {
    try {
      return Paths.get(new URI(uri)).toFile().getPath();
    } catch (URISyntaxException e) {
      LOG.error("Failed to get path from uri", e);
      throw new StarGateException(e);
    }
  }

  /**
   * Delete a file or directory and its contents recursively. Don't follow
   * directories if they are symlinks. Throws an exception if deletion is
   * unsuccessful.
   */
  public static void deleteRecursively(File file) throws IOException {
    if (file != null) {
      try {
        if (file.isDirectory() && !isSymlink(file)) {
          for (File child : file.listFiles()) {
            deleteRecursively(child);
          }
        }
      } finally {
        if (!file.delete()) {
          // Delete can also fail if the file simply did not exist
          if (file.exists()) {
            throw new IOException(
                "Failed to delete: " + file.getAbsolutePath());
          }
        }
      }
    }
  }

  public static void mkDirRecursively(File file) {
    if (file.getParentFile().exists()) {
      file.mkdir();
    } else {
      mkDirRecursively(file.getParentFile());
      file.mkdir();
    }
  }

}
