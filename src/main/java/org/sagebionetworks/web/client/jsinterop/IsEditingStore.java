package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.sagebionetworks.web.client.jsinterop.util.JsConsumer;
import org.sagebionetworks.web.client.jsinterop.util.JsRunnable;
import org.sagebionetworks.web.client.jsinterop.util.JsSupplier;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class IsEditingStore {

  @FunctionalInterface
  @JsFunction
  public interface SubscribeFn {
    JsRunnable subscribe(JsRunnable callback);
  }

  SubscribeFn subscribe;

  JsSupplier<Boolean> getSnapshot;

  JsConsumer<Boolean> setIsEditing;

  @JsOverlay
  public static IsEditingStore create(
    SubscribeFn subscribe,
    JsSupplier<Boolean> getSnapshot,
    JsConsumer<Boolean> setIsEditing
  ) {
    IsEditingStore props = new IsEditingStore();
    props.subscribe = subscribe;
    props.getSnapshot = getSnapshot;
    props.setIsEditing = setIsEditing;
    return props;
  }
}
