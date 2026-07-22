package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class ProjectInfoProps extends ReactComponentProps {

  public String projectId;

  @JsOverlay
  public static ProjectInfoProps create(String projectId) {
    ProjectInfoProps props = new ProjectInfoProps();
    props.projectId = projectId;
    return props;
  }
}
