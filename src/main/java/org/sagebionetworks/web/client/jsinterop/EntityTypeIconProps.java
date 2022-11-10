package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.sagebionetworks.repo.model.EntityType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class EntityTypeIconProps extends ReactComponentProps {

  String type;

  @JsOverlay
  public static EntityTypeIconProps create(EntityType type) {
    EntityTypeIconProps props = new EntityTypeIconProps();
    props.type = type.name();
    return props;
  }
}
