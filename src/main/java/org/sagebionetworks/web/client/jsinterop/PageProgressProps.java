package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class PageProgressProps extends ReactComponentProps {

  @JsFunction
  public interface Callback {
    void run();
  }

  String barColor;
  int barPercent;
  String backBtnLabel;
  Callback backBtnCallback;
  String forwardBtnLabel;
  Callback forwardBtnCallback;
  boolean forwardBtnActive;

  @JsOverlay
  public static PageProgressProps create(
    String barColor,
    int barPercent,
    String backBtnLabel,
    Callback backBtnCallback,
    String forwardBtnLabel,
    Callback forwardBtnCallback,
    boolean forwardBtnActive
  ) {
    PageProgressProps props = new PageProgressProps();
    props.barColor = barColor;
    props.barPercent = barPercent;
    props.backBtnLabel = backBtnLabel;
    props.backBtnCallback = backBtnCallback;
    props.forwardBtnLabel = forwardBtnLabel;
    props.forwardBtnCallback = forwardBtnCallback;
    props.forwardBtnActive = forwardBtnActive;
    return props;
  }
}
