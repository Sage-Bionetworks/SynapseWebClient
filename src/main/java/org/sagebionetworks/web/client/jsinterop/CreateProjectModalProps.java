package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class CreateProjectModalProps extends ReactComponentProps {

  @FunctionalInterface
  @JsFunction
  public interface Callback {
    void run();
  }

  @FunctionalInterface
  @JsFunction
  public interface GotoPlaceCallback {
    void run(String targetHref);
  }

  public boolean isShowingModal;

  public Callback onClose;

  public GotoPlaceCallback gotoPlace;

  @JsOverlay
  public static CreateProjectModalProps create(
    boolean isShowingModal,
    Callback onClose,
    GotoPlaceCallback gotoPlace
  ) {
    CreateProjectModalProps props = new CreateProjectModalProps();
    props.isShowingModal = isShowingModal;
    props.onClose = onClose;
    props.gotoPlace = gotoPlace;
    return props;
  }
}
