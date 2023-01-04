package org.sagebionetworks.web.client.jsinterop.entity.actionmenu;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.JsPropertyMap;
import org.sagebionetworks.web.client.jsinterop.ReactComponentProps;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class EntityActionMenuPropsJsInterop extends ReactComponentProps {

  public JsPropertyMap<ActionConfiguration> actionConfiguration;

  public EntityActionMenuDropdownMap menuConfiguration;

  public EntityActionMenuLayout layout;

  @JsOverlay
  public static EntityActionMenuPropsJsInterop create(
    JsPropertyMap<ActionConfiguration> actionConfiguration,
    EntityActionMenuDropdownMap menuConfiguration,
    EntityActionMenuLayout layout
  ) {
    EntityActionMenuPropsJsInterop props = new EntityActionMenuPropsJsInterop();
    props.actionConfiguration = actionConfiguration;
    props.menuConfiguration = menuConfiguration;
    props.layout = layout;
    return props;
  }

  @JsOverlay
  public final JsPropertyMap<ActionConfiguration> getActionConfiguration() {
    return this.actionConfiguration;
  }
}
