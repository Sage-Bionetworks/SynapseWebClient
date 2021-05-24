package org.sagebionetworks.web.client.context;

import org.sagebionetworks.web.client.jsinterop.ReactComponentProps;
import org.sagebionetworks.web.client.jsinterop.ReactFunctionComponent;
import org.sagebionetworks.web.client.jsinterop.SynapseContextProviderProps;
import org.sagebionetworks.web.client.jsni.SynapseContextProviderPropsJSNIObject;

/**
 * Synapse React Client components must be wrapped in a SynapseContextProvider. A SynapseContextPropsProvider provides the props for the 
 * SynapseContextProvider.
 */
public interface SynapseContextPropsProvider {
    /**
     * Provides props for {@link org.sagebionetworks.web.client.jsinterop.SRC.SynapseComponents#SynapseContextProvider}.
     * Typically, props will be supplied to {@link org.sagebionetworks.web.client.jsinterop.React#createElementWithSynapseContext(ReactFunctionComponent, ReactComponentProps, SynapseContextProviderProps)}
     */
    SynapseContextProviderProps getJsInteropContextProps();

    /**
     * Provides JSNI-compatible props for SynapseContextProvider. If you're porting a new React component, please consider
     * using JsInterop before using JSNI.
     */
    SynapseContextProviderPropsJSNIObject getJsniContextProps();
}
