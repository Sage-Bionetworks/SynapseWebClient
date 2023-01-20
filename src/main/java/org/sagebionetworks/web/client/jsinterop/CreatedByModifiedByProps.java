package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class CreatedByModifiedByProps extends ReactComponentProps {

  String entityId;

  @JsNullable
  long versionNumber;

  @JsOverlay
  public static CreatedByModifiedByProps create(
    String targetId,
    Long targetVersionNumber
  ) {
    CreatedByModifiedByProps props = new CreatedByModifiedByProps();
    props.entityId = targetId;
    if (targetVersionNumber != null) {
      props.versionNumber = targetVersionNumber.longValue();
    }
    return props;
  }

  @JsOverlay
  public static CreatedByModifiedByProps create() {
    CreatedByModifiedByProps props = new CreatedByModifiedByProps();
    return props;
  }
}
