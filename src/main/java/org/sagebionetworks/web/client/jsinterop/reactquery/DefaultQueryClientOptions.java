package org.sagebionetworks.web.client.jsinterop.reactquery;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;


@JsType(isNative = true, namespace = JsPackage.GLOBAL, name="Object")
public class DefaultQueryClientOptions {
    public QueryOptions query;

    @JsOverlay
    public static DefaultQueryClientOptions create() {
        DefaultQueryClientOptions defaultQueryClientOptions = new DefaultQueryClientOptions();
        defaultQueryClientOptions.query = QueryOptions.create();
        return defaultQueryClientOptions;
    }
}



