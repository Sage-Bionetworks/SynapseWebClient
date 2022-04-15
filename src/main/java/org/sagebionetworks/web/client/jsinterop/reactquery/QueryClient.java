package org.sagebionetworks.web.client.jsinterop.reactquery;

import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = "ReactQuery")
public class QueryClient {

    public QueryClient(QueryClientOptions config) {}

	public native void invalidateQueries(Object[] queryKey);
}
