package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class ReferenceJsObject extends ReactComponentProps {

  String targetId;

  @JsNullable
  long targetVersionNumber;

  @JsOverlay
  public static ReferenceJsObject create(
    String targetId,
    Long targetVersionNumber
  ) {
    ReferenceJsObject props = new ReferenceJsObject();
    props.targetId = targetId;
    if (targetVersionNumber != null) {
      props.targetVersionNumber = targetVersionNumber.longValue();
    }
    return props;
  }

  @JsOverlay
  public static ReferenceJsObject create() {
    ReferenceJsObject props = new ReferenceJsObject();
    return props;
  }
}
