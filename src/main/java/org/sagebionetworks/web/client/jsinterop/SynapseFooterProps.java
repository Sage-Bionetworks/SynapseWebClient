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

  @JsFunction
  public interface Callback3 {
    void run(boolean experimentalMode);
  }

  Callback3 onExperimentalModeToggle;

  @JsOverlay
  public static SynapseFooterProps create(
    String portalVersion,
    String srcVersion,
    String repoVersion,
    Callback2 gotoPlace,
    Callback3 onExperimentalModeToggle
  ) {
    SynapseFooterProps props = new SynapseFooterProps();
    props.portalVersion = portalVersion;
    props.srcVersion = srcVersion;
    props.repoVersion = repoVersion;
    props.gotoPlace = gotoPlace;
    props.onExperimentalModeToggle = onExperimentalModeToggle;
    return props;
  }
}
