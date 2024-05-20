package org.sagebionetworks.web.client;

import com.google.gwt.core.client.JavaScriptObject;

public class FeatureFlagConfigImpl
  extends JavaScriptObject
  implements FeatureFlagConfig {

  protected FeatureFlagConfigImpl() {}

  @Override
  public final native boolean isFeatureEnabled(String featureName) /*-{
        return this[featureName] === true;
    }-*/;
}
