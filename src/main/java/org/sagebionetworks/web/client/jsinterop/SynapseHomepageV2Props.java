package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class SynapseHomepageV2Props extends ReactComponentProps {

  @JsFunction
  public interface Callback {
    void run(String targetHref);
  }

  public Callback gotoPlace;

  @JsOverlay
  public static SynapseHomepageV2Props create(Callback gotoPlace) {
    SynapseHomepageV2Props props = new SynapseHomepageV2Props();
    props.gotoPlace = gotoPlace;
    return props;
  }
}
