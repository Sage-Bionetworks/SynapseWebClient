package org.sagebionetworks.web.client.jsinterop.entity.actionmenu;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.JsPropertyMap;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class ActionConfigurationMap
  implements JsPropertyMap<ActionConfiguration> {

  public ActionConfigurationMap() {
    super();
  }

  @JsOverlay
  public static ActionConfigurationMap create() {
    return new ActionConfigurationMap();
  }
}
