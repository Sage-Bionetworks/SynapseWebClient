package org.sagebionetworks.web.client.jsinterop;

import org.sagebionetworks.web.client.utils.Callback;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class SRC {
	@JsType(isNative = true)
	public static class SynapseComponents {
		public static ReactFunctionComponent<EntityFinderProps> EntityFinder;
		public static ReactFunctionComponent<EvaluationCardProps> EvaluationCard;
		public static ReactFunctionComponent<EvaluationEditorPageProps> EvaluationEditorPage;
		public static ReactFunctionComponent<AccessTokenPageProps> AccessTokenPage;
		public static ReactFunctionComponent<EmptyProps> DownloadCartPage;
		public static ReactFunctionComponent<ShowDownloadV2Props> ShowDownloadV2;
		public static ReactFunctionComponent<DownloadConfirmationProps> DownloadConfirmation;
		public static ReactFunctionComponent<FullWidthAlertProps> FullWidthAlert;
		public static ReactFunctionComponent<SchemaDrivenAnnotationEditorProps> SchemaDrivenAnnotationEditor;
		public static ReactFunctionComponent<SynapseNavDrawerProps> SynapseNavDrawer;
		public static ReactFunctionComponent<EmptyProps> FavoritesPage;
		public static ReactFunctionComponent<EntityModalProps> EntityModal;
		public static ReactFunctionComponent SynapseToastContainer;
		public static native void displayToast(String variant, String title, String description, Long autoCloseInMs, String primaryButtonText, Callback onPrimaryButtonClick, String secondaryButtonText, String secondaryButtonHref);
	}

	@JsType(isNative = true)
	public static class SynapseContext {
		public static ReactFunctionComponent<SynapseContextProviderProps> SynapseContextProvider;
	}
}

