package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class TwoFactorAuthSettingsPanelProps extends ReactComponentProps {

  @JsFunction
  public interface Callback {
    void run();
  }

  Callback onBeginTwoFactorEnrollment;
  Callback onRegenerateBackupCodes;
  boolean showTitle;

  @JsOverlay
  public static TwoFactorAuthSettingsPanelProps create(
    Callback onBeginTwoFactorEnrollment,
    Callback onRegenerateBackupCodes
  ) {
    TwoFactorAuthSettingsPanelProps props =
      new TwoFactorAuthSettingsPanelProps();
    props.onBeginTwoFactorEnrollment = onBeginTwoFactorEnrollment;
    props.onRegenerateBackupCodes = onRegenerateBackupCodes;
    props.showTitle = false; // always false in SWC
    return props;
  }
}
