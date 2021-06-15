package org.sagebionetworks.web.client.jsinterop.reactquery;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name="Object")
public class QueryOptions {
    public Long staleTime;
    public boolean retry;

    @JsOverlay
    public static QueryOptions create() {
        QueryOptions queryOptions = new QueryOptions();
        // We don't currently need to configure these values
        queryOptions.staleTime = 30 * 1000L; // 30s
        queryOptions.retry = false; // SynapseClient knows which queries to retry
        return queryOptions;
    }
}
