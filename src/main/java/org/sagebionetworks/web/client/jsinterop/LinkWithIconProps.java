package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class LinkWithIconProps extends ReactComponentProps {

  public String text;
  public String icon;
  public String href;

  @JsOverlay
  public static LinkWithIconProps create(
    String text,
    String icon,
    String href
  ) {
    LinkWithIconProps props = new LinkWithIconProps();
    props.text = text;
    props.icon = icon;
    props.href = href;
    return props;
  }
}
