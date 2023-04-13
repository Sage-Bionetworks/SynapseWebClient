package org.sagebionetworks.web.client.context;

import org.sagebionetworks.web.client.jsinterop.ReactComponentProps;
import org.sagebionetworks.web.client.jsinterop.ReactComponentType;
import org.sagebionetworks.web.client.jsinterop.SynapseReactClientFullContextProviderProps;
import org.sagebionetworks.web.client.jsni.FullContextProviderPropsJSNIObject;

/**
 * Synapse React Client components must be wrapped in a SynapseContext, react-query QueryContext, and MUI Theme context.
 * Implementers of this interface provide the props for a FullContextProvider, which provides all of these contexts.
 */
public interface SynapseReactClientFullContextPropsProvider {
  /**
   * Provides props for {@link org.sagebionetworks.web.client.jsinterop.SRC.SynapseContext#FullContextProvider}.
   * Typically, props will be supplied to {@link org.sagebionetworks.web.client.jsinterop.React#createElementWithSynapseContext(ReactComponentType, ReactComponentProps, SynapseReactClientFullContextProviderProps)}
   */
  SynapseReactClientFullContextProviderProps getJsInteropContextProps();

  /**
   * Provides JSNI-compatible props for FullContextProvider. If you're porting a new React component, please consider
   * using JsInterop before using JSNI.
   */
  FullContextProviderPropsJSNIObject getJsniContextProps();
}
