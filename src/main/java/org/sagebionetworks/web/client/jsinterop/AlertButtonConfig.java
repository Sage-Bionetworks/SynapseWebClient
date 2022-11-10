package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class AlertButtonConfig {

  @JsFunction
  public interface Callback {
    void run();
  }

  String text;

  @JsNullable
  Boolean isDisabled;

  @JsNullable
  String tooltipText;

  @JsNullable
  String href;

  @JsNullable
  Callback onClick;

  @JsOverlay
  public static AlertButtonConfig create(String text, String href) {
    AlertButtonConfig props = new AlertButtonConfig();
    props.text = text;
    props.href = href;
    return props;
  }

  @JsOverlay
  public static AlertButtonConfig create(String text, Callback onClick) {
    AlertButtonConfig props = new AlertButtonConfig();
    props.text = text;
    props.onClick = onClick;
    return props;
  }

  @JsOverlay
  public static AlertButtonConfig create(
    String text,
    String href,
    Boolean isDisabled,
    String tooltipText
  ) {
    AlertButtonConfig props = new AlertButtonConfig();
    props.text = text;
    props.href = href;
    props.isDisabled = isDisabled;
    props.tooltipText = tooltipText;
    return props;
  }

  @JsOverlay
  public static AlertButtonConfig create(
    String text,
    Callback onClick,
    Boolean isDisabled,
    String tooltipText
  ) {
    AlertButtonConfig props = new AlertButtonConfig();
    props.text = text;
    props.onClick = onClick;
    props.isDisabled = isDisabled;
    props.tooltipText = tooltipText;
    return props;
  }
}
