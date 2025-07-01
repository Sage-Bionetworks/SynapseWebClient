package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class DynamicFormProps extends ReactComponentProps {

  @JsNullable
  String schemaUrl;

  @JsNullable
  String uiSchemaUrl;

  @JsNullable
  String postUrl;

  @JsOverlay
  public static DynamicFormProps create(
    String schemaUrl,
    String uiSchemaUrl,
    String postUrl
  ) {
    DynamicFormProps props = new DynamicFormProps();
    props.schemaUrl = schemaUrl;
    props.uiSchemaUrl = uiSchemaUrl;
    props.postUrl = postUrl;
    return props;
  }
}
