package org.sagebionetworks.web.server.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PortalPropertiesHolder {

  private static Properties propsFromFile;
  private static HashMap<String, String> propsMap;

  static {
    InputStream s =
      SynapseClientImpl.class.getResourceAsStream("/portal.properties");
    propsFromFile = new Properties();
    try {
      propsFromFile.load(s);
    } catch (IOException e) {
      throw new RuntimeException("portal.properties file not found", e);
    }
  }

  public static String getProperty(String key) {
    String propertyFromSystem = System.getProperty(key);
    if (propertyFromSystem != null) {
      return propertyFromSystem;
    }

    return propsFromFile.getProperty(key);
  }

  public static HashMap<String, String> getPropertiesMap() {
    if (propsMap == null) {
      propsMap = new HashMap<String, String>();
      for (Map.Entry<Object, Object> entry : propsFromFile.entrySet()) {
        propsMap.put(entry.getKey().toString(), entry.getValue().toString());
      }
    }

    return propsMap;
  }
}
