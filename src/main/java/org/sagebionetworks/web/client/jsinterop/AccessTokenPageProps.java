package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class AccessTokenPageProps extends ReactComponentProps {

  public String title;

  public String body;

  @JsOverlay
  public static AccessTokenPageProps create(String title, String body) {
    AccessTokenPageProps props = new AccessTokenPageProps();
    props.title = title;
    props.body = body;
    return props;
  }
}
