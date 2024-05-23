package org.sagebionetworks.web.client;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.inject.Inject;

public class FeatureFlagConfigFactory {

  PortalGinInjector ginInjector;
  private JSONObject config;

  @Inject
  public FeatureFlagConfigFactory(PortalGinInjector ginInjector) {
    this.ginInjector = ginInjector;
  }

  public void create(String json) {
    JSONValue parsed = JSONParser.parseStrict(json);
    config = parsed.isObject();
  }

  public boolean isFeatureEnabled(String featureName) {
    return (
      DisplayUtils.isInTestWebsite(ginInjector.getCookieProvider()) ||
      config.get(featureName).isBoolean().booleanValue()
    );
  }
}
