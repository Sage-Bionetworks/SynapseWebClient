package org.sagebionetworks.web.client.jsni;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * JSNI-compatible object for SynapseContextProvider props.
 *
 * If you're porting a new React component, please consider using JsInterop before using this object.
 */
public class SynapseContextJSNIObject extends JavaScriptObject {

  protected SynapseContextJSNIObject() {}

  public static native SynapseContextJSNIObject create() /*-{
        return {};
    }-*/;

  public final native void setAccessToken(String accessToken) /*-{
        this.accessToken = accessToken;
    }-*/;

  public final native void setIsInExperimentalMode(
    boolean isInExperimentalMode
  ) /*-{
        this.isInExperimentalMode = isInExperimentalMode;
    }-*/;

  public final native void setUtcTime(boolean utcTime) /*-{
        this.utcTime = utcTime;
    }-*/;
}
