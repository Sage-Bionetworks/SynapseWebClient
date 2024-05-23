package org.sagebionetworks.web.client;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import org.sagebionetworks.web.client.cookie.CookieProvider;

public class FeatureFlagConfig {

  CookieProvider cookieProvider;
  private JSONObject config;

  public FeatureFlagConfig(String json, CookieProvider cookieProvider) {
    JSONValue parsed = JSONParser.parseStrict(json);
    config = parsed.isObject();
    this.cookieProvider = cookieProvider;
  }

  /**
   * Constructor for testing with dependency injection
   * The required native library for GWT is not available in JUnit
   */
  public FeatureFlagConfig(JSONObject config, CookieProvider cookieProvider) {
    this.config = config;
    this.cookieProvider = cookieProvider;
  }

  public boolean isFeatureEnabled(String featureName) {
    try {
      return (
        DisplayUtils.isInTestWebsite(cookieProvider) ||
        config.get(featureName).isBoolean().booleanValue()
      );
    } catch (Exception e) {
      return DisplayUtils.isInTestWebsite(cookieProvider);
    }
  }
}
