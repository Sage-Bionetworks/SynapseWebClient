package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class DatasetEditorProps extends ReactComponentProps {

  @JsFunction
  public interface Callback {
    void run();
  }

  @JsFunction
  public interface BooleanCallback {
    void run(boolean arg);
  }

  String entityId;
  Callback onSave;
  Callback onClose;
  BooleanCallback onUnsavedChangesChange;

  @JsOverlay
  public static DatasetEditorProps create(
    String entityId,
    Callback onSave,
    Callback onClose,
    BooleanCallback onUnsavedChangesChange
  ) {
    DatasetEditorProps props = new DatasetEditorProps();
    props.entityId = entityId;
    props.onSave = onSave;
    props.onClose = onClose;
    props.onUnsavedChangesChange = onUnsavedChangesChange;
    return props;
  }
}
