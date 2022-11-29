package org.sagebionetworks.web.client.jsinterop.entity.actionmenu;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class EntityActionMenuDropdownConfiguration {

  private boolean visible;
  /* Only strings that map to synapse-react-client Icons */
  private String tooltipText;
  private boolean disabled;

  @JsOverlay
  public static EntityActionMenuDropdownConfiguration create(
    boolean visible,
    String tooltipText,
    boolean disabled
  ) {
    EntityActionMenuDropdownConfiguration config = new EntityActionMenuDropdownConfiguration();
    config.visible = visible;
    config.tooltipText = tooltipText;
    config.disabled = disabled;
    return config;
  }

  @JsOverlay
  public final boolean isVisible() {
    return visible;
  }

  @JsOverlay
  public final void setVisible(boolean visible) {
    this.visible = visible;
  }

  @JsOverlay
  public final String getTooltipText() {
    return tooltipText;
  }

  @JsOverlay
  public final void setTooltipText(String tooltipText) {
    this.tooltipText = tooltipText;
  }

  @JsOverlay
  public final boolean isDisabled() {
    return disabled;
  }

  @JsOverlay
  public final void setDisabled(boolean disabled) {
    this.disabled = disabled;
  }
}
