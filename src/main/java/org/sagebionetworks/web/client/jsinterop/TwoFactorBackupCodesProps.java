package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class TwoFactorBackupCodesProps extends ReactComponentProps {

  @JsFunction
  public interface Callback {
    void run();
  }

  /* Whether to show a warning before generating new codes, to prevent users from overwriting their existing codes */
  boolean showReplaceOldCodesWarning;
  /* Invoked when the user decides not to generate new codes, or has acknowledged new codes. */
  Callback onClose;

  @JsOverlay
  public static TwoFactorBackupCodesProps create(
    boolean showReplaceOldCodesWarning,
    Callback onClose
  ) {
    TwoFactorBackupCodesProps props = new TwoFactorBackupCodesProps();
    props.showReplaceOldCodesWarning = showReplaceOldCodesWarning;
    props.onClose = onClose;
    return props;
  }
}
