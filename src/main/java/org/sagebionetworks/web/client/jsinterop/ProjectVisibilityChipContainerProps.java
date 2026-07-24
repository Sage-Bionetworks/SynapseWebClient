package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class ProjectVisibilityChipContainerProps extends ReactComponentProps {

  String entityId;

  @JsOverlay
  public static ProjectVisibilityChipContainerProps create(String entityId) {
    ProjectVisibilityChipContainerProps props =
      new ProjectVisibilityChipContainerProps();
    props.entityId = entityId;
    return props;
  }
}
