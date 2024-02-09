package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class EntityModalProps extends ReactComponentProps {

  @JsFunction
  public interface Callback {
    void run();
  }

  @JsFunction
  public interface BooleanCallback {
    void run(boolean value);
  }

  String entityId;
  boolean show;

  @JsNullable
  Callback onClose;

  @JsNullable
  String initialTab; // "METADATA" | "ANNOTATIONS"

  @JsNullable
  boolean showTabs;

  @JsNullable
  Double versionNumber;

  @JsNullable
  BooleanCallback onEditModeChanged;

  @JsOverlay
  public static EntityModalProps create(
    String entityId,
    Long versionNumber,
    boolean show,
    Callback onClose,
    String initialTab,
    boolean showTabs,
    BooleanCallback onEditModeChanged
  ) {
    EntityModalProps props = new EntityModalProps();
    props.entityId = entityId;
    if (versionNumber != null) {
      props.versionNumber = versionNumber.doubleValue();
    }
    props.show = show;
    props.onClose = onClose;
    props.initialTab = initialTab;
    props.showTabs = showTabs;
    props.onEditModeChanged = onEditModeChanged;
    return props;
  }
}
