package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class RedirectDialogProps extends ReactComponentProps {

  String redirectUrl;
  String redirectInstructions;

  @JsOverlay
  public static RedirectDialogProps create(
    String redirectUrl,
    String redirectInstructions
  ) {
    RedirectDialogProps props = new RedirectDialogProps();
    props.redirectUrl = redirectUrl;
    props.redirectInstructions = redirectInstructions;
    return props;
  }
}
