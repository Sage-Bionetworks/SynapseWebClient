package org.sagebionetworks.web.client;

import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

public class FeatureFlagConfigFactory {

  public static FeatureFlagConfig create(String json) {
    JSONValue parsed = JSONParser.parseStrict(json);
    return (FeatureFlagConfig) parsed.isObject().getJavaScriptObject().cast();
  }
}
