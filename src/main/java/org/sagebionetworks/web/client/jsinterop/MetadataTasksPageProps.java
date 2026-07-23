package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class MetadataTasksPageProps extends ReactComponentProps {

  String projectId;
  String routerBaseName;

  @JsOverlay
  public static MetadataTasksPageProps create(
    String projectId,
    String routerBaseName
  ) {
    MetadataTasksPageProps props = new MetadataTasksPageProps();
    props.projectId = projectId;
    props.routerBaseName = routerBaseName;
    return props;
  }
}
