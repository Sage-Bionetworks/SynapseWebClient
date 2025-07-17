package org.sagebionetworks.web.client.jsinterop.context;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.sagebionetworks.web.client.jsinterop.ReactComponentProps;
import org.sagebionetworks.web.client.jsinterop.ReactComponentType;
import org.sagebionetworks.web.client.jsinterop.SynapseReactClientFullContextProviderProps;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class ContextUtils {

  public static ReactComponentType<
    ? extends ReactComponentProps
  > SynapseContextProviderFromStore;

  public static native void setGlobalContext(
    SynapseReactClientFullContextProviderProps props
  );
}
