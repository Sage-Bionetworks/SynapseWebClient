package org.sagebionetworks.web.client.jsni;

import com.google.gwt.core.client.JavaScriptObject;

public class QueryClientJSNIObject extends JavaScriptObject {

  protected QueryClientJSNIObject() {}

  public static native QueryClientJSNIObject getQueryClientSingleton() /*-{
        return $wnd.SynapseQueryClient;
    }-*/;
}
