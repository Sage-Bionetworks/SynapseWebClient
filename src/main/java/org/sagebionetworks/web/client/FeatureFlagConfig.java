package org.sagebionetworks.web.client;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.sagebionetworks.web.client.cookie.CookieProvider;

public class FeatureFlagConfig {

  CookieProvider cookieProvider;
  private JSONObject config;

  public FeatureFlagConfig(String json, CookieProvider cookieProvider) {
    JSONValue parsed = JSONParser.parseStrict(json);
    config = parsed.isObject();
  }

  public void create(String json) {
    JSONValue parsed = JSONParser.parseStrict(json);
    config = parsed.isObject();
  }

  public boolean isFeatureEnabled(String featureName) {
    return (
      DisplayUtils.isInTestWebsite(cookieProvider) ||
      config.get(featureName).isBoolean().booleanValue()
    );
  }
}
