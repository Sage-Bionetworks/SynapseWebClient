package org.sagebionetworks.web.client.jsinterop.entity.actionmenu;

import java.util.ArrayList;
import java.util.List;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.sagebionetworks.web.client.jsinterop.ReactMouseEventHandler;
import org.sagebionetworks.web.client.widget.entity.menu.v3.Action;
import org.sagebionetworks.web.client.widget.entity.menu.v3.ActionListener;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class ActionConfiguration {

  private String action;
  private String text;
  private String href;
  private boolean visible;
  private boolean disabled;
  private String tooltipText;
  private ReactMouseEventHandler onClick;

  private Action _action;
  private List<ActionListener> _actionListeners;

  @JsOverlay
  public static ActionConfiguration create(Action action, String text) {
    ActionConfiguration config = new ActionConfiguration();
    config._action = action;
    config.action = action.name();
    config.text = text;
    config.visible = false;
    config.disabled = false;
    config.tooltipText = "";
    config._actionListeners = new ArrayList<>();
    config.onClick =
      (
        event ->
          config._actionListeners.forEach(listener ->
            listener.onAction(action, event)
          )
      );
    return config;
  }

  @JsOverlay
  public final void setAction(Action action) {
    this._action = action;
    this.action = action.name();
  }

  @JsOverlay
  public final Action getAction() {
    return this._action;
  }

  @JsOverlay
  public final void addActionListener(final ActionListener listener) {
    _actionListeners.add(listener);
  }

  @JsOverlay
  public final void clearActionListeners() {
    _actionListeners.clear();
  }

  @JsOverlay
  public final List<ActionListener> getActionListeners() {
    return _actionListeners;
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
  public final String getText() {
    return text;
  }

  @JsOverlay
  public final void setText(String text) {
    this.text = text;
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

  @JsOverlay
  public final String getHref() {
    return href;
  }

  @JsOverlay
  public final void setHref(String href) {
    this.href = href;
  }
}
