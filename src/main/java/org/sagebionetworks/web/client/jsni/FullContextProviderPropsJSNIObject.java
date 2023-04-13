package org.sagebionetworks.web.client.jsni;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * JSNI-compatible object for FullContextProvider props.
 *
 * If you're porting a new React component, please consider using JsInterop before using this object.
 */
public class FullContextProviderPropsJSNIObject extends JavaScriptObject {

  protected FullContextProviderPropsJSNIObject() {}

  public static native FullContextProviderPropsJSNIObject create() /*-{
        return {};
    }-*/;

  public final native void setSynapseContext(
    SynapseReactClientFullContextJSNIObject context
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
