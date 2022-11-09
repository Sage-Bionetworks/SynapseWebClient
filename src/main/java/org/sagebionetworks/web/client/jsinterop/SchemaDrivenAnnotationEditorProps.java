package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class SchemaDrivenAnnotationEditorProps extends ReactComponentProps {

  @JsFunction
  public interface Callback {
    void run();
  }

  String entityId;

  @JsNullable
  Callback onSuccess;

  @JsOverlay
  public static SchemaDrivenAnnotationEditorProps create(
    String entityId,
    Callback onSuccess
  ) {
    SchemaDrivenAnnotationEditorProps props = new SchemaDrivenAnnotationEditorProps();
    props.entityId = entityId;
    props.onSuccess = onSuccess;
    return props;
  }
}
