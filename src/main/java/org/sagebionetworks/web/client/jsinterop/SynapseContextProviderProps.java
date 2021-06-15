package org.sagebionetworks.web.client.jsinterop;

import org.sagebionetworks.web.client.jsinterop.reactquery.QueryClient;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name="Object")
public class SynapseContextProviderProps extends ReactComponentProps {

	public SynapseContextJsObject synapseContext;
	public QueryClient queryClient;

	@JsOverlay
	public static SynapseContextProviderProps create(SynapseContextJsObject synapseContext, QueryClient queryClient) {
		SynapseContextProviderProps props = new SynapseContextProviderProps();
		props.synapseContext = synapseContext;
		props.queryClient = queryClient;
		return props;
	}
}
