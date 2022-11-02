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
		public static ReactComponentType<EntityBadgeIconsProps> EntityBadgeIcons;
		public static ReactComponentType<DatasetEditorProps> DatasetItemsEditor;
		public static ReactComponentType<EntityFinderProps> EntityFinder;
		public static ReactComponentType<EvaluationCardProps> EvaluationCard;
		public static ReactComponentType<EvaluationEditorPageProps> EvaluationEditorPage;
		public static ReactComponentType<AccessTokenPageProps> AccessTokenPage;
		public static ReactComponentType<DownloadCartPageProps> DownloadCartPage;
		public static ReactComponentType<DownloadConfirmationProps> DownloadConfirmation;
		public static ReactComponentType<FullWidthAlertProps> FullWidthAlert;
		public static ReactComponentType<SchemaDrivenAnnotationEditorProps> SchemaDrivenAnnotationEditor;
		public static ReactComponentType<SynapseNavDrawerProps> SynapseNavDrawer;
		public static ReactComponentType<EmptyProps> FavoritesPage;
		public static ReactComponentType<EntityModalProps> EntityModal;
		public static ReactComponentType<IconSvgProps> IconSvg;
		public static ReactComponentType<EntityTypeIconProps> EntityTypeIcon;
		public static ReactComponentType<UserProfileLinksProps> UserProfileLinks;
		public static ReactComponentType<SkeletonButtonProps> SkeletonButton;
		public static ReactComponentType<QueryWrapperPlotNavProps> QueryWrapperPlotNav;
		public static ReactComponentType<StandaloneQueryWrapperProps> StandaloneQueryWrapper;
		public static ReactComponentType<ForumSearchProps> ForumSearch;
		public static ReactComponentType<ReviewerDashboardProps> ReviewerDashboard;
		public static ReactComponentType<ProvenanceGraphProps> ProvenanceGraph;
		public static ReactComponentType SynapseToastContainer;
		public static ReactComponentType<EmptyProps> OAuthManagement;
		public static ReactComponentType TrashCanList;
		public static ReactComponentType<SynapseHomepageProps> SynapseHomepage;
		public static ReactComponentType<ErrorPageProps> ErrorPage;
		public static ReactComponentType<LoginPageProps> LoginPage;
		public static ReactComponentType<HasAccessProps> HasAccess;
		public static ReactComponentType<UserCardProps> UserCard;
		public static ReactComponentType<AccountLevelBadgeProps> AccountLevelBadge;
		public static ReactComponentType<PageProgressProps> PageProgress;
		public static ReactComponentType<TermsAndConditionsProps> TermsAndConditions;
		public static ReactComponentType<IDUReportProps> IDUReport;
		public static ReactComponentType CertificationQuiz;

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
		public static ReactComponentType<SynapseContextProviderProps> SynapseContextProvider;
	}
}

