package org.sagebionetworks.web.client.jsinterop.reactquery;

import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;


@JsType(isNative = true, namespace = JsPackage.GLOBAL, name="Object")
public class SynapseReactClientQueryKey {

	public String objectType;
	@JsNullable
	public String id;

    @JsOverlay
    public static SynapseReactClientQueryKey create(String objectType, String id) {
        SynapseReactClientQueryKey defaultQueryKey = new SynapseReactClientQueryKey();
		defaultQueryKey.objectType = objectType;
		defaultQueryKey.id = id;
        return defaultQueryKey;
    }
}



