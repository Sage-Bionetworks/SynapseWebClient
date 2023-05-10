package org.sagebionetworks.web.client.jsni;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * JSNI-compatible object for SynapseContextProvider props.
 *
 * If you're porting a new React component, please consider using JsInterop before using this object.
 */
public class SynapseReactClientFullContextJSNIObject extends JavaScriptObject {

  protected SynapseReactClientFullContextJSNIObject() {}

  public static native SynapseReactClientFullContextJSNIObject create() /*-{
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

  public final native void setDownloadCartPageUrl(
    String downloadCartPageUrl
  ) /*-{
  	this.downloadCartPageUrl = downloadCartPageUrl;
	}-*/;
}
