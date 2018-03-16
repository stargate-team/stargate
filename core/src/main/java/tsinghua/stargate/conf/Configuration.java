/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tsinghua.stargate.conf;

import java.io.*;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import com.google.common.base.Preconditions;

import tsinghua.stargate.Log;
import tsinghua.stargate.util.StringInterner;
import tsinghua.stargate.util.Utils;

/** Provides access to configuration parameters. */
public class Configuration extends Log implements Serializable {

  private boolean quietmode = true;

  private static final String DEFAULT_STRING_CHECK =
      "testingforemptydefaultvalue";

  private boolean allowNullValueProperties = false;

  private Map<String, Object> objectSettings = new ConcurrentHashMap<>();

  /** Set a configuration variable. */
  public Configuration setObject(String key, Object value) {
    if (key == null) {
      throw new NullPointerException("null key");
    }
    if (value == null) {
      throw new NullPointerException("null value for " + key);
    }
    objectSettings.put(key, value);
    return this;
  }

  public Object getObject(String key) {
    Object value = objectSettings.get(key);
    if (value == null) {
      return null;
    } else {
      return value;
    }
  }

  private Properties getProps() {
    if (null == properties) {
      properties = new Properties();
      loadResources(properties, resources, quietmode);
    }
    return properties;
  }

  private static class Resource {
    private final Object resource;
    private final String name;

    Resource(Object resource) {
      this(resource, resource.toString());
    }

    Resource(Object resource, String name) {
      this.resource = resource;
      this.name = name;
    }

    public String getName() {
      return name;
    }

