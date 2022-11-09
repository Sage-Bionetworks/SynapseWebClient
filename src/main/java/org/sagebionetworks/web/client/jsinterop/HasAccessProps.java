package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class HasAccessProps extends ReactComponentProps {

  @JsFunction
  public interface Callback {
    void run();
  }

  String entityId;
  String entityVersionNumber;
  String className;
  Callback onHide;

  @JsOverlay
  public static HasAccessProps create(
    String entityId,
    String entityVersionNumber,
    String className,
    Callback onHide
  ) {
    HasAccessProps props = new HasAccessProps();
    props.entityId = entityId;
    props.entityVersionNumber = entityVersionNumber;
    props.className = className;
    props.onHide = onHide;
    return props;
  }
}
