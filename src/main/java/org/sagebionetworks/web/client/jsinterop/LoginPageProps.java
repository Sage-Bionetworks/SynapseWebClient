package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class LoginPageProps extends ReactComponentProps {

  @JsFunction
  public interface Callback {
    void run();
  }

  String ssoRedirectUrl;
  String redirectUrl;
  Callback sessionCallback;

  @JsOverlay
  public static LoginPageProps create(
    String ssoRedirectUrl,
    String redirectUrl,
    Callback sessionCallback
  ) {
    LoginPageProps props = new LoginPageProps();
    props.ssoRedirectUrl = ssoRedirectUrl;
    props.redirectUrl = redirectUrl;
    props.sessionCallback = sessionCallback;
    return props;
  }
}
