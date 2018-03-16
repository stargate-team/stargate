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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tsinghua.stargate.conf.NameSpace;
import tsinghua.stargate.exception.StarGateException;

public class AppUtils {

  private static final Logger LOG = LoggerFactory.getLogger(AppUtils.class);

  public static void addJar(Map<String, String> jars, String jarPath)
      throws StarGateException {
    URI uri;
    try {
      uri = PathUtils.resolveURI(jarPath);
    } catch (URISyntaxException e) {
      LOG.error("Failed to resolve uri with {} since uri syntax", jarPath, e);
      throw new StarGateException(e);
    } catch (UnsupportedEncodingException e) {
      LOG.error("Failed to resolve uri with {} since unsupported encoding",
          jarPath, e);
      throw new StarGateException(e);
    }
    jars.put(NameSpace.APP_RESOURCES_JAR, uri.toString());
  }

  public static void addFile(Map<String, String> files, String filePath) {
    files.put(NameSpace.APP_RESOURCES_FILE, filePath);
  }
}
