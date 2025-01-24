package org.sagebionetworks.web.client.jsinterop;

import java.util.Map;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class CardConfiguration {

  String type;
  double secondaryLabelLimit;
  GenericCardSchema genericCardSchema;

  @JsOverlay
  public static CardConfiguration create(
    String type,
    int secondaryLabelLimit,
    GenericCardSchema genericCardSchema
  ) {
    CardConfiguration config = new CardConfiguration();
    config.type = type;
    config.secondaryLabelLimit = secondaryLabelLimit;
    config.genericCardSchema = genericCardSchema;
    return config;
  }
}
