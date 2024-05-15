package org.sagebionetworks.web.client;

/**
 * Utilities for working with strings.
 *
 * @author jhill
 *
 */
public class StringUtils {

  /**
   * Compare two cell values and decide if there is a change. This method will treat empty string and
   * null as equal.
   *
   * @param original
   * @param changed
   * @return
   */
  public static boolean isValueChanged(String inOriginal, String inChanged) {
    String tOriginal = emptyAsNull(inOriginal);
    String tChanged = emptyAsNull(inChanged);
    if (tOriginal == null) {
      if (tChanged == null) {
        return false;
      }
      return true;
    } else {
      return !tOriginal.equals(tChanged);
    }
  }

  /**
   * @return Returns null if the passed value is null. Returns null if the string is empty (or only
   *         whitespace), else the string.
   */
  public static String emptyAsNull(String toUpdate) {
    if (toUpdate == null || toUpdate.isEmpty() || toUpdate.trim().isEmpty()) {
      return null;
    } else {
      return toUpdate;
    }
  }

  public static String toTitleCase(String s) {
    if (s == null) {
      return null;
    }
    StringBuilder output = new StringBuilder();
    boolean isNextUpperCase = true;
    for (char c : s.toLowerCase().toCharArray()) {
      if (!Character.isLetterOrDigit(c)) {
        isNextUpperCase = true;
      } else if (isNextUpperCase) {
        c = Character.toUpperCase(c);
        isNextUpperCase = false;
      }
      output.append(c);
    }

    return output.toString();
  }

  public static String getGWTPlaceTokenFromURL(String url) {
    String placeToken = null;
    if (url.contains(":")) {
      // optimization - use place changer in this case as well
      int colonIndex = url.lastIndexOf(':');
      // Search backwards from colonIndex to find the last '/'
      int lastSlashIndex = url.lastIndexOf('/', colonIndex);
      placeToken = url.substring(lastSlashIndex + 1);
    }
    return placeToken;
  }
}
