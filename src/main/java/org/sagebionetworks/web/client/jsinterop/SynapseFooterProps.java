package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class SynapseFooterProps extends ReactComponentProps {

  String portalVersion;
  String srcVersion;
  String repoVersion;

  @JsFunction
  public interface Callback {
    void run();
  }

  @JsFunction
  public interface Callback2 {
    void run(String targetHref);
  }

  public Callback2 gotoPlace;

  @JsOverlay
  public static SynapseFooterProps create(
    String portalVersion,
    String srcVersion,
    String repoVersion,
    Callback2 gotoPlace
  ) {
    SynapseFooterProps props = new SynapseFooterProps();
    props.portalVersion = portalVersion;
    props.srcVersion = srcVersion;
    props.repoVersion = repoVersion;
    props.gotoPlace = gotoPlace;
    return props;
  }
}
