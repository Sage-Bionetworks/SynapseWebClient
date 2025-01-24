package org.sagebionetworks.web.client;

public interface OneSageUtils {
  String getAppIdForOneSage();

  String getOneSageURL();

  /**
   * Based on the current hostname, generate a URL pointing to an instance of OneSage with an appropriate appId search param.
   * @param path
   * @return a String representation of the OneSage URL
   */
  String getOneSageURL(String path);

  String getAccountSettingsURL();
}
