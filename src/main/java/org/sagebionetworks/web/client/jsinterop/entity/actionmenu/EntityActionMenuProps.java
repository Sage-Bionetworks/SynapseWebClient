package org.sagebionetworks.web.client.jsinterop.entity.actionmenu;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.JsPropertyMap;
import org.sagebionetworks.web.client.jsinterop.ReactComponentProps;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class EntityActionMenuProps extends ReactComponentProps {

  JsPropertyMap<ActionConfiguration> actionConfiguration;

  EntityActionMenuDropdownMap menuConfiguration;

  EntityActionMenuLayout layout;

  @JsOverlay
  public static EntityActionMenuProps create(
    JsPropertyMap<ActionConfiguration> actionConfiguration,
    EntityActionMenuDropdownMap menuConfiguration,
    EntityActionMenuLayout layout
  ) {
    EntityActionMenuProps props = new EntityActionMenuProps();
    props.actionConfiguration = actionConfiguration;
    props.menuConfiguration = menuConfiguration;
    props.layout = layout;
    return props;
  }
}
