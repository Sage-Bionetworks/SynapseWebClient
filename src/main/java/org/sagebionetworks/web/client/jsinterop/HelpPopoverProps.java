package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class HelpPopoverProps extends ReactComponentProps {

  String markdownText;
  String helpUrl;
  String placement;
  boolean showCloseButton;
  String className;

  @JsOverlay
  public static HelpPopoverProps create(
    String markdownText,
    String helpUrl,
    String placement,
    boolean showCloseButton,
    String className
  ) {
    HelpPopoverProps props = new HelpPopoverProps();
    props.markdownText = markdownText;
    props.helpUrl = helpUrl;
    props.placement = placement;
    props.showCloseButton = showCloseButton;
    props.className = className;
    return props;
  }
}
