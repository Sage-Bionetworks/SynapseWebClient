package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.sagebionetworks.web.client.widget.entity.menu.v3.Action;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class ActionViewProps {

  private String action;
  /* Only strings that map to synapse-react-client Icons */
  private String icon;
  private SxProps textSx;
  private SxProps iconSx;

  @JsOverlay
  public static ActionViewProps create(Action action) {
    ActionViewProps actionViewProps = new ActionViewProps();
    actionViewProps.action = action.name();
    return actionViewProps;
  }

  @JsOverlay
  public static ActionViewProps create(Action action, String icon) {
    ActionViewProps actionViewProps = ActionViewProps.create(action);
    actionViewProps.icon = icon;
    return actionViewProps;
  }

  @JsOverlay
  public static ActionViewProps create(
    Action action,
    String icon,
    SxProps textSx,
    SxProps iconSx
  ) {
    ActionViewProps actionViewProps = create(action, icon);
    actionViewProps.textSx = textSx;
    actionViewProps.iconSx = iconSx;
    return actionViewProps;
  }
}
