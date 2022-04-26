package org.sagebionetworks.web.client.jsinterop.reactquery;

import java.util.Arrays;
import java.util.List;

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
    public static List<SynapseReactClientQueryKey> create(String objectType, String id) {
        SynapseReactClientQueryKey defaultQueryKey = new SynapseReactClientQueryKey();
		defaultQueryKey.objectType = objectType;
		defaultQueryKey.id = id;
        return Arrays.asList(defaultQueryKey);
    }
}



