package org.sagebionetworks.web.client.jsinterop;

import org.sagebionetworks.web.client.DisplayUtils;

import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class SRC {
	public static String SynapseReactClientVersion;

	@JsType(isNative = true)
	public static class SynapseComponents {
		public static ReactFunctionComponent<EntityBadgeIconsProps> EntityBadgeIcons;
		public static ReactFunctionComponent<DatasetEditorProps> DatasetItemsEditor;
		public static ReactFunctionComponent<EntityFinderProps> EntityFinder;
		public static ReactFunctionComponent<EvaluationCardProps> EvaluationCard;
		public static ReactFunctionComponent<EvaluationEditorPageProps> EvaluationEditorPage;
		public static ReactFunctionComponent<AccessTokenPageProps> AccessTokenPage;
		public static ReactFunctionComponent<DownloadCartPageProps> DownloadCartPage;
		public static ReactFunctionComponent<DownloadConfirmationProps> DownloadConfirmation;
		public static ReactFunctionComponent<FullWidthAlertProps> FullWidthAlert;
		public static ReactFunctionComponent<SchemaDrivenAnnotationEditorProps> SchemaDrivenAnnotationEditor;
		public static ReactFunctionComponent<SynapseNavDrawerProps> SynapseNavDrawer;
		public static ReactFunctionComponent<EmptyProps> FavoritesPage;
		public static ReactFunctionComponent<EntityModalProps> EntityModal;
		public static ReactFunctionComponent<IconSvgProps> IconSvg;
		public static ReactFunctionComponent<EntityTypeIconProps> EntityTypeIcon;
		public static ReactFunctionComponent<UserProfileLinksProps> UserProfileLinks;
		public static ReactFunctionComponent<SkeletonButtonProps> SkeletonButton;
		public static ReactFunctionComponent<QueryWrapperPlotNavProps> QueryWrapperPlotNav;
		public static ReactFunctionComponent<StandaloneQueryWrapperProps> StandaloneQueryWrapper;
		public static ReactFunctionComponent<ForumSearchProps> ForumSearch;
		public static ReactFunctionComponent<ReviewerDashboardProps> ReviewerDashboard;
		public static ReactFunctionComponent SynapseToastContainer;
		public static ReactFunctionComponent<EmptyProps> OAuthManagement;

		/**
		 * Pushes a global toast message. In SWC, you should use {@link DisplayUtils#notify}, rather than calling this method directly.
		 * @param message
		 * @param variant
		 * @param options
		 */
		public static native void displayToast(String message, String variant, @JsNullable ToastMessageOptions options);
	}

	@JsType(isNative = true)
	public static class SynapseContext {
		public static ReactFunctionComponent<SynapseContextProviderProps> SynapseContextProvider;
	}
}

