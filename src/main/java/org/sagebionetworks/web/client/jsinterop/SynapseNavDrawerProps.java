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

  public Callback signoutCallback;

  @JsOverlay
  public static SynapseNavDrawerProps create(Callback signoutCallback) {
    SynapseNavDrawerProps props = new SynapseNavDrawerProps();
    props.signoutCallback = signoutCallback;
    return props;
  }
}
