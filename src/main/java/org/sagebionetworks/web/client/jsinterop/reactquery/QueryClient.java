package org.sagebionetworks.web.client.jsinterop.reactquery;

import java.util.List;

import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = "ReactQuery")
public class QueryClient {

    public QueryClient(QueryClientOptions config) {}

	public native void resetQueries(List<SynapseReactClientQueryKey> queryKey);
}
