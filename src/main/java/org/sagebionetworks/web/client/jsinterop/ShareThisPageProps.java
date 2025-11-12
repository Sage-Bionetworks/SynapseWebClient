package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class ShareThisPageProps extends ReactComponentProps {

  @JsFunction
  @FunctionalInterface
  public interface Callback {
    void run();
  }

  public String shortIoPublicApiKey;
  public String domain;
  public Boolean open;
  public Callback onClose;
  public String renderAs;

  @JsOverlay
  public final void setOpen(Boolean open) {
    this.open = open;
  }

  @JsOverlay
  public final void setOnClose(Callback onClose) {
    this.onClose = onClose;
  }

  @JsOverlay
  public static ShareThisPageProps create(
    String variant,
    String shortIoPublicApiKey,
    String domain
  ) {
    ShareThisPageProps props = new ShareThisPageProps();
    props.shortIoPublicApiKey = shortIoPublicApiKey;
    props.domain = domain;
    return props;
  }

  @JsOverlay
  public static ShareThisPageProps create() {
    return create(null, null, null);
  }

  @JsOverlay
  public final void setShortIoPublicApiKey(String shortIoPublicApiKey) {
    this.shortIoPublicApiKey = shortIoPublicApiKey;
  }

  @JsOverlay
  public final void setDomain(String domain) {
    this.domain = domain;
  }

  @JsOverlay
  public final void setRenderAs(String renderAs) {
    this.renderAs = renderAs;
  }
}
