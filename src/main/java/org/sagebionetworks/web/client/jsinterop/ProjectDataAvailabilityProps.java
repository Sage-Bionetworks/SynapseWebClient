package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class ProjectDataAvailabilityProps extends ReactComponentProps {

  @JsNullable
  public String projectId;

  @JsNullable
  public SxProps sx;

  @JsOverlay
  public static ProjectDataAvailabilityProps create(
    String projectId,
    SxProps sx
  ) {
    ProjectDataAvailabilityProps props = new ProjectDataAvailabilityProps();
    if (projectId != null) {
      props.projectId = projectId;
    }
    if (sx != null) {
      props.sx = sx;
    }
    return props;
  }
}
