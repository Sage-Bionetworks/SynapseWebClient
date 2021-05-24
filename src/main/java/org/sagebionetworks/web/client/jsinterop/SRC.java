package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class SRC {
	@JsType(isNative = true)
	public static class SynapseComponents {
		public static ReactFunctionComponent<SynapseContextProviderProps> SynapseContextProvider;
		public static ReactFunctionComponent<EntityFinderProps> EntityFinder;
		public static ReactFunctionComponent<EvaluationCardProps> EvaluationCard;
		public static ReactFunctionComponent<EvaluationEditorPageProps> EvaluationEditorPage;
		public static ReactFunctionComponent<AccessTokenPageProps> AccessTokenPage;
	}
}

