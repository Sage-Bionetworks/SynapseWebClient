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
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.cookie.GWTCookieImpl;
import org.sagebionetworks.web.client.presenter.DownPresenter;
import org.sagebionetworks.web.client.presenter.EntityPresenter;
import org.sagebionetworks.web.client.presenter.ProfilePresenter;
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
import org.sagebionetworks.web.client.widget.accessrequirements.requestaccess.CreateDataAccessSubmissionWizardStep2View;
import org.sagebionetworks.web.client.widget.accessrequirements.requestaccess.CreateDataAccessSubmissionWizardStep2ViewImpl;
import org.sagebionetworks.web.client.widget.accessrequirements.requestaccess.CreateResearchProjectWizardStep1View;
import org.sagebionetworks.web.client.widget.accessrequirements.requestaccess.CreateResearchProjectWizardStep1ViewImpl;
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
import org.sagebionetworks.web.client.widget.discussion.*;
import org.sagebionetworks.web.client.widget.discussion.modal.DiscussionThreadModalView;
import org.sagebionetworks.web.client.widget.discussion.modal.DiscussionThreadModalViewImpl;
import org.sagebionetworks.web.client.widget.discussion.modal.ReplyModalView;
import org.sagebionetworks.web.client.widget.discussion.modal.ReplyModalViewImpl;
import org.sagebionetworks.web.client.widget.docker.*;
import org.sagebionetworks.web.client.widget.docker.modal.AddExternalRepoModalView;
import org.sagebionetworks.web.client.widget.docker.modal.AddExternalRepoModalViewImpl;
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
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidgetImpl;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidgetView;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidgetViewImpl;
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
import org.sagebionetworks.web.client.widget.header.*;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadWikiWidgetWrapperView;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadWikiWidgetWrapperViewImpl;
import org.sagebionetworks.web.client.widget.login.*;
import org.sagebionetworks.web.client.widget.pagination.BasicPaginationView;
import org.sagebionetworks.web.client.widget.pagination.BasicPaginationViewImpl;
import org.sagebionetworks.web.client.widget.profile.*;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidgetView;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidgetViewImpl;
import org.sagebionetworks.web.client.widget.refresh.RefreshAlertView;
import org.sagebionetworks.web.client.widget.refresh.RefreshAlertViewImpl;
import org.sagebionetworks.web.client.widget.search.SearchBoxView;
import org.sagebionetworks.web.client.widget.search.SearchBoxViewImpl;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBoxView;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBoxViewImpl;
import org.sagebionetworks.web.client.widget.sharing.*;
import org.sagebionetworks.web.client.widget.subscription.*;
import org.sagebionetworks.web.client.widget.table.*;
import org.sagebionetworks.web.client.widget.table.modal.download.*;
import org.sagebionetworks.web.client.widget.table.modal.fileview.*;
import org.sagebionetworks.web.client.widget.table.modal.upload.*;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardView;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardViewImpl;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidgetImpl;
import org.sagebionetworks.web.client.widget.table.v2.QueryInputView;
import org.sagebionetworks.web.client.widget.table.v2.QueryInputViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.TableEntityWidgetView;
import org.sagebionetworks.web.client.widget.table.v2.TableEntityWidgetViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.*;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.*;
import org.sagebionetworks.web.client.widget.table.v2.results.facets.*;
import org.sagebionetworks.web.client.widget.table.v2.schema.*;
import org.sagebionetworks.web.client.widget.team.*;
import org.sagebionetworks.web.client.widget.team.controller.*;
import org.sagebionetworks.web.client.widget.upload.*;
import org.sagebionetworks.web.client.widget.user.UserBadgeView;
import org.sagebionetworks.web.client.widget.user.UserBadgeViewImpl;
import org.sagebionetworks.web.client.widget.user.UserGroupListWidgetView;
import org.sagebionetworks.web.client.widget.user.UserGroupListWidgetViewImpl;

public class PortalGinModule extends AbstractGinModule {

