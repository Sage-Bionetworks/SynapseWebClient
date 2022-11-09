package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class ErrorPageProps extends ReactComponentProps {

  String image;
  String title;
  String message;

  @JsOverlay
  public static ErrorPageProps create(
    String image,
    String title,
    String message
  ) {
    ErrorPageProps props = new ErrorPageProps();
    props.image = image;
    props.title = title;
    props.message = message;
    return props;
  }
}
