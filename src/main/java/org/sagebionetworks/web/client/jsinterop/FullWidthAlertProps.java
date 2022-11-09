package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class FullWidthAlertProps extends ReactComponentProps {

  @JsFunction
  public interface Callback {
    void run();
  }

  @JsNullable
  String title;

  @JsNullable
  String description;

  @JsNullable
  AlertButtonConfig primaryButtonConfig;

  @JsNullable
  AlertButtonConfig secondaryButtonConfig;

  @JsNullable
  String variant;

  @JsNullable
  Callback onClose;

  @JsNullable
  Double autoCloseAfterDelayInSeconds;

  @JsNullable
  Boolean isGlobal;

  @JsOverlay
  public static FullWidthAlertProps create(
    String title,
    String description,
    AlertButtonConfig primaryButtonConfig,
    AlertButtonConfig secondaryButtonConfig,
    Callback onClose,
    Double autoCloseAfterDelayInSeconds,
    Boolean isGlobal,
    String variant
  ) {
    FullWidthAlertProps props = new FullWidthAlertProps();
    props.title = title;
    props.description = description;
    props.primaryButtonConfig = primaryButtonConfig;
    props.secondaryButtonConfig = secondaryButtonConfig;
    props.onClose = onClose;
    props.autoCloseAfterDelayInSeconds = autoCloseAfterDelayInSeconds;
    props.isGlobal = isGlobal;
    props.variant = variant;
    return props;
  }
}
