package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class ErrorPageProps extends ReactComponentProps {

  String type;
  String message;
  String id;
  double version;
  String objectType;

  @JsFunction
  public interface Callback {
    void run(String targetHref);
  }

  public Callback gotoPlace;

  @JsOverlay
  public static ErrorPageProps create(
    String type,
    String message,
    String id,
    Long version,
    String objectType,
    Callback gotoPlace
  ) {
    ErrorPageProps props = new ErrorPageProps();
    props.type = type;
    props.message = message;
    props.id = id;
    if (version != null) {
      props.version = version.doubleValue();
    }
    props.gotoPlace = gotoPlace;
    props.objectType = objectType;
    return props;
  }
}
