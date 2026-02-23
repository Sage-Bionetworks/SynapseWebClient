package org.sagebionetworks.web.client.jsinterop;

import elemental2.promise.Promise;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * JsInterop accessor for the {@code window.SynapseSessionManager} singleton
 * instantiated in {@code globalContext.js}.
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class SynapseSessionManagerJs {

  /** Get the current session state snapshot (synchronous). */
  public native SessionStateJsObject getSnapshot();

  /**
   * Subscribe to session state changes.
   *
   * @param listener called whenever session state is updated
   * @return an unsubscribe function
   */
  public native UnsubscribeFunction subscribe(ListenerFunction listener);

  /** Start the 60s refresh loop. Must be called after SRC endpoints are configured. */
  public native void start();

  /** Refresh the session (async — re-reads cookie, validates via introspection). */
  public native Promise<Void> refreshSession();

  /** Clear the session (async — signs out, initializes anonymous session). */
  public native Promise<Void> clearSession();

  @FunctionalInterface
  @JsFunction
  public interface ListenerFunction {
    void onStateChange();
  }

  @FunctionalInterface
  @JsFunction
  public interface UnsubscribeFunction {
    void unsubscribe();
  }
}
