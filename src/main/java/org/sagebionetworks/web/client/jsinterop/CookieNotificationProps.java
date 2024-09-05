package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class CookieNotificationProps extends ReactComponentProps {

  @JsFunction
  public interface Callback {
    void run(CookiePreference prefs);
  }

  public Callback onClose;

  @JsOverlay
  public static CookieNotificationProps create(Callback onClose) {
    CookieNotificationProps props = new CookieNotificationProps();
    props.onClose = onClose;
    return props;
  }
}
