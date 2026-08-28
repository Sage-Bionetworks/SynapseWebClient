package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class DownloadCartPageProps extends ReactComponentProps {

  @JsOverlay
  public static DownloadCartPageProps create() {
    return new DownloadCartPageProps();
  }
}
