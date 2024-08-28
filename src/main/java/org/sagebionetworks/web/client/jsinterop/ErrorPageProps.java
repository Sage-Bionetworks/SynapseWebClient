package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class ErrorPageProps extends ReactComponentProps {

  String type;
  String message;
  String entityId;
  Double entityVersionNumber;

  @JsFunction
  public interface Callback {
    void run(String targetHref);
  }

  public Callback gotoPlace;

  @JsOverlay
  public static ErrorPageProps create(
    String type,
    String message,
    String entityId,
    Long entityVersion,
    Callback gotoPlace
  ) {
    ErrorPageProps props = new ErrorPageProps();
    props.type = type;
    props.message = message;
    props.entityId = entityId;
    props.entityVersionNumber =
      entityVersion != null ? entityVersion.doubleValue() : null;
    props.gotoPlace = gotoPlace;
    return props;
  }
}
