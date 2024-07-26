package org.sagebionetworks.web.client;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import org.sagebionetworks.web.client.cookie.CookieProvider;

public class FeatureFlagConfig {

  private final CookieProvider cookieProvider;
  private final JSONObject config;

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

  public boolean isFeatureEnabled(FeatureFlagKey feature) {
    try {
      return (
        DisplayUtils.isInTestWebsite(cookieProvider) ||
        config.get(feature.getKey()).isBoolean().booleanValue()
      );
    } catch (Exception e) {
      return DisplayUtils.isInTestWebsite(cookieProvider);
    }
  }
}
