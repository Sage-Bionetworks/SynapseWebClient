package org.sagebionetworks.web.client.jsinterop;

import com.google.gwt.user.client.Window;
import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class SynapseContextJsObject {

  @JsNullable
  public String accessToken;

  public String downloadCartPageUrl;
  public boolean isInExperimentalMode;
  public boolean utcTime;
  public String appId;
  public boolean withErrorBoundary;

  @JsOverlay
  public static SynapseContextJsObject create(
    String accessToken,
    boolean isInExperimentalMode,
    boolean utcTime
  ) {
    SynapseContextJsObject context = new SynapseContextJsObject();
    // SWC-6533: Sending all to One Sage for login, and we do not want to stack hop for Prod and Staging
    boolean isStaging = Window.Location
      .getHostName()
      .equalsIgnoreCase("staging.synapse.org");
    context.accessToken = accessToken;
    context.isInExperimentalMode = isInExperimentalMode;
    context.utcTime = utcTime;
    context.downloadCartPageUrl = "/DownloadCart:0";
    context.appId = isStaging ? "staging.synapse.org" : "synapse.org";
    context.withErrorBoundary = true;
    return context;
  }
}
