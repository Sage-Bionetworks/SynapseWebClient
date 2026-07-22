package org.sagebionetworks.web.client;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.sagebionetworks.web.client.cookie.CookieProvider;

@Singleton
public class FeatureFlagConfig {

  private final CookieProvider cookieProvider;
  private JSONObject config;

  @Inject
  public FeatureFlagConfig(CookieProvider cookieProvider) {
    this.cookieProvider = cookieProvider;
    config = new JSONObject();
  }

  public void setJson(String json) {
    if (json != null) {
      JSONValue parsed = JSONParser.parseStrict(json);
      config = parsed.isObject();
    }
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
      JSONValue value = config.get(feature.getKey());
      if (value != null && value.isBoolean() != null) {
        // Explicit true/false: flag overrides experimental mode
        // true  → always enabled
        // false → always disabled, even in experimental mode
        return value.isBoolean().booleanValue();
      }
    } catch (Exception e) {
      // fall through to experimental mode check
    }
    // null/undefined: follow experimental mode
    return DisplayUtils.isInTestWebsite(cookieProvider);
  }
}
