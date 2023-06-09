package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class OrientationBannerProps extends ReactComponentProps {

  String name;

  String title;

  String text;

  @JsNullable
  AlertButtonConfig primaryButtonConfig;

  @JsNullable
  AlertButtonConfig secondaryButtonConfig;

  @JsOverlay
  public static OrientationBannerProps create(
    String name,
    String title,
    String text,
    AlertButtonConfig primaryButtonConfig,
    AlertButtonConfig secondaryButtonConfig
  ) {
    OrientationBannerProps props = new OrientationBannerProps();
    props.name = name;
    props.title = title;
    props.text = text;
    props.primaryButtonConfig = primaryButtonConfig;
    props.secondaryButtonConfig = secondaryButtonConfig;
    return props;
  }
}