	@Override
	protected void configure() {
		// Event Bus
		bind(EventBus.class).to(SimpleEventBus.class).in(Singleton.class);
		
		// JsoProvider
		bind(JsoProvider.class).to(JsoProviderImpl.class);
		bind(JsoProviderImpl.class).in(Singleton.class);
		
		// AuthenticationController
		bind(AuthenticationControllerImpl.class).in(Singleton.class);
		bind(AuthenticationController.class).to(AuthenticationControllerImpl.class);

		// GlobalApplicationState
		bind(GlobalApplicationStateImpl.class).in(Singleton.class);
		bind(GlobalApplicationState.class).to(GlobalApplicationStateImpl.class);
		bind(GlobalApplicationStateView.class).to(GlobalApplicationStateViewImpl.class);
		
		bind(ResourceLoaderImpl.class).in(Singleton.class);
		bind(ResourceLoader.class).to(ResourceLoaderImpl.class);
		
		// Header & Footer
		bind(Header.class).in(Singleton.class);
		bind(HeaderViewImpl.class).in(Singleton.class);
		bind(HeaderView.class).to(HeaderViewImpl.class);
		bind(Footer.class).in(Singleton.class);
		bind(FooterViewImpl.class).in(Singleton.class);
		bind(FooterView.class).to(FooterViewImpl.class);
		
		// JSONAdapters
		bind(JSONObjectAdapter.class).to(JSONObjectGwt.class);
		bind(JSONArrayAdapter.class).to(JSONArrayGwt.class);
		
		// EntitySchemaCacheImpl
		bind(EntitySchemaCacheImpl.class).in(Singleton.class);
		bind(EntitySchemaCache.class).to(EntitySchemaCacheImpl.class);
		
		// cache place presenters
		bind(ProfilePresenter.class).in(Singleton.class);
		bind(EntityPresenter.class).in(Singleton.class);
		bind(DownPresenter.class).in(Singleton.class);
		
		bind(AnnotationsRendererWidgetView.class).to(AnnotationsRendererWidgetViewImpl.class);
		
		//file history
		bind(FileHistoryWidget.class).in(Singleton.class);
		bind(FileHistoryWidgetViewImpl.class).in(Singleton.class);
		bind(FileHistoryWidgetView.class).to(FileHistoryWidgetViewImpl.class);
		
		// the logger
		bind(ClientLoggerImpl.class).in(Singleton.class);
		bind(ClientLogger.class).to(ClientLoggerImpl.class);
				
		// The URL cache
		bind(UrlCacheImpl.class).in(Singleton.class);
		bind(UrlCache.class).to(UrlCacheImpl.class);
		
		//GWT utility methods
		bind(GWTWrapperImpl.class).in(Singleton.class);
		bind(GWTWrapper.class).to(GWTWrapperImpl.class);
		bind(GWTTimer.class).to(GWTTimerImpl.class);
		
		//RequestBuilder
		bind(RequestBuilderWrapperImpl.class).in(Singleton.class);
		bind(RequestBuilderWrapper.class).to(RequestBuilderWrapperImpl.class);
		
		// Adapter factoyr
		bind(AdapterFactory.class).to(GwtAdapterFactory.class);
		
		bind(JiraURLHelper.class).to(JiraURLHelperImpl.class);
		
		// ClientCache
		bind(ClientCacheImpl.class).in(Singleton.class);
		bind(ClientCache.class).to(ClientCacheImpl.class);

		// Storage wrapper
		bind(StorageImpl.class).in(Singleton.class);
		bind(StorageWrapper.class).to(StorageImpl.class);
		
		/*
		 * Vanilla Implementation binding
		 */
		
		// JSNI impls
		bind(SynapseJSNIUtilsImpl.class).in(Singleton.class);
		bind(SynapseJSNIUtils.class).to(SynapseJSNIUtilsImpl.class);
		
		/*
		 * Places
		 */
		
		// The home page
		bind(HomeViewImpl.class).in(Singleton.class);
		bind(HomeView.class).to(HomeViewImpl.class);

		// EntityView
		bind(EntityViewImpl.class).in(Singleton.class);
		bind(EntityView.class).to(EntityViewImpl.class);
		
		// ProjectsHomeView
		bind(ProjectsHomeViewImpl.class).in(Singleton.class);
		bind(ProjectsHomeView.class).to(ProjectsHomeViewImpl.class);		
		
		// LoginView
		bind(LoginViewImpl.class).in(Singleton.class);
		bind(LoginView.class).to(LoginViewImpl.class);
		
		// PasswordResetView
		bind(PasswordResetViewImpl.class).in(Singleton.class);
		bind(PasswordResetView.class).to(PasswordResetViewImpl.class);
		
		// NewAccountView
		bind(NewAccountViewImpl.class).in(Singleton.class);
		bind(NewAccountView.class).to(NewAccountViewImpl.class);

		// RegisterAccountView
		bind(RegisterAccountViewImpl.class).in(Singleton.class);
		bind(RegisterAccountView.class).to(RegisterAccountViewImpl.class);

		bind(RegisterWidgetView.class).to(RegisterWidgetViewImpl.class);

		// ProfileView
		bind(ProfileViewImpl.class).in(Singleton.class);
		bind(ProfileView.class).to(ProfileViewImpl.class);		
		
		// SettingsView
		bind(SettingsViewImpl.class).in(Singleton.class);
		bind(SettingsView.class).to(SettingsViewImpl.class);	
		
		// CominSoonView
		bind(ComingSoonViewImpl.class).in(Singleton.class);
		bind(ComingSoonView.class).to(ComingSoonViewImpl.class);									
		
		// BCCOverviewView
		bind(ChallengeOverviewViewImpl.class).in(Singleton.class);
		bind(ChallengeOverviewView.class).to(ChallengeOverviewViewImpl.class);	
		
		//Help
		bind(HelpViewImpl.class).in(Singleton.class);
		bind(HelpView.class).to(HelpViewImpl.class);	
		
		// SearchView
		bind(SearchViewImpl.class).in(Singleton.class);
		bind(SearchView.class).to(SearchViewImpl.class);

		// Down
		bind(DownViewImpl.class).in(Singleton.class);
		bind(DownView.class).to(DownViewImpl.class);

		//Synapse Wiki Pages
		bind(SynapseWikiView.class).to(SynapseWikiViewImpl.class);
		
		// QuizView
		bind(QuizViewImpl.class).in(Singleton.class);
		bind(QuizView.class).to(QuizViewImpl.class);
		
		//Certificate place
		bind(CertificateView.class).to(CertificateViewImpl.class);
		
		// Certificate
		bind(CertificateWidgetView.class).to(CertificateWidgetViewImpl.class);		
		
		//Account
		bind(AccountViewImpl.class).in(Singleton.class);
		bind(AccountView.class).to(AccountViewImpl.class);
		
		//ChangeUsername
		bind(ChangeUsernameViewImpl.class).in(Singleton.class);
		bind(ChangeUsernameView.class).to(ChangeUsernameViewImpl.class);
		
		//SignedToken
		bind(SignedTokenViewImpl.class).in(Singleton.class);
		bind(SignedTokenView.class).to(SignedTokenViewImpl.class);
		
		// Trash
		bind(TrashViewImpl.class).in(Singleton.class);
		bind(TrashView.class).to(TrashViewImpl.class);
		
		// Asynchronous progress
		bind(TimerProvider.class).to(TimerProviderImpl.class);
		bind(NumberFormatProvider.class).to(NumberFormatProviderImpl.class);
		bind(AsynchronousProgressView.class).to(AsynchronousProgressViewImpl.class);
		bind(AsynchronousJobTracker.class).to(AsynchronousJobTrackerImpl.class);

		// EmailInvitation
		bind(EmailInvitationViewImpl.class).in(Singleton.class);
		bind(EmailInvitationView.class).to(EmailInvitationViewImpl.class);

		/*
		 * Widgets
		 */
		
		// QuestionContainerWidget
		bind(QuestionContainerWidgetView.class).to(QuestionContainerWidgetViewImpl.class);
		
		// DoiWidget
		bind(DoiWidgetView.class).to(DoiWidgetViewImpl.class);
		
		// LoginWidget
		bind(LoginWidgetViewImpl.class).in(Singleton.class);
		bind(LoginWidgetView.class).to(LoginWidgetViewImpl.class);
		
		// Breadcrumb
		bind(BreadcrumbView.class).to(BreadcrumbViewImpl.class);
		
		// Bind the cookie provider
		bind(GWTCookieImpl.class).in(Singleton.class);
		bind(CookieProvider.class).to(GWTCookieImpl.class);
		
		// ACL Editor
		bind(AccessControlListEditorView.class).to(AccessControlListEditorViewImpl.class);
		bind(AccessControlListModalWidget.class).to(AccessControlListModalWidgetImpl.class);
		bind(AccessControlListModalWidgetView.class).to(AccessControlListModalWidgetViewImpl.class);
		
		bind(EvaluationAccessControlListModalWidget.class).to(EvaluationAccessControlListModalWidgetImpl.class);
		
		// Sharing Permissions Grid
		bind(SharingPermissionsGridView.class).to(SharingPermissionsGridViewImpl.class);
		
		
		// basic pagination
		bind(BasicPaginationView.class).to(BasicPaginationViewImpl.class);
		
		// EntityPageTop
		bind(EntityPageTopView.class).to(EntityPageTopViewImpl.class);
		
		// Preview
		bind(PreviewWidgetView.class).to(PreviewWidgetViewImpl.class);
		
		// ActionMenu V2
		bind(ActionMenuWidget.class).to(ActionMenuWidgetImpl.class);
		bind(ActionMenuWidgetView.class).to(ActionMenuWidgetViewImpl.class);
		
		bind(EntityActionController.class).to(EntityActionControllerImpl.class);
		bind(EntityActionControllerView.class).to(EntityActionControllerViewImpl.class);
		bind(PreflightController.class).to(PreflightControllerImpl.class);
		bind(CertifiedUserController.class).to(CertifiedUserControllerImpl.class);
		
		bind(PromptModalView.class).to(PromptModalViewImpl.class);
		bind(BigPromptModalView.class).to(BigPromptModalViewImpl.class);
		bind(PromptTwoValuesModalView.class).to(PromptTwoValuesModalViewImpl.class);
		bind(RenameEntityModalWidget.class).to(RenameEntityModalWidgetImpl.class);
		
		// FileBox
		bind(FileTitleBarViewImpl.class).in(Singleton.class);
		bind(FileTitleBarView.class).to(FileTitleBarViewImpl.class);
		bind(BasicTitleBarView.class).to(BasicTitleBarViewImpl.class);
				
		// Search Box
		bind(SearchBoxViewImpl.class).in(Singleton.class);
		bind(SearchBoxView.class).to(SearchBoxViewImpl.class);
		
		// User Suggest Box
		bind(SynapseSuggestBoxView.class).to(SynapseSuggestBoxViewImpl.class);
		
		bind(MultipartUploader.class).to(MultipartUploaderImpl.class);
		bind(FileInputView.class).to(FileInputViewImpl.class);
		
		bind(FileHandleUploadView.class).to(FileHandleUploadViewImpl.class);
		bind(FileHandleUploadWidget.class).to(FileHandleUploadWidgetImpl.class);

		// LocationableUploader
		bind(UploaderView.class).to(UploaderViewImpl.class);
		
		bind(QuizInfoWidgetView.class).to(QuizInfoViewImpl.class);

		// EntityTreeBrowser
		bind(EntityTreeBrowserView.class).to(EntityTreeBrowserViewImpl.class);

		// MyEntitiesBrowser
		bind(MyEntitiesBrowser.class).in(Singleton.class);
		bind(MyEntitiesBrowserView.class).to(MyEntitiesBrowserViewImpl.class);

		// Wiki Attachments		
		bind(WikiAttachmentsView.class).to(WikiAttachmentsViewImpl.class);

		bind(WikiHistoryWidgetView.class).to(WikiHistoryWidgetViewImpl.class);
		
		//Evaluation selector
		bind(EvaluationListView.class).to(EvaluationListViewImpl.class);
		
		//Administer Evaluations list 
		bind(AdministerEvaluationsListView.class).to(AdministerEvaluationsListViewImpl.class);
		
		// EntitySearchBox		
		bind(EntitySearchBoxView.class).to(EntitySearchBoxViewImpl.class);

		// EntityMetadata
		bind(EntityMetadataView.class).to(EntityMetadataViewImpl.class);
				
		bind(UserProfileEditorWidget.class).to(UserProfileEditorWidgetImpl.class);
		bind(UserProfileEditorWidgetView.class).to(UserProfileEditorWidgetViewImpl.class);
		bind(UserProfileModalWidget.class).to(UserProfileModalWidgetImpl.class);
		bind(UserProfileModalView.class).to(UserProfileModalViewImpl.class);
		bind(ProfileImageView.class).to(ProfileImageViewImpl.class);
		bind(ProfileImageWidget.class).to(ProfileImageWidgetImpl.class);

		// API Table Column manager	
		bind(APITableColumnManagerView.class).to(APITableColumnManagerViewImpl.class);
		bind(APITableColumnConfigView.class).to(APITableColumnConfigViewImpl.class);

		//single subpages view
		bind(WikiSubpagesView.class).to(WikiSubpagesViewImpl.class);
		
		// SubPages Order Editor
		bind(WikiSubpagesOrderEditorView.class).to(WikiSubpagesOrderEditorViewImpl.class);
		bind(WikiSubpagesOrderEditorModalWidget.class).to(WikiSubpagesOrderEditorModalWidgetImpl.class);
		bind(WikiSubpagesOrderEditorModalWidgetView.class).to(WikiSubpagesOrderEditorModalWidgetViewImpl.class);
		
		// SubPages Order Editor Tree
		bind(WikiSubpageOrderEditorTreeView.class).to(WikiSubpageOrderEditorTreeViewImpl.class);
		
		// SubPages Navigation Tree
		bind(WikiSubpageNavigationTreeView.class).to(WikiSubpageNavigationTreeViewImpl.class);
		
		//Widget Registration
		bind(WidgetRegistrarImpl.class).in(Singleton.class);
		bind(WidgetRegistrar.class).to(WidgetRegistrarImpl.class);
		
		// UI Widget Descriptor editor
		bind(BaseEditWidgetDescriptorView.class).to(BaseEditWidgetDescriptorViewImpl.class);
		bind(ReferenceConfigView.class).to(ReferenceConfigViewImpl.class);
		bind(ImageConfigViewImpl.class).in(Singleton.class);
		bind(ImageConfigView.class).to(ImageConfigViewImpl.class);
		bind(AttachmentConfigViewImpl.class).in(Singleton.class);
		bind(AttachmentConfigView.class).to(AttachmentConfigViewImpl.class);
		bind(ProvenanceConfigView.class).to(ProvenanceConfigViewImpl.class);
		bind(LinkConfigView.class).to(LinkConfigViewImpl.class);
		bind(TabbedTableConfigView.class).to(TabbedTableConfigViewImpl.class);
		bind(APITableConfigView.class).to(APITableConfigViewImpl.class);
		bind(QueryTableConfigView.class).to(QueryTableConfigViewImpl.class);
		bind(EntityListConfigView.class).to(EntityListConfigViewImpl.class);
		bind(ShinySiteConfigView.class).to(ShinySiteConfigViewImpl.class);
		bind(ButtonLinkConfigView.class).to(ButtonLinkConfigViewImpl.class);
		bind(VideoConfigView.class).to(VideoConfigViewImpl.class);
		bind(TableQueryResultWikiView.class).to(TableQueryResultWikiViewImpl.class);
		
		// UI Widget Renderers
		bind(ReferenceWidgetView.class).to(ReferenceWidgetViewImpl.class);
		bind(EntityListWidgetView.class).to(EntityListWidgetViewImpl.class);
		bind(ShinySiteWidgetView.class).to(ShinySiteWidgetViewImpl.class);		
		bind(ImageWidgetView.class).to(ImageWidgetViewImpl.class);
		bind(AttachmentPreviewWidgetView.class).to(AttachmentPreviewWidgetViewImpl.class);
		bind(APITableWidgetView.class).to(APITableWidgetViewImpl.class);
		bind(TableOfContentsWidgetView.class).to(TableOfContentsWidgetViewImpl.class);
		bind(WikiFilesPreviewWidgetView.class).to(WikiFilesPreviewWidgetViewImpl.class);
		bind(ButtonLinkWidgetView.class).to(ButtonLinkWidgetViewImpl.class);
		bind(EmptyWidgetView.class).to(EmptyWidgetViewImpl.class);
		bind(VideoWidgetView.class).to(VideoWidgetViewImpl.class);
		bind(TeamMemberCountView.class).to(TeamMemberCountViewImpl.class);
		
		
		// ProvenanceWidget
		bind(ProvenanceWidgetView.class).to(ProvenanceWidgetViewImpl.class);
		
		// MarkdownWidget
		bind(MarkdownWidgetView.class).to(MarkdownWidgetViewImpl.class);
		
		// MarkdownEditorWidget
		bind(MarkdownEditorWidgetView.class).to(MarkdownEditorWidgetViewImpl.class);
		
		// FilesBrowser
		bind(FilesBrowserView.class).to(FilesBrowserViewImpl.class);
		
		// Entity Finder
		bind(EntityFinderView.class).to(EntityFinderViewImpl.class);		
		
		// MoreTreeItem
		bind(MoreTreeItemView.class).to(MoreTreeItemViewImpl.class);

		bind(EvaluationSubmitterView.class).to(EvaluationSubmitterViewImpl.class);
		
		bind(FavoriteWidgetView.class).to(FavoriteWidgetViewImpl.class);
				
		bind(WikiPageWidgetView.class).to(WikiPageWidgetViewImpl.class);
		bind(UserBadgeView.class).to(UserBadgeViewImpl.class);
		
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
		bind(ColumnModelsViewBase.class).to(ColumnModelsViewBaseImpl.class);
		bind(ColumnModelsView.class).to(ColumnModelsViewImpl.class);
		bind(ColumnModelTableRowEditorView.class).to(ColumnModelTableRowEditorViewImpl.class);
		bind(ColumnModelTableRowEditorWidget.class).to(ColumnModelTableRowEditorWidgetImpl.class);
		bind(ColumnModelTableRowViewer.class).to(ColumnModelTableRowViewerImpl.class);
		bind(TableEntityWidgetView.class).to(TableEntityWidgetViewImpl.class);
		bind(RowView.class).to(RowViewImpl.class);
		bind(TablePageView.class).to(TablePageViewImpl.class);
		bind(TableQueryResultView.class).to(TableQueryResultViewImpl.class);
		bind(QueryResultEditorView.class).to(QueryResultEditorViewImpl.class);
		bind(CellFactory.class).to(CellFactoryImpl.class);
		bind(QueryInputView.class).to(QueryInputViewImpl.class);
		bind(JobTrackingWidget.class).to(AsynchronousProgressWidget.class);
		bind(UploadTableModalWidget.class).to(UploadTableModalWidgetImpl.class);
		bind(UploadCSVPreviewPage.class).to(UploadCSVPreviewPageImpl.class);
		bind(CSVOptionsView.class).to(CSVOptionsViewImpl.class);
		bind(CSVOptionsWidget.class).to(CSVOptionsWidgetImpl.class);
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
		
		bind(CreateDownloadPage.class).to(CreateDownloadPageImpl.class);
		bind(CreateDownloadPageView.class).to(CreateDownloadPageViewImpl.class);
		
		bind(DownloadFilePage.class).to(DownloadFilePageImpl.class);
		bind(DownloadFilePageView.class).to(DownloadFilePageViewImpl.class);
		bind(DownloadTableQueryModalWidget.class).to(DownloadTableQueryModalWidgetImpl.class);
		
		// Keyboard navigation
		bind(KeyboardNavigationHandler.class).to(KeyboardNavigationHandlerImpl.class);
		bind(FocusSetter.class).to(FocusSetterImpl.class);
		
		/*
		 * TableEntity cell bindings.
		 */
		bind(CellEditorView.class).to(CellEditorViewImpl.class);
		bind(StringEditorCell.class).to(StringEditorCellImpl.class);
		bind(StringRendererCell.class).to(StringRendererCellImpl.class);
		bind(EntityIdCellEditor.class).to(EntityIdCellEditorImpl.class);
		bind(EntityIdCellRenderer.class).to(EntityIdCellRendererImpl.class);
		bind(EnumCellEditor.class).to(EnumCellEditorImpl.class);
		bind(ListCellEdtiorView.class).to(ListCellEditorViewImpl.class);
		bind(BooleanCellEditor.class).to(BooleanCellEditorImpl.class);
		bind(DateCellEditorView.class).to(DateCellEditorViewImpl.class);
		bind(UserIdCellEditorView.class).to(UserIdCellEditorViewImpl.class);
		bind(DateCellEditor.class).to(DateCellEditorImpl.class);
		bind(UserIdCellEditor.class).to(UserIdCellEditorImpl.class);
		bind(DateCellRenderer.class).to(DateCellRendererImpl.class);
		bind(DoubleCellEditor.class).to(DoubleCellEditorImpl.class);
		bind(IntegerCellEditor.class).to(IntegerCellEditorImpl.class);
		bind(LinkCellRenderer.class).to(LinkCellRendererImpl.class);
		bind(FileCellEditorView.class).to(FileCellEditorViewImpl.class);
		bind(FileCellEditor.class).to(FileCellEditorImpl.class);
		bind(FileCellRenderer.class).to(FileCellRendererImpl.class);
		bind(FileCellRendererView.class).to(FileCellRendererViewImpl.class);
		bind(EntityIdCellRendererView.class).to(EntityIdCellRendererViewImpl.class);
		bind(UserIdCellRenderer.class).to(UserIdCellRendererImpl.class);
		
		/*
		 * Teams Places
		 */
		// Team Page
		bind(TeamViewImpl.class).in(Singleton.class);
		bind(TeamView.class).to(TeamViewImpl.class);
		
		// Team Search Page
		bind(TeamSearchViewImpl.class).in(Singleton.class);
		bind(TeamSearchView.class).to(TeamSearchViewImpl.class);
		
		bind(MapView.class).to(MapViewImpl.class);
		
		// People Search Page
		bind(PeopleSearchViewImpl.class).in(Singleton.class);
		bind(PeopleSearchView.class).to(PeopleSearchViewImpl.class);
		
		/*
		 * Teams Widgets
		 */
		
		// Team Action Menu Items
		bind(TeamEditModalWidgetView.class).to(TeamEditModalWidgetViewImpl.class);
		bind(TeamLeaveModalWidgetView.class).to(TeamLeaveModalWidgetViewImpl.class);
		bind(TeamDeleteModalWidgetView.class).to(TeamDeleteModalWidgetViewImpl.class);
		
		// Open Team Invitations widget
		bind(OpenTeamInvitationsWidgetView.class).to(OpenTeamInvitationsWidgetViewImpl.class);
		
		// Pending Team Join Requests widget
		bind(OpenMembershipRequestsWidgetView.class).to(OpenMembershipRequestsWidgetViewImpl.class);
		
		// Current User Invites widget
		bind(OpenUserInvitationsWidgetView.class).to(OpenUserInvitationsWidgetViewImpl.class);
		
		// Team List widget (link to search teams page, optionally can create team)
		bind(TeamListWidgetView.class).to(TeamListWidgetViewImpl.class);
		
		// User Group List widget
		bind(UserGroupListWidgetView.class).to(UserGroupListWidgetViewImpl.class);

		// Member List widget
		bind(MemberListWidgetView.class).to(MemberListWidgetViewImpl.class);
		
		//Invite Team member widget
		bind(InviteWidgetView.class).to(InviteWidgetViewImpl.class);
		
		//Request Team membership widget
		bind(JoinTeamWidgetView.class).to(JoinTeamWidgetViewImpl.class);
		
		//Join Team Button Config widget
		bind(JoinTeamConfigEditorView.class).to(JoinTeamConfigEditorViewImpl.class);
		
		//Submit to evaluation widget
		bind(SubmitToEvaluationWidgetView.class).to(SubmitToEvaluationWidgetViewImpl.class);
		//Team renderer
		bind(TeamBadgeView.class).to(TeamBadgeViewImpl.class);
		bind(BigTeamBadgeView.class).to(BigTeamBadgeViewImpl.class);
		
		
		bind(UserTeamConfigView.class).to(UserTeamConfigViewImpl.class);
		
		bind(SharingAndDataUseConditionWidgetView.class).to(SharingAndDataUseConditionWidgetViewImpl.class);
		
		bind(WizardProgressWidgetView.class).to(WizardProgressWidgetViewImpl.class);
		bind(UploadDialogWidgetView.class).to(UploadDialogWidgetViewImpl.class);
		
		bind(LoginModalView.class).to(LoginModalViewImpl.class);
		
		bind(ImageParamsPanelView.class).to(ImageParamsPanelViewImpl.class);
		bind(RegisterTeamDialogView.class).to(RegisterTeamDialogViewImpl.class);
		bind(EditRegisteredTeamDialogView.class).to(EditRegisteredTeamDialogViewImpl.class);
		bind(ChallengeTeamsView.class).to(ChallengeTeamsViewImpl.class);
		bind(ChallengeBadgeView.class).to(ChallengeBadgeViewImpl.class);
		bind(ProjectBadgeView.class).to(ProjectBadgeViewImpl.class);
		bind(TableQueryResultWikiWidgetView.class).to(TableQueryResultWikiWidgetViewImpl.class);
		
		bind(SingleButtonView.class).to(SingleButtonViewImpl.class);
		bind(UserListView.class).to(UserListViewImpl.class);
		
		bind(AnnotationTransformerImpl.class).in(Singleton.class);
		bind(AnnotationTransformer.class).to(AnnotationTransformerImpl.class);
		bind(AnnotationEditorView.class).to(AnnotationEditorViewImpl.class);
		bind(EditAnnotationsDialogView.class).to(EditAnnotationsDialogViewImpl.class);
		
		bind(AnnotationCellFactoryImpl.class).in(Singleton.class);
		bind(AnnotationCellFactory.class).to(AnnotationCellFactoryImpl.class);
		
		bind(FileHistoryRowView.class).to(FileHistoryRowViewImpl.class);
		bind(SynapseStandaloneWikiView.class).to(SynapseStandaloneWikiViewImpl.class);
		
		bind(SynapseAlertView.class).to(SynapseAlertViewImpl.class);
		bind(SynapseAlert.class).to(SynapseAlertImpl.class);
		
		bind(ProvenanceEditorWidgetView.class).to(ProvenanceEditorWidgetViewImpl.class);
		bind(ProvenanceListWidgetView.class).to(ProvenanceListWidgetViewImpl.class);
		bind(ProvenanceURLDialogWidgetView.class).to(ProvenanceURLDialogWidgetViewImpl.class);
		bind(EntityRefProvEntryView.class).to(EntityRefProvEntryViewImpl.class);
		bind(URLProvEntryView.class).to(URLProvEntryViewImpl.class);
		bind(StorageLocationWidgetView.class).to(StorageLocationWidgetViewImpl.class);
		bind(ErrorView.class).to(ErrorViewImpl.class);
		bind(PreviewConfigView.class).to(PreviewConfigViewImpl.class);
		
		bind(EditFileMetadataModalView.class).to(EditFileMetadataModalViewImpl.class);
		bind(EditFileMetadataModalWidget.class).to(EditFileMetadataModalWidgetImpl.class);
		bind(EditProjectMetadataModalView.class).to(EditProjectMetadataModalViewImpl.class);
		bind(EditProjectMetadataModalWidget.class).to(EditProjectMetadataModalWidgetImpl.class);
		bind(BiodallianceWidgetView.class).to(BiodallianceWidgetViewImpl.class);
		bind(BiodallianceSourceEditorView.class).to(BiodallianceSourceEditorViewImpl.class);
		bind(BiodallianceEditorView.class).to(BiodallianceEditorViewImpl.class);
		
		bind(TabView.class).to(TabViewImpl.class);
		bind(TabsView.class).to(TabsViewImpl.class);
		
		bind(FilesTabView.class).to(FilesTabViewImpl.class);
		bind(TablesTabView.class).to(TablesTabViewImpl.class);
		bind(ChallengeTabView.class).to(ChallengeTabViewImpl.class);
		bind(DiscussionTabView.class).to(DiscussionTabViewImpl.class);
		bind(DockerTabView.class).to(DockerTabViewImpl.class);
		bind(ModifiedCreatedByWidgetView.class).to(ModifiedCreatedByWidgetViewImpl.class);
		bind(FileHandleListView.class).to(FileHandleListViewImpl.class);
		bind(ACTView.class).to(ACTViewImpl.class);
		bind(CytoscapeConfigView.class).to(CytoscapeConfigViewImpl.class);
		bind(CytoscapeView.class).to(CytoscapeViewImpl.class);

		// discussion
		bind(DiscussionThreadModalView.class).to(DiscussionThreadModalViewImpl.class);
		bind(ReplyModalView.class).to(ReplyModalViewImpl.class);
		bind(DiscussionThreadListWidgetView.class).to(DiscussionThreadListWidgetViewImpl.class);
		bind(DiscussionThreadListItemWidgetView.class).to(DiscussionThreadListItemWidgetViewImpl.class);
		bind(SingleDiscussionThreadWidgetView.class).to(SingleDiscussionThreadWidgetViewImpl.class);
		bind(ReplyWidgetView.class).to(ReplyWidgetViewImpl.class);
		bind(ForumWidgetView.class).to(ForumWidgetViewImpl.class);
		bind(NewReplyWidgetView.class).to(NewReplyWidgetViewImpl.class);

		// docker
		bind(DockerRepoListWidgetView.class).to(DockerRepoListWidgetViewImpl.class);
		bind(DockerRepoWidgetView.class).to(DockerRepoWidgetViewImpl.class);
		bind(AddExternalRepoModalView.class).to(AddExternalRepoModalViewImpl.class);
		bind(DockerCommitRowWidgetView.class).to(DockerCommitRowWidgetViewImpl.class);
		bind(DockerCommitListWidgetView.class).to(DockerCommitListWidgetViewImpl.class);
		
		bind(SessionStorage.class).to(SessionStorageImpl.class);
		bind(SynapseForumView.class).to(SynapseForumViewImpl.class);
		bind(WikiMarkdownEditorView.class).to(WikiMarkdownEditorViewImpl.class);
		bind(StuAlertView.class).to(StuAlertViewImpl.class);
		
		bind(SynapseTableFormWidgetView.class).to(SynapseTableFormWidgetViewImpl.class);
		bind(RowFormView.class).to(RowFormViewImpl.class);
		bind(RadioCellEditorView.class).to(RadioCellEditorViewImpl.class);
		bind(BooleanFormCellEditor.class).to(BooleanFormCellEditorImpl.class);
		bind(EnumFormCellEditor.class).to(EnumFormCellEditorImpl.class);
		
		bind(MarkdownIt.class).to(MarkdownItImpl.class);
		bind(SubscriptionView.class).to(SubscriptionViewImpl.class);
		bind(TopicWidgetView.class).to(TopicWidgetViewImpl.class);
		bind(SubscribeButtonWidgetView.class).to(SubscribeButtonWidgetViewImpl.class);
		bind(SubscriptionListWidgetView.class).to(SubscriptionListWidgetViewImpl.class);
		bind(TopicRowWidgetView.class).to(TopicRowWidgetViewImpl.class);
		bind(RefreshAlertView.class).to(RefreshAlertViewImpl.class);
		bind(PasswordStrengthWidgetView.class).to(PasswordStrengthWidgetViewImpl.class);
		bind(ZxcvbnWrapper.class).to(ZxcvbnWrapperImpl.class);
		
		bind(UserSelectorView.class).to(UserSelectorViewImpl.class);
		bind(CreateTableViewWizardStep1View.class).to(CreateTableViewWizardStep1ViewImpl.class);
		bind(EntityContainerListWidgetView.class).to(EntityContainerListWidgetViewImpl.class);
		bind(StuAnnouncementWidgetView.class).to(StuAnnouncementWidgetViewImpl.class);
		bind(ScopeWidgetView.class).to(ScopeWidgetViewImpl.class);
		bind(CopyTextModal.class).to(CopyTextModalImpl.class);
		
		bind(EvaluationEditorModalView.class).to(EvaluationEditorModalViewImpl.class);
		bind(LoadMoreWidgetContainerView.class).to(LoadMoreWidgetContainerViewImpl.class);
		bind(RadioWidget.class).to(RadioWidgetViewImpl.class);
		
		bind(FileClientsHelp.class).to(FileClientsHelpImpl.class);
		bind(ContainerClientsHelp.class).to(ContainerClientsHelpImpl.class);
		bind(FileDownloadButtonView.class).to(FileDownloadButtonViewImpl.class);
		bind(CreateTableViewWizardStep2View.class).to(CreateTableViewWizardStep2ViewImpl.class);
		bind(ChallengeWidgetView.class).to(ChallengeWidgetViewImpl.class);
		bind(SelectTeamModalView.class).to(SelectTeamModalViewImpl.class);
		bind(ApproveUserAccessModalView.class).to(ApproveUserAccessModalViewImpl.class);
		bind(UserBadgeListView.class).to(UserBadgeListViewImpl.class);
		bind(EntityListRowBadgeView.class).to(EntityListRowBadgeViewImpl.class);
		
		bind(LazyLoadWikiWidgetWrapperView.class).to(LazyLoadWikiWidgetWrapperViewImpl.class);
		
		bind(EntityHeaderAsyncHandlerImpl.class).in(Singleton.class);
		bind(EntityHeaderAsyncHandler.class).to(EntityHeaderAsyncHandlerImpl.class);
		
		bind(GoogleMapView.class).to(GoogleMapViewImpl.class);
		
		bind(FileHandleAsyncHandlerImpl.class).in(Singleton.class);
		bind(FileHandleAsyncHandler.class).to(FileHandleAsyncHandlerImpl.class);
		
		bind(UserProfileAsyncHandlerImpl.class).in(Singleton.class);
		bind(UserProfileAsyncHandler.class).to(UserProfileAsyncHandlerImpl.class);
		
		bind(UserGroupHeaderAsyncHandlerImpl.class).in(Singleton.class);
		bind(UserGroupHeaderAsyncHandler.class).to(UserGroupHeaderAsyncHandlerImpl.class);
		
		bind(UserGroupHeaderFromAliasAsyncHandlerImpl.class).in(Singleton.class);
		bind(UserGroupHeaderFromAliasAsyncHandler.class).to(UserGroupHeaderFromAliasAsyncHandlerImpl.class);
		
		bind(DivView.class).to(DivViewImpl.class);
		bind(FacetColumnResultValuesView.class).to(FacetColumnResultValuesViewImpl.class);
		bind(FacetColumnResultSliderRangeView.class).to(FacetColumnResultSliderRangeViewImpl.class);
		bind(FacetColumnResultRangeView.class).to(FacetColumnResultRangeViewImpl.class);
		bind(FacetColumnResultDateRangeView.class).to(FacetColumnResultDateRangeViewImpl.class);
		
		bind(ViewDefaultColumns.class).in(Singleton.class);
		bind(SubscribersWidgetView.class).to(SubscribersWidgetViewImpl.class);
		bind(PlaceView.class).to(PlaceViewImpl.class);
		bind(ManagedACTAccessRequirementWidgetView.class).to(ManagedACTAccessRequirementWidgetViewImpl.class);
		bind(ACTAccessRequirementWidgetView.class).to(ACTAccessRequirementWidgetViewImpl.class);
		bind(TermsOfUseAccessRequirementWidgetView.class).to(TermsOfUseAccessRequirementWidgetViewImpl.class);
		bind(CreateResearchProjectWizardStep1View.class).to(CreateResearchProjectWizardStep1ViewImpl.class);
		bind(CreateDataAccessSubmissionWizardStep2View.class).to(CreateDataAccessSubmissionWizardStep2ViewImpl.class);
		bind(FileHandleWidgetView.class).to(FileHandleWidgetViewImpl.class);
		bind(CreateAccessRequirementStep1View.class).to(CreateAccessRequirementStep1ViewImpl.class);
		bind(CreateManagedACTAccessRequirementStep2View.class).to(CreateManagedACTAccessRequirementStep2ViewImpl.class);
		bind(CreateBasicAccessRequirementStep2View.class).to(CreateBasicAccessRequirementStep2ViewImpl.class);
		bind(Button.class).to(ButtonImpl.class);

		bind(IsACTMemberAsyncHandlerImpl.class).in(Singleton.class);
		bind(IsACTMemberAsyncHandler.class).to(IsACTMemberAsyncHandlerImpl.class);
		
		bind(PopupUtilsViewImpl.class).in(Singleton.class);
		bind(PopupUtilsView.class).to(PopupUtilsViewImpl.class);
		bind(ProfileCertifiedValidatedView.class).to(ProfileCertifiedValidatedViewImpl.class);
		bind(ACTDataAccessSubmissionsView.class).to(ACTDataAccessSubmissionsViewImpl.class);
		bind(RestrictionWidgetView.class).to(RestrictionWidgetViewImpl.class);
		bind(ACTDataAccessSubmissionWidgetView.class).to(ACTDataAccessSubmissionWidgetViewImpl.class);
		bind(OpenSubmissionWidgetView.class).to(OpenSubmissionWidgetViewImpl.class);
		bind(LockAccessRequirementWidgetView.class).to(LockAccessRequirementWidgetViewImpl.class);
		bind(ImageUploadView.class).to(ImageUploadViewImpl.class);
		bind(RevokeUserAccessModalView.class).to(RevokeUserAccessModalViewImpl.class);
		bind(PlotlyWidgetView.class).to(PlotlyWidgetViewImpl.class);
		bind(PlotlyConfigView.class).to(PlotlyConfigViewImpl.class);
		
		bind(DateTimeUtilsImpl.class).in(Singleton.class);
		bind(DateTimeUtils.class).to(DateTimeUtilsImpl.class);
		bind(ACTAccessApprovalsView.class).to(ACTAccessApprovalsViewImpl.class);
		bind(AccessorGroupView.class).to(AccessorGroupViewImpl.class);
		bind(SelfSignAccessRequirementWidgetView.class).to(SelfSignAccessRequirementWidgetViewImpl.class);
		bind(SubjectWidgetView.class).to(SubjectWidgetViewImpl.class);
		bind(AwsLoginView.class).to(AwsLoginViewImpl.class);
		bind(TeamMemberRowWidgetView.class).to(TeamMemberRowWidgetViewImpl.class);
		bind(TeamMembersWidgetView.class).to(TeamMembersWidgetViewImpl.class);
		bind(FileViewClientsHelp.class).to(FileViewClientsHelpImpl.class);
		bind(EmailAddressesWidgetView.class).to(EmailAddressesWidgetViewImpl.class);
		
		// Synapse js client
		bind(SynapseJavascriptClient.class).in(Singleton.class);
		bind(SynapseJavascriptFactory.class).in(Singleton.class);
	}
}
