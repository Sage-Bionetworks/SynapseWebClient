package org.sagebionetworks.web.client;

import java.util.List;

public class ArrayUtils {

  public static String[] getStringArray(List<String> l) {
    if (l == null) {
      return null;
    }
    String[] d = new String[l.size()];
    for (int i = 0; i < l.size(); i++) {
      d[i] = l.get(i);
    }
    return d;
  }
}