    public Object getResource() {
      return resource;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  /**
   * List of configuration resources.
   */
  private ArrayList<Resource> resources = new ArrayList<Resource>();

  /**
   * The value reported as the setting resource when a key is set by code rather
   * than a file resource by dumpConfiguration.
   */
  static final String UNKNOWN_RESOURCE = "Unknown";

  /**
   * List of configuration parameters marked <b>final</b>.
   */
  private Set<String> finalParameters = new HashSet<String>();

  private boolean loadDefaults = true;

  /**
   * Configuration objects
   */
  private static final WeakHashMap<Configuration, Object> REGISTRY =
      new WeakHashMap<>();

  /**
   * List of default Resources. Resources are loaded in the order of the list
   * entries
   */
  private static final CopyOnWriteArrayList<String> defaultResources =
      new CopyOnWriteArrayList<>();

  private transient static final Map<ClassLoader, Map<String, WeakReference<Class<?>>>> CACHE_CLASSES =
      new WeakHashMap<>();

  private Properties properties;
  private transient ClassLoader classLoader;

  {
    classLoader = Thread.currentThread().getContextClassLoader();
    if (classLoader == null) {
      classLoader = Configuration.class.getClassLoader();
    }
  }

  /**
   * Add a default resource. Resources are loaded in the order of the resources
   * added.
   *
   * @param name file name. File should be present in the classpath.
   */
  public static synchronized void addDefaultResource(String name) {
    if (!defaultResources.contains(name)) {
      defaultResources.add(name);
      for (Configuration conf : REGISTRY.keySet()) {
        if (conf.loadDefaults) {
          conf.reloadConfiguration();
        }
      }
    }
  }

  /**
   * Add a configuration resource.
   *
   * The properties of this resource will override properties of previously
   * added resources, unless they were marked <a href="#Final">final</a>.
   *
   * @param name resource to be added, the classpath is examined for a file with
   *          that name.
   */
  public void addResource(String name) {
    addResourceObject(new Resource(name));
  }

  /**
   * Reload configuration from previously added resources.
   *
   * This method will clear all the configuration read from the added resources,
   * and final parameters. This will make the resources to be read again before
   * accessing the values. Values that are added via set methods will overlay
   * values read from the resources.
   */
  public synchronized void reloadConfiguration() {
    properties = null; // trigger reload
    finalParameters.clear(); // clear site-limits
  }

  private synchronized void addResourceObject(Resource resource) {
    resources.add(resource); // add to resources
    reloadConfiguration();
  }

  private static final Pattern VAR_PATTERN =
      Pattern.compile("\\$\\{[^\\}\\$\u0020]+\\}");

  private static final int MAX_SUBST = 20;

  private String substituteVars(String expr) {
    if (expr == null) {
      return null;
    }
    Matcher match = VAR_PATTERN.matcher("");
    String eval = expr;
    for (int s = 0; s < MAX_SUBST; s++) {
      match.reset(eval);
      if (!match.find()) {
        return eval;
      }
      String var = match.group();
      var = var.substring(2, var.length() - 1); // remove ${ .. }
      String val = null;
      try {
        val = System.getProperty(var);
      } catch (SecurityException se) {
        warn("Unexpected SecurityException in Configuration", se);
      }
      if (val == null) {
        return eval; // return literal ${var}: var is unbound
      }
      // substitute
      eval =
          eval.substring(0, match.start()) + val + eval.substring(match.end());
    }
    throw new IllegalStateException(
        "Variable substitution depth too large: " + MAX_SUBST + " " + expr);
  }

  public String get(String name) {
    return substituteVars(getProps().getProperty(name));
  }

  /**
   * Get the value of the <code>name</code> property as a trimmed
   * <code>String</code>, <code>null</code> if no such property exists. If the
   * key is deprecated, it returns the value of the first key which replaces the
   * deprecated key and is not null
   *
   * Values are processed for <a href="#VariableExpansion">variable
   * expansion</a> before being returned.
   *
   * @param name the property name.
   * @return the value of the <code>name</code> or its replacing property, or
   *         null if no such property exists.
   */
  private String getTrimmed(String name) {
    String value = get(name);

    if (null == value) {
      return null;
    } else {
      return value.trim();
    }
  }

  /**
   * Set the <code>value</code> of the <code>name</code> property. If
   * <code>name</code> is deprecated or there is a deprecated name associated to
   * it, it sets the value to both names. Name will be trimmed before put into
   * configuration.
   *
   * @param name property name.
   * @param value property value.
   */
  public void set(String name, String value) {
    set(name, value, null);
  }

  /**
   * Set the <code>value</code> of the <code>name</code> property. If
   * <code>name</code> is deprecated, it also sets the <code>value</code> to the
   * keys that replace the deprecated key. Name will be trimmed before put into
   * configuration.
   *
   * @param name property name.
   * @param value property value.
   * @param source the place that this configuration value came from (For
   *          debugging).
   * @throws IllegalArgumentException when the value or name is null.
   */
  public void set(String name, String value, String source) {
    Preconditions.checkArgument(name != null, "Property name must not be null");
    Preconditions.checkArgument(value != null,
        "The value of property " + name + " must not be null");
    name = name.trim();
    getProps().setProperty(name, value);
  }

  /**
   * Get the value of the <code>name</code>. If the key is deprecated, it
   * returns the value of the first key which replaces the deprecated key and is
   * not null. If no such property exists, then <code>defaultValue</code> is
   * returned.
   *
   * @param name property name, will be trimmed before get value.
   * @param defaultValue default value.
   * @return property value, or <code>defaultValue</code> if the property
   *         doesn't exist.
   */
  public String get(String name, String defaultValue) {
    return substituteVars(getProps().getProperty(name, defaultValue));
  }

  /**
   * Get the value of the <code>name</code> property as an <code>int</code>.
   *
   * If no such property exists, the provided default value is returned, or if
   * the specified value is not a valid <code>int</code>, then an error is
   * thrown.
   *
   * @param name property name.
   * @param defaultValue default value.
   * @return property value as an <code>int</code>, or
   *         <code>defaultValue</code>.
   * @throws NumberFormatException when the value is invalid
   */
  public int getInt(String name, int defaultValue) {
    String valueString = getTrimmed(name);
    if (valueString == null) {
      return defaultValue;
    }
    String hexString = getHexDigits(valueString);
    if (hexString != null) {
      return Integer.parseInt(hexString, 16);
    }
    return Integer.parseInt(valueString);
  }

  /**
   * Set the value of the <code>name</code> property to an <code>int</code>.
   *
   * @param name property name.
   * @param value <code>int</code> value of the property.
   */
  public void setInt(String name, int value) {
    set(name, Integer.toString(value));
  }

  /**
   * Get the value of the <code>name</code> property as a <code>long</code>. If
   * no such property exists, the provided default value is returned, or if the
   * specified value is not a valid <code>long</code>, then an error is thrown.
   *
   * @param name property name.
   * @param defaultValue default value.
   * @return property value as a <code>long</code>, or
   *         <code>defaultValue</code>.
   * @throws NumberFormatException when the value is invalid
   */
  public long getLong(String name, long defaultValue) {
    String valueString = getTrimmed(name);
    if (valueString == null) {
      return defaultValue;
    }
    String hexString = getHexDigits(valueString);
    if (hexString != null) {
      return Long.parseLong(hexString, 16);
    }
    return Long.parseLong(valueString);
  }

  private String getHexDigits(String value) {
    boolean negative = false;
    String str = value;
    String hexString = null;
    if (value.startsWith("-")) {
      negative = true;
      str = value.substring(1);
    }
    if (str.startsWith("0x") || str.startsWith("0X")) {
      hexString = str.substring(2);
      if (negative) {
        hexString = "-" + hexString;
      }
      return hexString;
    }
    return null;
  }

  /**
   * Get the value of the <code>name</code> property as a <code>float</code>. If
   * no such property exists, the provided default value is returned, or if the
   * specified value is not a valid <code>float</code>, then an error is thrown.
   *
   * @param name property name.
   * @param defaultValue default value.
   * @return property value as a <code>float</code>, or
   *         <code>defaultValue</code>.
   * @throws NumberFormatException when the value is invalid
   */
  public float getFloat(String name, float defaultValue) {
    String valueString = getTrimmed(name);
    if (valueString == null) {
      return defaultValue;
    }
    return Float.parseFloat(valueString);
  }

  /**
   * Set the value of the <code>name</code> property to a <code>float</code>.
   *
   * @param name property name.
   * @param value property value.
   */
  public void setFloat(String name, float value) {
    set(name, Float.toString(value));
  }

  /**
   * Get the value of the <code>name</code> property as a <code>double</code>.
   * If no such property exists, the provided default value is returned, or if
   * the specified value is not a valid <code>double</code>, then an error is
   * thrown.
   *
   * @param name property name.
   * @param defaultValue default value.
   * @return property value as a <code>double</code>, or
   *         <code>defaultValue</code>.
   * @throws NumberFormatException when the value is invalid
   */
  public double getDouble(String name, double defaultValue) {
    String valueString = getTrimmed(name);
    if (valueString == null) {
      return defaultValue;
    }
    return Double.parseDouble(valueString);
  }

  /**
   * Set the value of the <code>name</code> property to a <code>double</code>.
   *
   * @param name property name.
   * @param value property value.
   */
  public void setDouble(String name, double value) {
    set(name, Double.toString(value));
  }

  /**
   * Get the value of the <code>name</code> property as a <code>boolean</code>.
   * If no such property is specified, or if the specified value is not a valid
   * <code>boolean</code>, then <code>defaultValue</code> is returned.
   *
   * @param name property name.
   * @param defaultValue default value.
   * @return property value as a <code>boolean</code>, or
   *         <code>defaultValue</code>.
   */
  public boolean getBoolean(String name, boolean defaultValue) {
    String valueString = getTrimmed(name);
    if (null == valueString || valueString.isEmpty()) {
      return defaultValue;
    }

    valueString = valueString.toLowerCase();

    if ("true".equals(valueString)) {
      return true;
    } else if ("false".equals(valueString)) {
      return false;
    } else {
      return defaultValue;
    }
  }

  /**
   * Set the value of the <code>name</code> property to a <code>boolean</code>.
   *
   * @param name property name.
   * @param value <code>boolean</code> value of the property.
   */
  public void setBoolean(String name, boolean value) {
    set(name, Boolean.toString(value));
  }

  /**
   * Set the value of the <code>name</code> property to the given type. This is
   * equivalent to <code>set(&lt;name&gt;, value.toString())</code>.
   *
   * @param name property name
   * @param value new value
   */
  public <T extends Enum<T>> void setEnum(String name, T value) {
    set(name, value.toString());
  }

  /**
   * Return value matching this enumerated type.
   *
   * @param name Property name
   * @param defaultValue Value returned if no mapping exists
   * @throws IllegalArgumentException If mapping is illegal for the type
   *           provided
   */
  public <T extends Enum<T>> T getEnum(String name, T defaultValue) {
    final String val = get(name);
    return null == val ? defaultValue
        : Enum.valueOf(defaultValue.getDeclaringClass(), val);
  }

  enum ParsedTimeDuration {
    NS {
      TimeUnit unit() {
        return TimeUnit.NANOSECONDS;
      }

      String suffix() {
        return "ns";
      }
    },
    US {
      TimeUnit unit() {
        return TimeUnit.MICROSECONDS;
      }

      String suffix() {
        return "us";
      }
    },
    MS {
      TimeUnit unit() {
        return TimeUnit.MILLISECONDS;
      }

      String suffix() {
        return "ms";
      }
    },
    S {
      TimeUnit unit() {
        return TimeUnit.SECONDS;
      }

      String suffix() {
        return "s";
      }
    },
    M {
      TimeUnit unit() {
        return TimeUnit.MINUTES;
      }

      String suffix() {
        return "m";
      }
    },
    H {
      TimeUnit unit() {
        return TimeUnit.HOURS;
      }

      String suffix() {
        return "h";
      }
    },
    D {
      TimeUnit unit() {
        return TimeUnit.DAYS;
      }

      String suffix() {
        return "d";
      }
    };

    abstract TimeUnit unit();

    abstract String suffix();

    static ParsedTimeDuration unitFor(String s) {
      for (ParsedTimeDuration ptd : values()) {
        // iteration order is in decl order, so SECONDS matched last
        if (s.endsWith(ptd.suffix())) {
          return ptd;
        }
      }
      return null;
    }

    static ParsedTimeDuration unitFor(TimeUnit unit) {
      for (ParsedTimeDuration ptd : values()) {
        if (ptd.unit() == unit) {
          return ptd;
        }
      }
      return null;
    }
  }

  /**
   * Set the value of <code>name</code> to the given time duration. This is
   * equivalent to <code>set(&lt;name&gt;, value + &lt;time suffix&gt;)</code>.
   *
   * @param name Property name
   * @param value Time duration
   * @param unit Unit of time
   */
  public void setTimeDuration(String name, long value, TimeUnit unit) {
    set(name, value + ParsedTimeDuration.unitFor(unit).suffix());
  }

  /**
   * Return time duration in the given time unit. Valid units are encoded in
   * properties as suffixes: nanoseconds (ns), microseconds (us), milliseconds
   * (ms), seconds (s), minutes (m), hours (h), and days (d).
   *
   * @param name Property name
   * @param defaultValue Value returned if no mapping exists.
   * @param unit Unit to convert the stored property, if it exists.
   * @throws NumberFormatException If the property stripped of its unit is not a
   *           number
   */
  public long getTimeDuration(String name, long defaultValue, TimeUnit unit) {
    String vStr = get(name);
    if (null == vStr) {
      return defaultValue;
    }
    vStr = vStr.trim();
    ParsedTimeDuration vUnit = ParsedTimeDuration.unitFor(vStr);
    if (null == vUnit) {
      warn("No unit for " + name + "(" + vStr + ") assuming " + unit);
      vUnit = ParsedTimeDuration.unitFor(unit);
    } else {
      vStr = vStr.substring(0, vStr.lastIndexOf(vUnit.suffix()));
    }
    return unit.convert(Long.parseLong(vStr), vUnit.unit());
  }

  /**
   * Get the socket address for <code>name</code> property as a
   * <code>InetSocketAddress</code>.
   *
   * @param name property name.
   * @param defaultAddress the default value
   * @param defaultPort the default port
   * @return InetSocketAddress
   */
  public InetSocketAddress getSocketAddr(String name, String defaultAddress,
      int defaultPort) {
    final String address = get(name, defaultAddress);
    return Utils.createSocketAddr(address, defaultPort, name);
  }

  /**
   * Return the number of keys in the configuration.
   *
   * @return number of keys in the configuration.
   */
  public int size() {
    return getProps().size();
  }

  /**
   * Clears all keys from the configuration.
   */
  public void clear() {
    getProps().clear();
  }

  private Document parse(DocumentBuilder builder, URL url)
      throws IOException, SAXException {
    if (!quietmode) {
      debug("parsing URL " + url);
    }
    if (url == null) {
      return null;
    }
    return parse(builder, url.openStream(), url.toString());
  }

  private Document parse(DocumentBuilder builder, InputStream is,
      String systemId) throws IOException, SAXException {
    if (!quietmode) {
      debug("parsing input stream " + is);
    }
    if (is == null) {
      return null;
    }
    try {
      return (systemId == null) ? builder.parse(is)
          : builder.parse(is, systemId);
    } finally {
      is.close();
    }
  }

  /**
   * Get the {@link URL} for the named resource.
   *
   * @param name resource name.
   * @return the url for the named resource.
   */
  private URL getResource(String name) {
    return classLoader.getResource(name);
  }

  private void loadResources(Properties properties,
      ArrayList<Resource> resources, boolean quiet) {
    if (loadDefaults) {
      for (String resource : defaultResources) {
        loadResource(properties, new Resource(resource), quiet);
      }
    }

    for (int i = 0; i < resources.size(); i++) {
      Resource ret = loadResource(properties, resources.get(i), quiet);
      if (ret != null) {
        resources.set(i, ret);
      }
    }
  }

  private Resource loadResource(Properties properties, Resource wrapper,
      boolean quiet) {
    String name = UNKNOWN_RESOURCE;
    try {
      Object resource = wrapper.getResource();
      name = wrapper.getName();

      DocumentBuilderFactory docBuilderFactory =
          DocumentBuilderFactory.newInstance();
      // ignore all comments inside the xml file
      docBuilderFactory.setIgnoringComments(true);

      // allow includes in the xml file
      docBuilderFactory.setNamespaceAware(true);
      try {
        docBuilderFactory.setXIncludeAware(true);
      } catch (UnsupportedOperationException e) {
        error("Failed to set setXIncludeAware(true) for parser "
            + docBuilderFactory + ":" + e, e);
      }
      DocumentBuilder builder = docBuilderFactory.newDocumentBuilder();
      Document doc = null;
      Element root = null;
      boolean returnCachedProperties = false;

      if (resource instanceof URL) { // an URL resource
        doc = parse(builder, (URL) resource);
      } else if (resource instanceof String) { // a CLASSPATH resource
        URL url = getResource((String) resource);
        doc = parse(builder, url);
      } else if (resource instanceof Path) { // a file resource
        // Can't use FileSystem API or we get an infinite loop
        // since FileSystem uses Configuration API. Use java.io.File instead.
        File file =
            new File(((Path) resource).toUri().getPath()).getAbsoluteFile();
        if (file.exists()) {
          if (!quiet) {
            debug("parsing File " + file);
          }
          doc =
              parse(builder, new BufferedInputStream(new FileInputStream(file)),
                  resource.toString());
        }
      } else if (resource instanceof InputStream) {
        doc = parse(builder, (InputStream) resource, null);
        returnCachedProperties = true;
      } else if (resource instanceof Properties) {
        overlay(properties, (Properties) resource);
      } else if (resource instanceof Element) {
        root = (Element) resource;
      }

      if (root == null) {
        if (doc == null) {
          if (quiet) {
            return null;
          }
          throw new RuntimeException(resource + " not found");
        }
        root = doc.getDocumentElement();
      }
      Properties toAddTo = properties;
      if (returnCachedProperties) {
        toAddTo = new Properties();
      }
      if (!"configuration".equals(root.getTagName())) {
        error("bad conf file: top-level element not <configuration>");
      }
      NodeList props = root.getChildNodes();

      for (int i = 0; i < props.getLength(); i++) {
        Node propNode = props.item(i);
        if (!(propNode instanceof Element)) {
          continue;
        }
        Element prop = (Element) propNode;
        if ("configuration".equals(prop.getTagName())) {
          loadResource(toAddTo, new Resource(prop, name), quiet);
          continue;
        }
        if (!"property".equals(prop.getTagName())) {
          warn("bad conf file: element not <property>");
        }
        NodeList fields = prop.getChildNodes();
        String attr = null;
        String value = null;
        boolean finalParameter = false;
        LinkedList<String> source = new LinkedList<String>();
        for (int j = 0; j < fields.getLength(); j++) {
          Node fieldNode = fields.item(j);
          if (!(fieldNode instanceof Element)) {
            continue;
          }
          Element field = (Element) fieldNode;
          if ("name".equals(field.getTagName()) && field.hasChildNodes()) {
            attr = StringInterner
                .weakIntern(((Text) field.getFirstChild()).getData().trim());
          }
          if ("value".equals(field.getTagName()) && field.hasChildNodes()) {
            value = StringInterner
                .weakIntern(((Text) field.getFirstChild()).getData());
          }
          if ("final".equals(field.getTagName()) && field.hasChildNodes()) {
            finalParameter =
                "true".equals(((Text) field.getFirstChild()).getData());
          }
          if ("source".equals(field.getTagName()) && field.hasChildNodes()) {
            source.add(StringInterner
                .weakIntern(((Text) field.getFirstChild()).getData()));
          }
        }
        source.add(name);

        // Ignore this parameter if it has already been marked as 'final'
        if (attr != null) {
          loadProperty(toAddTo, name, attr, value, finalParameter,
              source.toArray(new String[source.size()]));
        }
      }

      if (returnCachedProperties) {
        overlay(properties, toAddTo);
        return new Resource(toAddTo, name);
      }
      return null;
    } catch (IOException e) {
      error("error parsing conf " + name, e);
      throw new RuntimeException(e);
    } catch (DOMException e) {
      error("error parsing conf " + name, e);
      throw new RuntimeException(e);
    } catch (SAXException e) {
      error("error parsing conf " + name, e);
      throw new RuntimeException(e);
    } catch (ParserConfigurationException e) {
      error("error parsing conf " + name, e);
      throw new RuntimeException(e);
    }
  }

  private void overlay(Properties to, Properties from) {
    for (Entry<Object, Object> entry : from.entrySet()) {
      to.put(entry.getKey(), entry.getValue());
    }
  }

  private void loadProperty(Properties properties, String name, String attr,
      String value, boolean finalParameter, String[] source) {
    if (value != null || allowNullValueProperties) {
      if (!finalParameters.contains(attr)) {
        if (value == null && allowNullValueProperties) {
          value = DEFAULT_STRING_CHECK;
        }
        properties.setProperty(attr, value);
      } else if (!value.equals(properties.getProperty(attr))) {
        warn(name + ":an attempt to override final parameter: " + attr
            + ";  Ignoring.");
      }
    }
    if (finalParameter) {
      finalParameters.add(attr);
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Configuration: ");
    if (loadDefaults) {
      toString(defaultResources, sb);
      if (resources.size() > 0) {
        sb.append(", ");
      }
    }
    toString(resources, sb);
    return sb.toString();
  }

  private <T> void toString(List<T> resources, StringBuilder sb) {
    ListIterator<T> i = resources.listIterator();
    while (i.hasNext()) {
      if (i.nextIndex() != 0) {
        sb.append(", ");
      }
      sb.append(i.next());
    }
  }

  /**
   * Load a class by name.
   *
   * @param name the class name.
   * @return the class object.
   * @throws ClassNotFoundException if the class is not found.
   */
  public Class<?> getClassByName(String name) throws ClassNotFoundException {
    Class<?> ret = getClassByNameOrNull(name);
    if (ret == null) {
      throw new ClassNotFoundException("Class " + name + " not found");
    }
    return ret;
  }

  /**
   * Sentinel value to store negative cache results in .
   */
  private static final Class<?> NEGATIVE_CACHE_SENTINEL =
      NegativeCacheSentinel.class;

  /**
   * Load a class by name, returning null rather than throwing an exception if
   * it couldn't be loaded. This is to avoid the overhead of creating an
   * exception.
   *
   * @param name the class name
   * @return the class object, or null if it could not be found.
   */
  private Class<?> getClassByNameOrNull(String name) {
    Map<String, WeakReference<Class<?>>> map;

    synchronized (CACHE_CLASSES) {
      map = CACHE_CLASSES.get(classLoader);
      if (map == null) {
        map = Collections.synchronizedMap(
            new WeakHashMap<String, WeakReference<Class<?>>>());
        CACHE_CLASSES.put(classLoader, map);
      }
    }

    Class<?> clazz = null;
    WeakReference<Class<?>> ref = map.get(name);
    if (ref != null) {
      clazz = ref.get();
    }

    if (clazz == null) {
      try {
        clazz = Class.forName(name, true, classLoader);
      } catch (ClassNotFoundException e) {
        // Leave a marker that the class isn't found
        map.put(name, new WeakReference<Class<?>>(NEGATIVE_CACHE_SENTINEL));
        return null;
      }
      // two putters can race here, but they'll put the same class
      map.put(name, new WeakReference<Class<?>>(clazz));
      return clazz;
    } else if (clazz == NEGATIVE_CACHE_SENTINEL) {
      return null; // not found
    } else {
      // cache hit
      return clazz;
    }
  }

  /**
   * A unique class which is used as a sentinel value in the caching for
   * getClassByName. {@see Configuration#getClassByNameOrNull(String)}
   */
  private static abstract class NegativeCacheSentinel {
  }

  /**
   * Get the value of the <code>name</code> property as a <code>Class</code>. If
   * no such property is specified, then <code>defaultValue</code> is returned.
   *
   * @param name the class name.
   * @param defaultValue default value.
   * @return property value as a <code>Class</code>, or
   *         <code>defaultValue</code>.
   */
  public Class<?> getClass(String name, Class<?> defaultValue) {
    String valueString = getTrimmed(name);
    if (valueString == null) {
      return defaultValue;
    }
    try {
      return getClassByName(valueString);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Get the value of the <code>name</code> property as a <code>Class</code>
   * implementing the interface specified by <code>xface</code>.
   *
   * If no such property is specified, then <code>defaultValue</code> is
   * returned.
   *
   * An exception is thrown if the returned class does not implement the named
   * interface.
   *
   * @param name the class name.
   * @param defaultValue default value.
   * @param xface the interface implemented by the named class.
   * @return property value as a <code>Class</code>, or
   *         <code>defaultValue</code>.
   */
  public <U> Class<? extends U> getClass(String name,
      Class<? extends U> defaultValue, Class<U> xface) {
    try {
      Class<?> theClass = getClass(name, defaultValue);
      if (theClass != null && !xface.isAssignableFrom(theClass)) {
        throw new RuntimeException(theClass + " not " + xface.getName());
      } else if (theClass != null) {
        return theClass.asSubclass(xface);
      } else {
        return null;
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Set the value of the <code>name</code> property to the name of a
   * <code>theClass</code> implementing the given interface <code>xface</code>.
   *
   * An exception is thrown if <code>theClass</code> does not implement the
   * interface <code>xface</code>.
   *
   * @param name property name.
   * @param theClass property value.
   * @param xface the interface implemented by the named class.
   */
  public void setClass(String name, Class<?> theClass, Class<?> xface) {
    if (!xface.isAssignableFrom(theClass)) {
      throw new RuntimeException(theClass + " not " + xface.getName());
    }
    set(name, theClass.getName());
  }
}
