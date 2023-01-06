package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class HtmlPreviewProps extends ReactComponentProps {

  String createdByUserId;
  String rawHtml;

  @JsOverlay
  public static HtmlPreviewProps create(
    String createdByUserId,
    String rawHtml
  ) {
    HtmlPreviewProps props = new HtmlPreviewProps();
    props.createdByUserId = createdByUserId;
    props.rawHtml = rawHtml;
    return props;
  }
}
