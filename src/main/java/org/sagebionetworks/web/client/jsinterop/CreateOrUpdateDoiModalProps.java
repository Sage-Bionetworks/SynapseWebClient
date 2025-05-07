package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.sagebionetworks.repo.model.ObjectType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class CreateOrUpdateDoiModalProps extends ReactComponentProps {

  @JsFunction
  public interface Callback {
    void run();
  }

  /** Whether the dialog is open */
  boolean open;
  /** Callback to call when the dialog is closed */
  Callback onClose;
  /** The type of object */
  String objectType;
  /** The ID of the object */
  String objectId;
  /** The optional version number of the object used to populate the form. */
  Long defaultVersionNumber;

  @JsOverlay
  public static CreateOrUpdateDoiModalProps create(
    boolean open,
    Callback onClose,
    ObjectType objectType,
    String objectId,
    Long defaultVersionNumber
  ) {
    CreateOrUpdateDoiModalProps props = new CreateOrUpdateDoiModalProps();
    props.open = open;
    props.onClose = onClose;
    props.objectType = objectType.toString();
    props.objectId = objectId;
    props.defaultVersionNumber = defaultVersionNumber;
    return props;
  }
}
