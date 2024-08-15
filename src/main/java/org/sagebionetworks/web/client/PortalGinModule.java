package org.sagebionetworks.web.client;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;
import org.sagebionetworks.gwt.client.schema.adapter.GwtAdapterFactory;
import org.sagebionetworks.gwt.client.schema.adapter.JSONArrayGwt;
import org.sagebionetworks.gwt.client.schema.adapter.JSONObjectGwt;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONArrayAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.cache.*;
import org.sagebionetworks.web.client.context.*;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.cookie.GWTCookieImpl;
import org.sagebionetworks.web.client.presenter.*;
import org.sagebionetworks.web.client.resources.ResourceLoader;
import org.sagebionetworks.web.client.resources.ResourceLoaderImpl;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.security.AuthenticationControllerImpl;
import org.sagebionetworks.web.client.transform.JsoProvider;
import org.sagebionetworks.web.client.transform.JsoProviderImpl;
import org.sagebionetworks.web.client.view.*;
import org.sagebionetworks.web.client.view.users.*;
import org.sagebionetworks.web.client.widget.*;
import org.sagebionetworks.web.client.widget.accessrequirements.*;
import org.sagebionetworks.web.client.widget.accessrequirements.approval.AccessorGroupView;
import org.sagebionetworks.web.client.widget.accessrequirements.approval.AccessorGroupViewImpl;
import org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement.*;
import org.sagebionetworks.web.client.widget.accessrequirements.submission.ACTDataAccessSubmissionWidgetView;
import org.sagebionetworks.web.client.widget.accessrequirements.submission.ACTDataAccessSubmissionWidgetViewImpl;
import org.sagebionetworks.web.client.widget.accessrequirements.submission.OpenSubmissionWidgetView;
import org.sagebionetworks.web.client.widget.accessrequirements.submission.OpenSubmissionWidgetViewImpl;
import org.sagebionetworks.web.client.widget.asynch.*;
import org.sagebionetworks.web.client.widget.biodalliance13.BiodallianceWidgetView;
import org.sagebionetworks.web.client.widget.biodalliance13.BiodallianceWidgetViewImpl;
import org.sagebionetworks.web.client.widget.biodalliance13.editor.BiodallianceEditorView;
import org.sagebionetworks.web.client.widget.biodalliance13.editor.BiodallianceEditorViewImpl;
import org.sagebionetworks.web.client.widget.biodalliance13.editor.BiodallianceSourceEditorView;
import org.sagebionetworks.web.client.widget.biodalliance13.editor.BiodallianceSourceEditorViewImpl;
import org.sagebionetworks.web.client.widget.breadcrumb.BreadcrumbView;
import org.sagebionetworks.web.client.widget.breadcrumb.BreadcrumbViewImpl;
import org.sagebionetworks.web.client.widget.clienthelp.*;
import org.sagebionetworks.web.client.widget.csv.PapaCSVParser;
import org.sagebionetworks.web.client.widget.discussion.*;
import org.sagebionetworks.web.client.widget.discussion.modal.DiscussionThreadModalView;
import org.sagebionetworks.web.client.widget.discussion.modal.DiscussionThreadModalViewImpl;
import org.sagebionetworks.web.client.widget.discussion.modal.ReplyModalView;
import org.sagebionetworks.web.client.widget.discussion.modal.ReplyModalViewImpl;
import org.sagebionetworks.web.client.widget.docker.*;
import org.sagebionetworks.web.client.widget.docker.modal.AddExternalRepoModalView;
import org.sagebionetworks.web.client.widget.docker.modal.AddExternalRepoModalViewImpl;
import org.sagebionetworks.web.client.widget.doi.CreateOrUpdateDoiModalView;
import org.sagebionetworks.web.client.widget.doi.CreateOrUpdateDoiModalViewImpl;
import org.sagebionetworks.web.client.widget.doi.DoiWidgetV2View;
import org.sagebionetworks.web.client.widget.doi.DoiWidgetV2ViewImpl;
import org.sagebionetworks.web.client.widget.entity.*;
import org.sagebionetworks.web.client.widget.entity.act.*;
import org.sagebionetworks.web.client.widget.entity.annotation.*;
import org.sagebionetworks.web.client.widget.entity.browse.*;
import org.sagebionetworks.web.client.widget.entity.controller.*;
import org.sagebionetworks.web.client.widget.entity.dialog.BaseEditWidgetDescriptorView;
import org.sagebionetworks.web.client.widget.entity.dialog.BaseEditWidgetDescriptorViewImpl;
import org.sagebionetworks.web.client.widget.entity.download.*;
import org.sagebionetworks.web.client.widget.entity.editor.*;
import org.sagebionetworks.web.client.widget.entity.file.*;
import org.sagebionetworks.web.client.widget.entity.file.downloadlist.PackageSizeSummaryView;
import org.sagebionetworks.web.client.widget.entity.file.downloadlist.PackageSizeSummaryViewImpl;
import org.sagebionetworks.web.client.widget.entity.menu.v3.EntityActionMenu;
import org.sagebionetworks.web.client.widget.entity.menu.v3.EntityActionMenuImpl;
import org.sagebionetworks.web.client.widget.entity.menu.v3.EntityActionMenuView;
import org.sagebionetworks.web.client.widget.entity.menu.v3.EntityActionMenuViewImpl;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrarImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.*;
import org.sagebionetworks.web.client.widget.entity.restriction.v2.RestrictionWidgetView;
import org.sagebionetworks.web.client.widget.entity.restriction.v2.RestrictionWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.tabs.*;
import org.sagebionetworks.web.client.widget.evaluation.*;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.footer.FooterView;
import org.sagebionetworks.web.client.widget.footer.FooterViewImpl;
import org.sagebionetworks.web.client.widget.googlemap.GoogleMapView;
import org.sagebionetworks.web.client.widget.googlemap.GoogleMapViewImpl;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.header.HeaderView;
import org.sagebionetworks.web.client.widget.header.HeaderViewImpl;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadCallbackQueue;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadCallbackQueueImpl;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadWikiWidgetWrapperView;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadWikiWidgetWrapperViewImpl;
import org.sagebionetworks.web.client.widget.login.LoginModalView;
import org.sagebionetworks.web.client.widget.login.LoginModalViewImpl;
import org.sagebionetworks.web.client.widget.login.LoginWidgetView;
import org.sagebionetworks.web.client.widget.login.LoginWidgetViewImpl;
import org.sagebionetworks.web.client.widget.modal.Dialog;
import org.sagebionetworks.web.client.widget.modal.DialogView;
import org.sagebionetworks.web.client.widget.pageprogress.PageProgressWidgetView;
import org.sagebionetworks.web.client.widget.pageprogress.PageProgressWidgetViewImpl;
import org.sagebionetworks.web.client.widget.pagination.BasicPaginationView;
import org.sagebionetworks.web.client.widget.pagination.BasicPaginationViewImpl;
import org.sagebionetworks.web.client.widget.profile.*;
import org.sagebionetworks.web.client.widget.provenance.v2.ProvenanceWidgetView;
import org.sagebionetworks.web.client.widget.provenance.v2.ProvenanceWidgetViewImpl;
import org.sagebionetworks.web.client.widget.refresh.RefreshAlertView;
import org.sagebionetworks.web.client.widget.refresh.RefreshAlertViewImpl;
import org.sagebionetworks.web.client.widget.search.SearchBoxView;
import org.sagebionetworks.web.client.widget.search.SearchBoxViewImpl;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBoxView;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBoxViewImpl;
import org.sagebionetworks.web.client.widget.sharing.*;
import org.sagebionetworks.web.client.widget.statistics.StatisticsPlotWidgetView;
import org.sagebionetworks.web.client.widget.statistics.StatisticsPlotWidgetViewImpl;
import org.sagebionetworks.web.client.widget.subscription.SubscribeButtonWidgetView;
import org.sagebionetworks.web.client.widget.subscription.SubscribeButtonWidgetViewImpl;
import org.sagebionetworks.web.client.widget.subscription.TopicWidgetView;
import org.sagebionetworks.web.client.widget.subscription.TopicWidgetViewImpl;
import org.sagebionetworks.web.client.widget.table.TableListWidgetView;
import org.sagebionetworks.web.client.widget.table.TableListWidgetViewImpl;
import org.sagebionetworks.web.client.widget.table.api.APITableWidgetView;
import org.sagebionetworks.web.client.widget.table.api.APITableWidgetViewImpl;
import org.sagebionetworks.web.client.widget.table.modal.download.*;
import org.sagebionetworks.web.client.widget.table.modal.fileview.*;
import org.sagebionetworks.web.client.widget.table.modal.upload.*;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardView;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardViewImpl;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidgetImpl;
import org.sagebionetworks.web.client.widget.table.v2.*;
import org.sagebionetworks.web.client.widget.table.v2.results.*;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.*;
import org.sagebionetworks.web.client.widget.table.v2.schema.*;
import org.sagebionetworks.web.client.widget.team.*;
import org.sagebionetworks.web.client.widget.team.controller.*;
import org.sagebionetworks.web.client.widget.upload.*;
import org.sagebionetworks.web.client.widget.user.UserBadgeView;
import org.sagebionetworks.web.client.widget.user.UserBadgeViewImpl;

