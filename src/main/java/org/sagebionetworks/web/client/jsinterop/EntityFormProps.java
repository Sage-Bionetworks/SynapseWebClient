package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class EntityFormProps extends ReactComponentProps {

  @FunctionalInterface
  @JsFunction
  public interface SynIdCallback {
    void run(String synId);
  }

  public String parentContainerId;
  public String formSchemaEntityId;
  public String formUiSchemaEntityId;
  public boolean initFormData;
  public SynIdCallback synIdCallback;
  public ReactRef<EntityFormHandle> ref;

  @JsOverlay
  public static EntityFormProps create(
    String parentContainerId,
    String formSchemaEntityId,
    String formUiSchemaEntityId,
    boolean initFormData,
    SynIdCallback synIdCallback,
    ReactRef<EntityFormHandle> ref
  ) {
    EntityFormProps props = new EntityFormProps();
    props.parentContainerId = parentContainerId;
    props.formSchemaEntityId = formSchemaEntityId;
    props.formUiSchemaEntityId = formUiSchemaEntityId;
    props.initFormData = initFormData;
    props.synIdCallback = synIdCallback;
    props.ref = ref;
    return props;
  }
}
