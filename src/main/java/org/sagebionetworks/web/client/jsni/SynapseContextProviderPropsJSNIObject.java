package org.sagebionetworks.web.client.jsni;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * JSNI-compatible object for SynapseContextProvider props.
 *
 * If you're porting a new React component, please consider using JsInterop before using this object.
 */
public class SynapseContextProviderPropsJSNIObject extends JavaScriptObject {

  protected SynapseContextProviderPropsJSNIObject() {}

  public static native SynapseContextProviderPropsJSNIObject create() /*-{
        return {};
    }-*/;

  public final native void setSynapseContext(
    SynapseContextJSNIObject context
  ) /*-{
        this.synapseContext = context;
    }-*/;

  public final native void setQueryClient(
    QueryClientJSNIObject queryClient
  ) /*-{
        this.queryClient = queryClient;
    }-*/;
  // This object also supports setting a react-query query client, but we aren't exposing that for now
}