public class PortalGinModule extends AbstractGinModule {

  @Override
  protected void configure() {
    // Event Bus
    bind(EventBus.class).to(SimpleEventBus.class).in(Singleton.class);

    // JsoProvider
    bind(JsoProvider.class).to(JsoProviderImpl.class);
    bind(JsoProviderImpl.class).in(Singleton.class);
    // AuthenticationController
    bind(AuthenticationController.class)
      .to(AuthenticationControllerImpl.class)
      .in(Singleton.class);
    // GlobalApplicationState
    bind(GlobalApplicationState.class)
      .to(GlobalApplicationStateImpl.class)
      .in(Singleton.class);

    bind(GlobalApplicationStateView.class)
      .to(GlobalApplicationStateViewImpl.class);
    bind(LazyLoadCallbackQueue.class)
      .to(LazyLoadCallbackQueueImpl.class)
      .in(Singleton.class);

    bind(ResourceLoader.class).to(ResourceLoaderImpl.class).in(Singleton.class);

    // Header & Footer
    bind(Header.class).in(Singleton.class);

    bind(HeaderView.class).to(HeaderViewImpl.class).in(Singleton.class);
    bind(Footer.class).in(Singleton.class);
    bind(FooterView.class).to(FooterViewImpl.class).in(Singleton.class);
    // JSONAdapters
    bind(JSONObjectAdapter.class).to(JSONObjectGwt.class);

    bind(JSONArrayAdapter.class).to(JSONArrayGwt.class);
    // cache place presenters
    bind(ProfilePresenter.class).in(Singleton.class);

    bind(EntityPresenter.class).in(Singleton.class);
    bind(DownPresenter.class).in(Singleton.class);
    bind(SignedTokenPresenter.class).in(Singleton.class);
    bind(PersonalAccessTokensPresenter.class).in(Singleton.class);
    bind(AnnotationsRendererWidgetView.class)
      .to(AnnotationsRendererWidgetViewImpl.class);

    bind(VersionHistoryWidgetView.class).to(VersionHistoryWidgetViewImpl.class);
    // GWT utility methods
    bind(GWTWrapper.class).to(GWTWrapperImpl.class).in(Singleton.class);

    bind(GWTTimer.class).to(GWTTimerImpl.class);
    bind(SessionDetector.class).in(Singleton.class);

    bind(WebStorageMaxSizeDetector.class).in(Singleton.class);
    // RequestBuilder
    bind(RequestBuilderWrapper.class).to(RequestBuilderWrapperImpl.class);

    // Adapter factoyr
    bind(AdapterFactory.class).to(GwtAdapterFactory.class);

    // ClientCache
    bind(ClientCache.class).to(ClientCacheImpl.class).in(Singleton.class);

    // Storage wrapper
    bind(StorageWrapper.class).to(StorageImpl.class).in(Singleton.class);

    /*
     * Vanilla Implementation binding
     */

    // JSNI impls
    bind(SynapseJSNIUtils.class)
      .to(SynapseJSNIUtilsImpl.class)
      .in(Singleton.class);
    /*
     * Places
     */

    // The home page
    bind(HomeView.class).to(HomeViewImpl.class).in(Singleton.class);

    // EntityView
    bind(EntityView.class).to(EntityViewImpl.class).in(Singleton.class);

    // LoginView
    bind(LoginView.class).to(LoginViewImpl.class).in(Singleton.class);

    // PasswordResetView
    bind(PasswordResetView.class)
      .to(PasswordResetViewImpl.class)
      .in(Singleton.class);

    // NewAccountView
    bind(NewAccountView.class).to(NewAccountViewImpl.class).in(Singleton.class);

    bind(RegisterWidgetView.class).to(RegisterWidgetViewImpl.class);

    // ProfileView
    bind(ProfileView.class).to(ProfileViewImpl.class).in(Singleton.class);

    // CominSoonView
    bind(ComingSoonView.class).to(ComingSoonViewImpl.class).in(Singleton.class);

    // BCCOverviewView
    bind(ChallengeOverviewViewImpl.class).in(Singleton.class);

    bind(ChallengeOverviewView.class).to(ChallengeOverviewViewImpl.class);

    // Help
    bind(HelpView.class).to(HelpViewImpl.class).in(Singleton.class);
    // SearchView
    bind(SearchView.class).to(SearchViewImpl.class).in(Singleton.class);

    // Down
    bind(DownView.class).to(DownViewImpl.class).in(Singleton.class);

    // Synapse Wiki Pages
    bind(SynapseWikiView.class).to(SynapseWikiViewImpl.class);

    // Certificate
    bind(CertificateWidgetView.class).to(CertificateWidgetViewImpl.class);

    // Account
    bind(AccountView.class).to(AccountViewImpl.class).in(Singleton.class);

    // ChangeUsername
    bind(ChangeUsernameView.class)
      .to(ChangeUsernameViewImpl.class)
      .in(Singleton.class);

    // SignedToken
    bind(SignedTokenView.class)
      .to(SignedTokenViewImpl.class)
      .in(Singleton.class);

    // NrgrSynapseGlue
    bind(DataAccessApprovalTokenView.class)
      .to(DataAccessApprovalTokenViewImpl.class)
      .in(Singleton.class);

    // Trash
    bind(TrashView.class).to(TrashViewImpl.class).in(Singleton.class);

    // Asynchronous progress
    bind(TimerProvider.class).to(TimerProviderImpl.class);

    bind(NumberFormatProvider.class).to(NumberFormatProviderImpl.class);

    bind(AsynchronousProgressView.class).to(AsynchronousProgressViewImpl.class);
    bind(AsynchronousJobTracker.class).to(AsynchronousJobTrackerImpl.class);
    // EmailInvitation
    bind(EmailInvitationView.class)
      .to(EmailInvitationViewImpl.class)
      .in(Singleton.class);
    // DataAccessManagement
    bind(DataAccessManagementView.class)
      .to(DataAccessManagementViewImpl.class)
      .in(Singleton.class);

    // OAuthClientEditor
    bind(OAuthClientEditorView.class)
      .to(OAuthClientEditorViewImpl.class)
      .in(Singleton.class);

    // CertificationQuiz
    bind(CertificationQuizView.class)
      .to(CertificationQuizViewImpl.class)
      .in(Singleton.class);

    /*
     * Widgets
     */

    // DoiWidget
    bind(DoiWidgetV2View.class).to(DoiWidgetV2ViewImpl.class);

    bind(CreateOrUpdateDoiModalView.class)
      .to(CreateOrUpdateDoiModalViewImpl.class);

    // LoginWidget
    bind(LoginWidgetView.class)
      .to(LoginWidgetViewImpl.class)
      .in(Singleton.class);
    // Breadcrumb
    bind(BreadcrumbView.class).to(BreadcrumbViewImpl.class);

    // Bind the cookie provider
    bind(GWTCookieImpl.class).in(Singleton.class);

    bind(CookieProvider.class).to(GWTCookieImpl.class);

    // ACL Editor
    bind(AccessControlListEditorView.class)
      .to(AccessControlListEditorViewImpl.class);
    bind(AccessControlListModalWidget.class)
      .to(AccessControlListModalWidgetImpl.class);
    bind(EntityAccessControlListModalWidget.class)
      .to(EntityAccessControlListModalWidgetImpl.class);

    bind(AccessControlListModalWidgetView.class)
      .to(AccessControlListModalWidgetViewImpl.class);
    bind(EvaluationAccessControlListModalWidget.class)
      .to(EvaluationAccessControlListModalWidgetImpl.class);
    // Sharing Permissions Grid
    bind(SharingPermissionsGridView.class)
      .to(SharingPermissionsGridViewImpl.class);

    // basic pagination
    bind(BasicPaginationView.class).to(BasicPaginationViewImpl.class);

    // EntityPageTop
    bind(EntityPageTopView.class).to(EntityPageTopViewImpl.class);

    // Preview
    bind(PreviewWidgetView.class).to(PreviewWidgetViewImpl.class);

    // ActionMenu
    bind(EntityActionMenu.class).to(EntityActionMenuImpl.class);
    bind(EntityActionMenuView.class).to(EntityActionMenuViewImpl.class);

    bind(EntityActionController.class).to(EntityActionControllerImpl.class);
    bind(EntityActionControllerView.class)
      .to(EntityActionControllerViewImpl.class);

    bind(PreflightController.class).to(PreflightControllerImpl.class);
    bind(CertifiedUserController.class).to(CertifiedUserControllerImpl.class);
    bind(BigPromptModalView.class).to(BigPromptModalViewImpl.class);
    bind(PromptForValuesModalView.class).to(PromptForValuesModalViewImpl.class);

    bind(PromptForValuesModalView.Configuration.Builder.class)
      .to(PromptForValuesModalConfigurationImpl.Builder.class);
    bind(RenameEntityModalWidget.class).to(RenameEntityModalWidgetImpl.class);
    // Rejected Reason
    bind(RejectReasonView.class).to(RejectReasonViewImpl.class);

    bind(ProjectTitleBarView.class).to(ProjectTitleBarViewImpl.class);
    bind(BasicTitleBarView.class).to(BasicTitleBarViewImpl.class);

    // Search Box
    bind(SearchBoxView.class).to(SearchBoxViewImpl.class).in(Singleton.class);
    // Reject Data Access Request Dialog
    bind(RejectDataAccessRequestModalView.class)
      .to(RejectDataAccessRequestModalViewImpl.class);

    // User Suggest Box
    bind(SynapseSuggestBoxView.class).to(SynapseSuggestBoxViewImpl.class);

    bind(MultipartUploader.class).to(MultipartUploaderImplV2.class);

    bind(FileInputView.class).to(FileInputViewImpl.class);

    bind(FileHandleUploadView.class).to(FileHandleUploadViewImpl.class);
    bind(FileHandleUploadWidget.class).to(FileHandleUploadWidgetImpl.class);

    // LocationableUploader
    bind(UploaderView.class).to(UploaderViewImpl.class);
    bind(QuizInfoWidgetView.class).to(QuizInfoViewImpl.class);

    // EntityTreeBrowser
    bind(EntityTreeBrowserView.class).to(EntityTreeBrowserViewImpl.class);

    // MyEntitiesBrowser
    bind(MyEntitiesBrowserView.class).to(MyEntitiesBrowserViewImpl.class);

    // Wiki Attachments
    bind(WikiAttachmentsView.class).to(WikiAttachmentsViewImpl.class);

    bind(WikiHistoryWidgetView.class).to(WikiHistoryWidgetViewImpl.class);

    // Evaluation selector
    bind(EvaluationListView.class).to(EvaluationListViewImpl.class);

    // Administer Evaluations list
    bind(AdministerEvaluationsListView.class)
      .to(AdministerEvaluationsListViewImpl.class);

    // EntitySearchBox
    bind(EntitySearchBoxView.class).to(EntitySearchBoxViewImpl.class);

    // EntityMetadata
    bind(EntityMetadataView.class).to(EntityMetadataViewImpl.class);

    bind(UserProfileWidget.class).to(UserProfileWidgetImpl.class);

    bind(UserProfileWidgetView.class).to(UserProfileWidgetViewImpl.class);

    bind(ProfileImageView.class).to(ProfileImageViewImpl.class);
    bind(ProfileImageWidget.class).to(ProfileImageWidgetImpl.class);
    // API Table Column manager
    bind(APITableColumnManagerView.class)
      .to(APITableColumnManagerViewImpl.class);
    bind(APITableColumnConfigView.class).to(APITableColumnConfigViewImpl.class);

    // single subpages view
    bind(WikiSubpagesView.class).to(WikiSubpagesViewImpl.class);
    // SubPages Order Editor
    bind(WikiSubpagesOrderEditorView.class)
      .to(WikiSubpagesOrderEditorViewImpl.class);

    // SubPages Order Editor Tree
    bind(WikiSubpageOrderEditorTreeView.class)
      .to(WikiSubpageOrderEditorTreeViewImpl.class);

    // SubPages Navigation Tree
    bind(WikiSubpageNavigationTreeView.class)
      .to(WikiSubpageNavigationTreeViewImpl.class);

    // Widget Registration
    bind(WidgetRegistrar.class)
      .to(WidgetRegistrarImpl.class)
      .in(Singleton.class);

    // UI Widget Descriptor editor
    bind(BaseEditWidgetDescriptorView.class)
      .to(BaseEditWidgetDescriptorViewImpl.class);

    bind(ReferenceConfigView.class).to(ReferenceConfigViewImpl.class);

    bind(ImageConfigView.class)
      .to(ImageConfigViewImpl.class)
      .in(Singleton.class);
    bind(AttachmentConfigView.class)
      .to(AttachmentConfigViewImpl.class)
      .in(Singleton.class);
    bind(ProvenanceConfigView.class).to(ProvenanceConfigViewImpl.class);
    bind(LinkConfigView.class).to(LinkConfigViewImpl.class);
    bind(DetailsSummaryConfigView.class).to(DetailsSummaryConfigViewImpl.class);
    bind(TabbedTableConfigView.class).to(TabbedTableConfigViewImpl.class);
    bind(APITableConfigView.class).to(APITableConfigViewImpl.class);
    bind(QueryTableConfigView.class).to(QueryTableConfigViewImpl.class);
    bind(EntityListConfigView.class).to(EntityListConfigViewImpl.class);
    bind(ShinySiteConfigView.class).to(ShinySiteConfigViewImpl.class);
    bind(ButtonLinkConfigView.class).to(ButtonLinkConfigViewImpl.class);
    bind(EvaluationSubmissionConfigView.class)
      .to(EvaluationSubmissionConfigViewImpl.class);
    bind(VideoConfigView.class).to(VideoConfigViewImpl.class);
    bind(TableQueryResultWikiView.class).to(TableQueryResultWikiViewImpl.class);
    bind(TeamSelectEditorView.class).to(TeamSelectEditorViewImpl.class);
    // UI Widget Renderers
    bind(BookmarkWidgetView.class).to(BookmarkWidgetViewImpl.class);
    bind(ReferenceWidgetView.class).to(ReferenceWidgetViewImpl.class);

    bind(EntityListWidgetView.class).to(EntityListWidgetViewImpl.class);
    bind(IFrameView.class).to(IFrameViewImpl.class);
    bind(ImageWidgetView.class).to(ImageWidgetViewImpl.class);
    bind(AttachmentPreviewWidgetView.class)
      .to(AttachmentPreviewWidgetViewImpl.class);
    bind(APITableWidgetView.class).to(APITableWidgetViewImpl.class);
    bind(TableOfContentsWidgetView.class)
      .to(TableOfContentsWidgetViewImpl.class);
    bind(WikiFilesPreviewWidgetView.class)
      .to(WikiFilesPreviewWidgetViewImpl.class);
    bind(ButtonLinkWidgetView.class).to(ButtonLinkWidgetViewImpl.class);
    bind(EmptyWidgetView.class).to(EmptyWidgetViewImpl.class);
    bind(VideoWidgetView.class).to(VideoWidgetViewImpl.class);
    bind(TeamMemberCountView.class).to(TeamMemberCountViewImpl.class);
    bind(TIFFPreviewWidgetView.class).to(TIFFPreviewWidgetViewImpl.class);
    // ProvenanceWidget
    bind(
      org.sagebionetworks.web.client.widget.provenance.ProvenanceWidgetView.class
    )
      .to(
        org.sagebionetworks.web.client.widget.provenance.ProvenanceWidgetViewImpl.class
      );
    bind(ProvenanceWidgetView.class).to(ProvenanceWidgetViewImpl.class);

    // MarkdownWidget
    bind(MarkdownWidgetView.class).to(MarkdownWidgetViewImpl.class);
    // MarkdownEditorWidget
    bind(MarkdownEditorWidgetView.class).to(MarkdownEditorWidgetViewImpl.class);

    // FilesBrowser
    bind(FilesBrowserView.class).to(FilesBrowserViewImpl.class);

    // MoreTreeItem
    bind(MoreTreeItemView.class).to(MoreTreeItemViewImpl.class);

    bind(EvaluationSubmitterView.class).to(EvaluationSubmitterViewImpl.class);

    bind(FavoriteWidgetView.class).to(FavoriteWidgetViewImpl.class);

    bind(WikiPageWidgetView.class).to(WikiPageWidgetViewImpl.class);

    bind(UserBadgeView.class).to(UserBadgeViewImpl.class);

    bind(EmailInvitationBadgeView.class).to(EmailInvitationBadgeViewImpl.class);
    bind(EntityBadgeView.class).to(EntityBadgeViewImpl.class);
    bind(TutorialWizardView.class).to(TutorialWizardViewImpl.class);

    bind(PublicPrivateBadgeView.class).to(PublicPrivateBadgeViewImpl.class);

    /*
     * Modal wizard stuff.
     */
    bind(ModalWizardView.class).to(ModalWizardViewImpl.class);

    bind(ModalWizardWidget.class).to(ModalWizardWidgetImpl.class);

    /*
     * TableEntity related bindings
     */
    bind(TableListWidgetView.class).to(TableListWidgetViewImpl.class);

    bind(ColumnModelsView.class).to(ColumnModelsViewImpl.class);
    bind(ColumnModelTableRowEditorView.class)
      .to(ColumnModelTableRowEditorViewImpl.class);
    bind(ColumnModelTableRowEditorWidget.class)
      .to(ColumnModelTableRowEditorWidgetImpl.class);
    bind(ColumnModelTableRowViewer.class)
      .to(ColumnModelTableRowViewerImpl.class);
    bind(ColumnModelsEditorWidgetView.class)
      .to(ColumnModelsEditorWidgetViewImpl.class);
    bind(TableEntityWidgetView.class).to(TableEntityWidgetViewImpl.class);
    bind(RowView.class).to(RowViewImpl.class);
    bind(TablePageView.class).to(TablePageViewImpl.class);
    bind(QueryResultEditorView.class).to(QueryResultEditorViewImpl.class);
    bind(QueryInputView.class).to(QueryInputViewImpl.class);
    bind(JobTrackingWidget.class).to(AsynchronousProgressWidget.class);
    bind(UploadTableModalWidget.class).to(UploadTableModalWidgetImpl.class);
    bind(UploadCSVPreviewPage.class).to(UploadCSVPreviewPageImpl.class);
    bind(CSVOptionsView.class).to(CSVOptionsViewImpl.class);
    bind(UploadCSVPreviewPageView.class).to(UploadCSVPreviewPageViewImpl.class);
    bind(UploadPreviewView.class).to(UploadPreviewViewImpl.class);
    bind(UploadPreviewWidget.class).to(UploadPreviewWidgetImpl.class);
    bind(UploadCSVFilePage.class).to(UploadCSVFilePageImpl.class);
    bind(UploadCSVFinishPage.class).to(UploadCSVFinishPageImpl.class);
    bind(UploadCSVFinishPageView.class).to(UploadCSVFinishPageViewImpl.class);
    bind(UploadCSVAppendPage.class).to(UploadCSVAppendPageImpl.class);
    bind(UploadCSVAppendPageView.class).to(UploadCSVAppendPageViewImpl.class);
    bind(SortableTableHeader.class).to(SortableTableHeaderImpl.class);
    bind(StaticTableHeader.class).to(StaticTableHeaderImpl.class);
    bind(TotalVisibleResultsWidgetView.class)
      .to(TotalVisibleResultsWidgetViewImpl.class);
    bind(CreateDownloadPage.class).to(CreateDownloadPageImpl.class);
    bind(CreateDownloadPageView.class).to(CreateDownloadPageViewImpl.class);

    bind(DownloadFilePage.class).to(DownloadFilePageImpl.class);
    bind(DownloadFilePageView.class).to(DownloadFilePageViewImpl.class);

    bind(DownloadTableQueryModalWidget.class)
      .to(DownloadTableQueryModalWidgetImpl.class);
    /*
     * TableEntity cell bindings.
     */
    bind(LinkCellRendererView.class).to(LinkCellRendererViewImpl.class);
    bind(StringRendererCellView.class).to(StringRendererCellViewImpl.class);

    bind(StringListRendererCellView.class)
      .to(StringListRendererCellViewImpl.class);
    bind(DateListRendererCellView.class).to(DateListRendererCellViewImpl.class);
    bind(UserIdListRendererCellView.class)
      .to(UserIdListRendererCellViewImpl.class);
    bind(EntityIdListRendererCellView.class)
      .to(EntityIdListRendererCellViewImpl.class);
    bind(CellEditorView.class).to(CellEditorViewImpl.class);
    bind(NumberCellEditorView.class).to(NumberCellEditorViewImpl.class);
    bind(ListCellEditorView.class).to(ListCellEditorViewImpl.class);
    bind(DateCellEditorView.class).to(DateCellEditorViewImpl.class);
    bind(UserIdCellEditorView.class).to(UserIdCellEditorViewImpl.class);
    bind(FileCellEditorView.class).to(FileCellEditorViewImpl.class);
    bind(FileCellRendererView.class).to(FileCellRendererViewImpl.class);
    bind(EntityIdCellRendererView.class).to(EntityIdCellRendererViewImpl.class);
    bind(LargeStringCellEditorView.class)
      .to(LargeStringCellEditorViewImpl.class);
    bind(JSONListCellEditorView.class).to(JSONListCellEditorViewImpl.class);
    bind(EditJSONListModalView.class)
      .to(EditJSONListModalViewImpl.class)
      .in(Singleton.class);
    /*
     * Teams Places
     */
    // Team Page
    bind(TeamView.class).to(TeamViewImpl.class).in(Singleton.class);
    // Team Search Page
    bind(TeamSearchView.class).to(TeamSearchViewImpl.class).in(Singleton.class);

    bind(MapView.class).to(MapViewImpl.class);

    // People Search Page
    bind(PeopleSearchView.class)
      .to(PeopleSearchViewImpl.class)
      .in(Singleton.class);

    /*
     * Teams Widgets
     */

    // Team Action Menu Items
    bind(TeamEditModalWidgetView.class).to(TeamEditModalWidgetViewImpl.class);

    bind(TeamLeaveModalWidgetView.class).to(TeamLeaveModalWidgetViewImpl.class);

    bind(TeamDeleteModalWidgetView.class)
      .to(TeamDeleteModalWidgetViewImpl.class);
    // Open Team Invitations widget
    bind(OpenTeamInvitationsWidgetView.class)
      .to(OpenTeamInvitationsWidgetViewImpl.class);
    // Pending Team Join Requests widget
    bind(OpenMembershipRequestsWidgetView.class)
      .to(OpenMembershipRequestsWidgetViewImpl.class);

    // Current User Invites widget
    bind(OpenUserInvitationsWidgetView.class)
      .to(OpenUserInvitationsWidgetViewImpl.class);

    // Team List widget (link to search teams page, optionally can create team)
    bind(TeamListWidgetView.class).to(TeamListWidgetViewImpl.class);

    // Member List widget
    bind(MemberListWidgetView.class).to(MemberListWidgetViewImpl.class);

    // Invite Team member widget
    bind(InviteWidgetView.class).to(InviteWidgetViewImpl.class);

    // Request Team membership widget
    bind(JoinTeamWidgetView.class).to(JoinTeamWidgetViewImpl.class);

    // Join Team Button Config widget
    bind(JoinTeamConfigEditorView.class).to(JoinTeamConfigEditorViewImpl.class);

    // Submit to evaluation widget
    bind(SubmitToEvaluationWidgetView.class)
      .to(SubmitToEvaluationWidgetViewImpl.class);

    // Team renderer
    bind(TeamBadgeView.class).to(TeamBadgeViewImpl.class);

    bind(BigTeamBadgeView.class).to(BigTeamBadgeViewImpl.class);
    bind(UserTeamConfigView.class).to(UserTeamConfigViewImpl.class);
    bind(SharingAndDataUseConditionWidgetView.class)
      .to(SharingAndDataUseConditionWidgetViewImpl.class);

    bind(WizardProgressWidgetView.class).to(WizardProgressWidgetViewImpl.class);

    bind(UploadDialogWidgetView.class).to(UploadDialogWidgetViewImpl.class);

    bind(AddFolderDialogWidgetView.class)
      .to(AddFolderDialogWidgetViewImpl.class);
    bind(LoginModalView.class).to(LoginModalViewImpl.class);
    bind(ImageParamsPanelView.class).to(ImageParamsPanelViewImpl.class);

    bind(RegisterTeamDialogView.class).to(RegisterTeamDialogViewImpl.class);

    bind(EditRegisteredTeamDialogView.class)
      .to(EditRegisteredTeamDialogViewImpl.class);
    bind(ChallengeTeamsView.class).to(ChallengeTeamsViewImpl.class);
    bind(ChallengeBadgeView.class).to(ChallengeBadgeViewImpl.class);
    bind(ProjectBadgeView.class).to(ProjectBadgeViewImpl.class);
    bind(TableQueryResultWikiWidgetView.class)
      .to(TableQueryResultWikiWidgetViewImpl.class);
    bind(SingleButtonView.class).to(SingleButtonViewImpl.class);
    bind(AnnotationTransformer.class)
      .to(AnnotationTransformerImpl.class)
      .in(Singleton.class);

    bind(AnnotationEditorView.class).to(AnnotationEditorViewImpl.class);

    bind(EditAnnotationsDialogView.class)
      .to(EditAnnotationsDialogViewImpl.class);
    bind(CommaSeparatedValuesParserView.class)
      .to(CommaSeparatedValuesParserViewImpl.class);
    bind(PapaCSVParser.class).in(Singleton.class);
    bind(AnnotationCellFactory.class)
      .to(AnnotationCellFactoryImpl.class)
      .in(Singleton.class);
    bind(EntityId2BundleCache.class)
      .to(EntityId2BundleCacheImpl.class)
      .in(Singleton.class);

    bind(VersionHistoryRowView.class).to(VersionHistoryRowViewImpl.class);
    bind(SynapseStandaloneWikiView.class)
      .to(SynapseStandaloneWikiViewImpl.class);

    bind(SynapseAlertView.class).to(SynapseAlertViewImpl.class);
    bind(SynapseAlert.class).to(SynapseAlertImpl.class);

    bind(ProvenanceEditorWidgetView.class)
      .to(ProvenanceEditorWidgetViewImpl.class);
    bind(ProvenanceListWidgetView.class).to(ProvenanceListWidgetViewImpl.class);

    bind(ProvenanceURLDialogWidgetView.class)
      .to(ProvenanceURLDialogWidgetViewImpl.class);
    bind(EntityRefProvEntryView.class).to(EntityRefProvEntryViewImpl.class);
    bind(URLProvEntryView.class).to(URLProvEntryViewImpl.class);
    bind(StorageLocationWidgetView.class)
      .to(StorageLocationWidgetViewImpl.class);
    bind(ErrorView.class).to(ErrorViewImpl.class);
    bind(PreviewConfigView.class).to(PreviewConfigViewImpl.class);
    bind(SynapseFormConfigView.class).to(SynapseFormConfigViewImpl.class);
    bind(DownloadCartPageView.class).to(DownloadCartPageViewImpl.class);
    bind(EditFileMetadataModalView.class)
      .to(EditFileMetadataModalViewImpl.class);
    bind(EditFileMetadataModalWidget.class)
      .to(EditFileMetadataModalWidgetImpl.class);

    bind(EditProjectMetadataModalView.class)
      .to(EditProjectMetadataModalViewImpl.class);
    bind(EditProjectMetadataModalWidget.class)
      .to(EditProjectMetadataModalWidgetImpl.class);
    bind(BiodallianceWidgetView.class).to(BiodallianceWidgetViewImpl.class);
    bind(BiodallianceSourceEditorView.class)
      .to(BiodallianceSourceEditorViewImpl.class);
    bind(BiodallianceEditorView.class).to(BiodallianceEditorViewImpl.class);
    bind(TabView.class).to(TabViewImpl.class);
    bind(TabsView.class).to(TabsViewImpl.class);

    bind(FilesTabView.class).to(FilesTabViewImpl.class);
    bind(TablesTabView.class).to(TablesTabViewImpl.class);

    bind(ChallengeTabView.class).to(ChallengeTabViewImpl.class);
    bind(DiscussionTabView.class).to(DiscussionTabViewImpl.class);
    bind(DockerTabView.class).to(DockerTabViewImpl.class);
    bind(ModifiedCreatedByWidgetView.class)
      .to(ModifiedCreatedByWidgetViewImpl.class);
    bind(FileHandleListView.class).to(FileHandleListViewImpl.class);
    bind(ACTView.class).to(ACTViewImpl.class);
    bind(CytoscapeConfigView.class).to(CytoscapeConfigViewImpl.class);
    bind(CytoscapeView.class).to(CytoscapeViewImpl.class);
    // discussion
    bind(DiscussionThreadModalView.class)
      .to(DiscussionThreadModalViewImpl.class);
    bind(ReplyModalView.class).to(ReplyModalViewImpl.class);

    bind(DiscussionThreadListWidgetView.class)
      .to(DiscussionThreadListWidgetViewImpl.class);
    bind(DiscussionThreadListItemWidgetView.class)
      .to(DiscussionThreadListItemWidgetViewImpl.class);
    bind(SingleDiscussionThreadWidgetView.class)
      .to(SingleDiscussionThreadWidgetViewImpl.class);
    bind(ReplyWidgetView.class).to(ReplyWidgetViewImpl.class);
    bind(ForumWidgetView.class).to(ForumWidgetViewImpl.class);
    bind(NewReplyWidgetView.class).to(NewReplyWidgetViewImpl.class);
    // docker
    bind(DockerRepoListWidgetView.class).to(DockerRepoListWidgetViewImpl.class);
    bind(DockerRepoWidgetView.class).to(DockerRepoWidgetViewImpl.class);

    bind(AddExternalRepoModalView.class).to(AddExternalRepoModalViewImpl.class);
    bind(DockerCommitRowWidgetView.class)
      .to(DockerCommitRowWidgetViewImpl.class);
    bind(DockerCommitListWidgetView.class)
      .to(DockerCommitListWidgetViewImpl.class);
    bind(SessionStorage.class).to(SessionStorageImpl.class);
    bind(SynapseForumView.class).to(SynapseForumViewImpl.class);

    bind(WikiMarkdownEditorView.class).to(WikiMarkdownEditorViewImpl.class);
    bind(StuAlertView.class).to(StuAlertViewImpl.class);
    bind(SynapseTableFormWidgetView.class)
      .to(SynapseTableFormWidgetViewImpl.class);
    bind(RowFormView.class).to(RowFormViewImpl.class);

    bind(RadioCellEditorView.class).to(RadioCellEditorViewImpl.class);
    bind(MarkdownIt.class).to(MarkdownItImpl.class);
    bind(SubscriptionView.class).to(SubscriptionViewImpl.class);

    bind(TopicWidgetView.class).to(TopicWidgetViewImpl.class);
    bind(SubscribeButtonWidgetView.class)
      .to(SubscribeButtonWidgetViewImpl.class);

    bind(RefreshAlertView.class).to(RefreshAlertViewImpl.class);
    bind(UserSelectorView.class).to(UserSelectorViewImpl.class);

    bind(EntityContainerListWidgetView.class)
      .to(EntityContainerListWidgetViewImpl.class);
    bind(EntityViewScopeWidgetView.class)
      .to(EntityViewScopeWidgetViewImpl.class);
    bind(CopyTextModal.class).to(CopyTextModalImpl.class);
    bind(EvaluationEditorModalView.class)
      .to(EvaluationEditorModalViewImpl.class);
    bind(LoadMoreWidgetContainerView.class)
      .to(LoadMoreWidgetContainerViewImpl.class);

    bind(RadioWidget.class).to(RadioWidgetViewImpl.class);
    bind(FileClientsHelpView.class).to(FileClientsHelpViewImpl.class);
    bind(ContainerClientsHelp.class).to(ContainerClientsHelpImpl.class);

    bind(FileDownloadMenuItemView.class).to(FileDownloadMenuItemViewImpl.class);
    bind(SqlDefinedEditorModalWidgetView.class)
      .to(SqlDefinedEditorModalWidgetViewImpl.class);
    bind(EntityViewScopeEditorModalWidgetView.class)
      .to(EntityViewScopeEditorModalWidgetViewImpl.class);
    bind(SubmissionViewScopeEditorModalWidgetView.class)
      .to(SubmissionViewScopeEditorModalWidgetViewImpl.class);
    bind(EntityModalWidgetView.class).to(EntityModalWidgetViewImpl.class);
    bind(ChallengeWidgetView.class).to(ChallengeWidgetViewImpl.class);
    bind(SelectTeamModalView.class).to(SelectTeamModalViewImpl.class);
    bind(ApproveUserAccessModalView.class)
      .to(ApproveUserAccessModalViewImpl.class);
    bind(UserBadgeListView.class).to(UserBadgeListViewImpl.class);
    bind(EntityListRowBadgeView.class).to(EntityListRowBadgeViewImpl.class);
    bind(LazyLoadWikiWidgetWrapperView.class)
      .to(LazyLoadWikiWidgetWrapperViewImpl.class);
    bind(EntityHeaderAsyncHandler.class)
      .to(EntityHeaderAsyncHandlerImpl.class)
      .in(Singleton.class);

    bind(VersionedEntityHeaderAsyncHandler.class)
      .to(VersionedEntityHeaderAsyncHandlerImpl.class)
      .in(Singleton.class);

    bind(GoogleMapView.class).to(GoogleMapViewImpl.class);
    bind(FileHandleAsyncHandler.class)
      .to(FileHandleAsyncHandlerImpl.class)
      .in(Singleton.class);

    bind(PresignedURLAsyncHandler.class)
      .to(PresignedURLAsyncHandlerImpl.class)
      .in(Singleton.class);

    bind(PresignedAndFileHandleURLAsyncHandler.class)
      .to(PresignedAndFileHandleURLAsyncHandlerImpl.class)
      .in(Singleton.class);
    bind(UserProfileAsyncHandler.class)
      .to(UserProfileAsyncHandlerImpl.class)
      .in(Singleton.class);
    bind(TeamAsyncHandler.class)
      .to(TeamAsyncHandlerImpl.class)
      .in(Singleton.class);

    bind(UserGroupHeaderAsyncHandler.class)
      .to(UserGroupHeaderAsyncHandlerImpl.class)
      .in(Singleton.class);

    bind(UserGroupHeaderFromAliasAsyncHandler.class)
      .to(UserGroupHeaderFromAliasAsyncHandlerImpl.class)
      .in(Singleton.class);

    bind(DivView.class).to(DivViewImpl.class);

    bind(ViewDefaultColumns.class).in(Singleton.class);
    bind(SubscribersWidgetView.class).to(SubscribersWidgetViewImpl.class);

    bind(PlaceView.class).to(PlaceViewImpl.class);
    bind(ManagedACTAccessRequirementWidgetView.class)
      .to(ManagedACTAccessRequirementWidgetViewImpl.class);
    bind(ACTAccessRequirementWidgetView.class)
      .to(ACTAccessRequirementWidgetViewImpl.class);
    bind(TermsOfUseAccessRequirementWidgetView.class)
      .to(TermsOfUseAccessRequirementWidgetViewImpl.class);
    bind(FileHandleWidgetView.class).to(FileHandleWidgetViewImpl.class);
    bind(CreateAccessRequirementStep1View.class)
      .to(CreateAccessRequirementStep1ViewImpl.class);
    bind(CreateManagedACTAccessRequirementStep2View.class)
      .to(CreateManagedACTAccessRequirementStep2ViewImpl.class);
    bind(CreateBasicAccessRequirementStep2View.class)
      .to(CreateBasicAccessRequirementStep2ViewImpl.class);
    bind(CreateManagedACTAccessRequirementStep3View.class)
      .to(CreateManagedACTAccessRequirementStep3ViewImpl.class);
    bind(Button.class).to(ButtonImpl.class);
    bind(IsACTMemberAsyncHandler.class)
      .to(IsACTMemberAsyncHandlerImpl.class)
      .in(Singleton.class);
    bind(PopupUtilsView.class).to(PopupUtilsViewImpl.class).in(Singleton.class);

    bind(ProfileCertifiedValidatedView.class)
      .to(ProfileCertifiedValidatedViewImpl.class);

    bind(ACTDataAccessSubmissionsView.class)
      .to(ACTDataAccessSubmissionsViewImpl.class);
    bind(RestrictionWidgetView.class).to(RestrictionWidgetViewImpl.class);
    bind(ACTDataAccessSubmissionWidgetView.class)
      .to(ACTDataAccessSubmissionWidgetViewImpl.class);
    bind(OpenSubmissionWidgetView.class).to(OpenSubmissionWidgetViewImpl.class);
    bind(LockAccessRequirementWidgetView.class)
      .to(LockAccessRequirementWidgetViewImpl.class);
    bind(ImageUploadView.class).to(ImageUploadViewImpl.class);
    bind(RevokeUserAccessModalView.class)
      .to(RevokeUserAccessModalViewImpl.class);
    bind(PlotlyWidgetView.class).to(PlotlyWidgetViewImpl.class);
    bind(PlotlyConfigView.class).to(PlotlyConfigViewImpl.class);
    bind(DateTimeUtils.class).to(DateTimeUtilsImpl.class).in(Singleton.class);
    bind(ACTAccessApprovalsView.class).to(ACTAccessApprovalsViewImpl.class);

    bind(AccessorGroupView.class).to(AccessorGroupViewImpl.class);
    bind(SelfSignAccessRequirementWidgetView.class)
      .to(SelfSignAccessRequirementWidgetViewImpl.class);
    bind(TeamSubjectWidgetView.class).to(TeamSubjectWidgetViewImpl.class);
    bind(EntitySubjectsWidgetView.class).to(EntitySubjectsWidgetViewImpl.class);
    bind(AwsLoginView.class).to(AwsLoginViewImpl.class);
    bind(UserListRowWidgetView.class).to(UserListRowWidgetViewImpl.class);
    bind(UserListView.class).to(UserListViewImpl.class);
    bind(FileViewClientsHelp.class).to(FileViewClientsHelpImpl.class);
    bind(EmailAddressesWidgetView.class).to(EmailAddressesWidgetViewImpl.class);
    // Synapse js client
    bind(SynapseJavascriptClient.class).in(Singleton.class);
    bind(SynapseJavascriptFactory.class).in(Singleton.class);

    bind(HtmlPreviewView.class).to(HtmlPreviewViewImpl.class);
    bind(NbConvertPreviewView.class).to(NbConvertPreviewViewImpl.class);
    bind(S3DirectLoginDialog.class).to(S3DirectLoginDialogImpl.class);

    bind(WikiPageDeleteConfirmationDialogView.class)
      .to(WikiPageDeleteConfirmationDialogViewImpl.class);
    bind(WikiDiffView.class).to(WikiDiffViewImpl.class);
    bind(SynapseProperties.class)
      .to(SynapsePropertiesImpl.class)
      .in(Singleton.class);
    bind(Moment.class).to(MomentImpl.class);
    bind(DownloadSpeedTester.class).to(DownloadSpeedTesterImpl.class);
    bind(PackageSizeSummaryView.class).to(PackageSizeSummaryViewImpl.class);
    bind(EntityPresenterEventBinder.class)
      .to(EntityPresenterEventBinderImpl.class);
    bind(Linkify.class).to(LinkifyImpl.class);
    bind(PasswordResetSignedTokenView.class)
      .to(PasswordResetSignedTokenViewImpl.class);
    bind(TeamProjectsModalWidgetView.class)
      .to(TeamProjectsModalWidgetViewImpl.class);
    bind(ContainerItemCountWidgetView.class)
      .to(ContainerItemCountWidgetViewImpl.class);
    bind(StatisticsPlotWidgetView.class).to(StatisticsPlotWidgetViewImpl.class);
    bind(QuarantinedEmailModal.class).in(Singleton.class);
    bind(EvaluationFinderView.class).to(EvaluationFinderViewImpl.class);
    bind(SubmissionViewScopeWidgetView.class)
      .to(SubmissionViewScopeWidgetViewImpl.class);
    bind(PageProgressWidgetView.class).to(PageProgressWidgetViewImpl.class);
    bind(EntityFinderWidget.class).to(EntityFinderWidgetImpl.class);
    bind(EntityFinderWidget.Builder.class)
      .to(EntityFinderWidgetImpl.Builder.class);

    bind(EntityFinderWidgetView.class).to(EntityFinderWidgetViewImpl.class);
    bind(SynapseReactClientFullContextPropsProvider.class)
      .to(SynapseReactClientFullContextPropsProviderImpl.class);

    bind(AddToDownloadListV2.class).to(AddToDownloadListV2Impl.class);
    bind(OpenDataView.class).to(OpenDataViewImpl.class);
    bind(QueryClientProvider.class)
      .to(QueryClientProviderImpl.class)
      .in(Singleton.class);
    bind(IntendedDataUseReportWidgetView.class)
      .to(IntendedDataUseReportWidgetViewImpl.class);
    bind(DialogView.class).to(Dialog.class);

    bind(FollowingPageView.class)
      .to(FollowingPageViewImpl.class)
      .in(Singleton.class);

    bind(KeyFactoryProvider.class).to(KeyFactoryProviderImpl.class);
    bind(SRCUploadFileWrapper.class).to(SRCUploadFileWrapperImpl.class);

    bind(FeatureFlagConfig.class).in(Singleton.class);
    bind(EntityTypeIcon.class).to(EntityTypeIconImpl.class);
  }
}
