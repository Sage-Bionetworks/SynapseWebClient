package org.sagebionetworks.web.client.jsinterop;

import com.google.gwt.core.client.JsArrayString;
import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.jsinterop.entity.actionmenu.EntityActionMenuPropsJsInterop;

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
    public static ReactComponentType<OrientationBannerProps> OrientationBanner;
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
    public static ReactComponentType ReviewerDashboard;
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
    public static ReactComponentType<EntityPageBreadcrumbsProps> EntityPageBreadcrumbs;
    public static ReactComponentType<EntityPageTitleBarProps> EntityPageTitleBar;

    public static ReactComponentType<EntityActionMenuPropsJsInterop> EntityActionMenu;
    public static ReactComponentType<HtmlPreviewProps> HtmlPreview;
    public static ReactComponentType<CreatedByModifiedByProps> CreatedByModifiedBy;
    public static ReactComponentType<TwoFactorAuthSettingsPanelProps> TwoFactorAuthSettingsPanel;
    public static ReactComponentType<TwoFactorBackupCodesProps> TwoFactorBackupCodes;
    public static ReactComponentType<TwoFactorEnrollmentFormProps> TwoFactorEnrollmentForm;
    public static ReactComponentType<EmptyProps> SubscriptionPage;
    public static ReactComponentType<AccessRequirementListProps> AccessRequirementList;

    /**
     * Pushes a global toast message. In SWC, you should use {@link DisplayUtils#notify}, rather than calling this method directly.
     * @param message
     * @param variant
     * @param options
     */
    public static native void displayToast(
      String message,
      String variant,
      @JsNullable ToastMessageOptions options
    );
  }

  @JsType(isNative = true)
  public static class SynapseContext {

    /* We use FullContextProvider because it will provide the SynapseContext, react-query QueryContext, and MUI Theme
     context for all React trees that we render */
    public static ReactComponentType<SynapseReactClientFullContextProviderProps> FullContextProvider;
  }

  @JsType(isNative = true)
  public static class SynapseConstants {

    public static JsArrayString PERSISTENT_LOCAL_STORAGE_KEYS;
  }
}
