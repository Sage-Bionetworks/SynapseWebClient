package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class SynapseNavDrawerProps extends ReactComponentProps {

  @JsFunction
  public interface Callback {
    void run();
  }

  @JsFunction
  public interface Callback2 {
    void run(String targetHref);
  }

  public Callback signoutCallback;
  public Callback2 gotoPlace;

  @JsOverlay
  public static SynapseNavDrawerProps create(
    Callback signoutCallback,
    Callback2 gotoPlace
  ) {
    SynapseNavDrawerProps props = new SynapseNavDrawerProps();
    props.signoutCallback = signoutCallback;
    props.gotoPlace = gotoPlace;
    return props;
  }
}
