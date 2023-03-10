package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class TwoFactorEnrollmentFormProps extends ReactComponentProps {

  @JsFunction
  public interface Callback {
    void run();
  }

  Callback onTwoFactorEnrollmentSuccess;

  @JsOverlay
  public static TwoFactorEnrollmentFormProps create(
    Callback onTwoFactorEnrollmentSuccess
  ) {
    TwoFactorEnrollmentFormProps props = new TwoFactorEnrollmentFormProps();
    props.onTwoFactorEnrollmentSuccess = onTwoFactorEnrollmentSuccess;
    return props;
  }
}
