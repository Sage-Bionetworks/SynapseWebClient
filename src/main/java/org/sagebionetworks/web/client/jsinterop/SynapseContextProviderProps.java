package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name="Object")
public class SynapseContextProviderProps extends ReactComponentProps {

	public SynapseContextJsObject synapseContext;

	@JsOverlay
	public static SynapseContextProviderProps create(SynapseContextJsObject synapseContext) {
		SynapseContextProviderProps props = new SynapseContextProviderProps();
		props.synapseContext = synapseContext;
		return props;
	}
}
