package org.sagebionetworks.web.client.jsinterop;

import java.util.Map;
import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class GenericCardSchema {

  String type;
  String title;
  String subTitle;
  String description;
  String[] secondaryLabels;

  @JsOverlay
  public static GenericCardSchema create(
    String type,
    String title,
    String subTitle,
    String description,
    String[] secondaryLabels
  ) {
    GenericCardSchema config = new GenericCardSchema();
    config.type = type;
    config.title = title;
    config.subTitle = subTitle;
    config.description = description;
    config.secondaryLabels = secondaryLabels;
    return config;
  }
}
