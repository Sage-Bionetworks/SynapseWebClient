package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class DownloadCartPageProps extends ReactComponentProps {

  @FunctionalInterface
  @JsFunction
  public interface OnViewSharingSettingsHandler {
    void onViewSharingSettingsClicked(String benefactorEntityId);
  }

  @JsNullable
  OnViewSharingSettingsHandler onViewSharingSettingsClicked;

  @JsOverlay
  public static DownloadCartPageProps create(
    OnViewSharingSettingsHandler onViewSharingSettingsClicked
  ) {
    DownloadCartPageProps props = new DownloadCartPageProps();
    props.onViewSharingSettingsClicked = onViewSharingSettingsClicked;
    return props;
  }
}
