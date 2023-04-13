package org.sagebionetworks.web.client.context;

import org.sagebionetworks.web.client.jsinterop.ReactComponentProps;
import org.sagebionetworks.web.client.jsinterop.ReactComponentType;
import org.sagebionetworks.web.client.jsinterop.SynapseReactClientFullContextProviderProps;
import org.sagebionetworks.web.client.jsni.FullContextProviderPropsJSNIObject;

/**
 * Synapse React Client components must be wrapped in a SynapseContextProvider. A SynapseContextPropsProvider provides the props for the
 * SynapseContextProvider.
 */
public interface SynapseReactClientFullContextPropsProvider {
  /**
   * Provides props for {@link org.sagebionetworks.web.client.jsinterop.SRC.SynapseContext#FullContextProvider}.
   * Typically, props will be supplied to {@link org.sagebionetworks.web.client.jsinterop.React#createElementWithSynapseContext(ReactComponentType, ReactComponentProps, SynapseReactClientFullContextProviderProps)}
   */
  SynapseReactClientFullContextProviderProps getJsInteropContextProps();

  /**
   * Provides JSNI-compatible props for SynapseContextProvider. If you're porting a new React component, please consider
   * using JsInterop before using JSNI.
   */
  FullContextProviderPropsJSNIObject getJsniContextProps();
}
