package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class EntityCitationProps extends ReactComponentProps {

  public String projectId;
  public String entityId;
  public Double version;

  @JsOverlay
  public static EntityCitationProps create(
    String projectId,
    String entityId,
    Double version
  ) {
    EntityCitationProps props = new EntityCitationProps();
    props.projectId = projectId;
    props.entityId = entityId;
    props.version = version;
    return props;
  }
}
