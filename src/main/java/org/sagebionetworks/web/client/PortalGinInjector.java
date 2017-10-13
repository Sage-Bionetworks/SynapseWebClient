package org.sagebionetworks.web.client;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.presenter.*;
import org.sagebionetworks.web.client.presenter.users.PasswordResetPresenter;
import org.sagebionetworks.web.client.presenter.users.RegisterAccountPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.FileHandleWidget;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.RadioWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.*;
import org.sagebionetworks.web.client.widget.accessrequirements.approval.AccessorGroupWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement.CreateAccessRequirementWizard;
import org.sagebionetworks.web.client.widget.accessrequirements.requestaccess.CreateDataAccessRequestWizard;
import org.sagebionetworks.web.client.widget.accessrequirements.submission.ACTDataAccessSubmissionWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.submission.OpenSubmissionWidget;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressWidget;
import org.sagebionetworks.web.client.widget.biodalliance13.BiodallianceWidget;
import org.sagebionetworks.web.client.widget.biodalliance13.editor.BiodallianceEditor;
import org.sagebionetworks.web.client.widget.biodalliance13.editor.BiodallianceSourceEditor;
import org.sagebionetworks.web.client.widget.breadcrumb.Breadcrumb;
import org.sagebionetworks.web.client.widget.clienthelp.FileClientsHelp;
import org.sagebionetworks.web.client.widget.discussion.DiscussionThreadListItemWidget;
import org.sagebionetworks.web.client.widget.discussion.DiscussionThreadListWidget;
import org.sagebionetworks.web.client.widget.discussion.ForumWidget;
import org.sagebionetworks.web.client.widget.discussion.ReplyWidget;
import org.sagebionetworks.web.client.widget.docker.DockerCommitRowWidget;
import org.sagebionetworks.web.client.widget.docker.DockerRepoListWidget;
import org.sagebionetworks.web.client.widget.docker.DockerRepoWidget;
import org.sagebionetworks.web.client.widget.entity.*;
import org.sagebionetworks.web.client.widget.entity.act.ACTRevokeUserAccessModal;
import org.sagebionetworks.web.client.widget.entity.act.ApproveUserAccessModal;
import org.sagebionetworks.web.client.widget.entity.act.UserBadgeItem;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationEditor;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import org.sagebionetworks.web.client.widget.entity.browse.EntityTreeBrowser;
import org.sagebionetworks.web.client.widget.entity.browse.FilesBrowser;
import org.sagebionetworks.web.client.widget.entity.controller.*;
import org.sagebionetworks.web.client.widget.entity.download.UploadDialogWidget;
import org.sagebionetworks.web.client.widget.entity.download.Uploader;
import org.sagebionetworks.web.client.widget.entity.editor.*;
import org.sagebionetworks.web.client.widget.entity.file.BasicTitleBar;
import org.sagebionetworks.web.client.widget.entity.file.FileTitleBar;
import org.sagebionetworks.web.client.widget.entity.file.Md5Link;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.*;
import org.sagebionetworks.web.client.widget.entity.tabs.*;
import org.sagebionetworks.web.client.widget.evaluation.AdministerEvaluationsList;
import org.sagebionetworks.web.client.widget.evaluation.ChallengeWidget;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationEditorModal;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationSubmitter;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadWikiWidgetWrapper;
import org.sagebionetworks.web.client.widget.login.LoginWidget;
import org.sagebionetworks.web.client.widget.profile.ProfileCertifiedValidatedWidget;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidget;
import org.sagebionetworks.web.client.widget.refresh.DiscussionThreadCountAlert;
import org.sagebionetworks.web.client.widget.refresh.RefreshAlert;
import org.sagebionetworks.web.client.widget.refresh.ReplyCountAlert;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListModalWidget;
import org.sagebionetworks.web.client.widget.subscription.TopicRowWidget;
import org.sagebionetworks.web.client.widget.table.KeyboardNavigationHandler;
import org.sagebionetworks.web.client.widget.table.TableListWidget;
import org.sagebionetworks.web.client.widget.table.v2.QueryTokenProvider;
import org.sagebionetworks.web.client.widget.table.v2.TableEntityWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.*;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.*;
import org.sagebionetworks.web.client.widget.table.v2.results.facets.FacetColumnResultDateRangeWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.facets.FacetColumnResultRangeWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.facets.FacetColumnResultSliderRangeWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.facets.FacetColumnResultValuesWidget;
import org.sagebionetworks.web.client.widget.table.v2.schema.*;
import org.sagebionetworks.web.client.widget.team.*;
import org.sagebionetworks.web.client.widget.upload.FileHandleLink;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.client.widget.user.UserGroupListWidget;
import org.sagebionetworks.web.client.widget.verification.VerificationSubmissionModalViewImpl;
import org.sagebionetworks.web.client.widget.verification.VerificationSubmissionRowViewImpl;
import org.sagebionetworks.web.client.widget.verification.VerificationSubmissionWidget;

/**
 * The root portal dependency injection root.
 * 
 * @author jmhill
 *
 */
@GinModules(PortalGinModule.class)
public interface PortalGinInjector extends Ginjector {

	BulkPresenterProxy getBulkPresenterProxy();

	GlobalApplicationState getGlobalApplicationState();

	PresenterProxy<HomePresenter, Home> getHomePresenter();

	EntityPresenter getEntityPresenter();

	ProjectsHomePresenter getProjectsHomePresenter();

	LoginPresenter getLoginPresenter();

	AuthenticationController getAuthenticationController();

	PasswordResetPresenter getPasswordResetPresenter();

	RegisterAccountPresenter getRegisterAccountPresenter();

	ProfilePresenter getProfilePresenter();

	ComingSoonPresenter getComingSoonPresenter();

	ChallengeOverviewPresenter getChallengeOverviewPresenter();

	HelpPresenter getHelpPresenter();

	SearchPresenter getSearchPresenter();

	SynapseWikiPresenter getSynapseWikiPresenter();

	DownPresenter getDownPresenter();

	TeamPresenter getTeamPresenter();

	MapPresenter getMapPresenter();

	QuizPresenter getQuizPresenter();

	CertificatePresenter getCertificatePresenter();

	AccountPresenter getAccountPresenter();

	NewAccountPresenter getNewAccountPresenter();

	SignedTokenPresenter getSignedTokenPresenter();

	ErrorPresenter getErrorPresenter();

	ChangeUsernamePresenter getChangeUsernamePresenter();

	TrashPresenter getTrashPresenter();

	TeamSearchPresenter getTeamSearchPresenter();

	PeopleSearchPresenter getPeopleSearchPresenter();

	SynapseStandaloneWikiPresenter getSynapseStandaloneWikiPresenter();

	EventBus getEventBus();

	JiraURLHelper getJiraURLHelper();

	MarkdownWidget getMarkdownWidget();

	ActionMenuWidget createActionMenuWidget();

	EntityActionController createEntityActionController();

	ACTPresenter getACTPresenter();

	AccessRequirementsPresenter getAccessRequirementsPresenter();

	ACTDataAccessSubmissionsPresenter getACTDataAccessSubmissionsPresenter();

	ACTDataAccessSubmissionDashboardPresenter getACTDataAccessSubmissionDashboardPresenter();

	SynapseForumPresenter getSynapseForumPresenter();

	SubscriptionPresenter getSubscriptionPresenter();

	ACTAccessApprovalsPresenter getACTAccessApprovalsPresenter();

	EmailInvitationPresenter getEmailInvitationPresenter();

	/*
	 *  Markdown Widgets
	 */
	////// Editors
	ReferenceConfigEditor getReferenceConfigEditor();

	ProvenanceConfigEditor getProvenanceConfigEditor();

	ImageConfigEditor getImageConfigEditor();

	ImageLinkConfigEditor getImageLinkConfigEditor();

	AttachmentConfigEditor getAttachmentConfigEditor();

	LinkConfigEditor getLinkConfigEditor();

	APITableConfigEditor getSynapseAPICallConfigEditor();

	QueryTableConfigEditor getSynapseQueryConfigEditor();

	LeaderboardConfigEditor getLeaderboardConfigEditor();

	TabbedTableConfigEditor getTabbedTableConfigEditor();

	EntityTreeBrowser getEntityTreeBrowser();

	EntityListConfigEditor getEntityListConfigEditor();

	ShinySiteConfigEditor getShinySiteConfigEditor();

	ButtonLinkConfigEditor getButtonLinkConfigEditor();

	UserTeamConfigEditor getUserTeamConfigEditor();

	VideoConfigEditor getVideoConfigEditor();

	TableQueryResultWikiEditor getSynapseTableQueryResultEditor();

	PreviewConfigEditor getPreviewConfigEditor();

	BiodallianceEditor getBiodallianceEditor();

	BiodallianceSourceEditor getBiodallianceSourceEditor();

	CytoscapeConfigEditor getCytoscapeConfigEditor();

	PlotlyConfigEditor getPlotlyConfigEditor();
	
	////// Renderers
	ReferenceWidget getReferenceRenderer();

	TutorialWizard getTutorialWidgetRenderer();

	ProvenanceWidget getProvenanceRenderer();

	AdministerEvaluationsList getAdministerEvaluationsList();

	ImageWidget getImageRenderer();

	AttachmentPreviewWidget getAttachmentPreviewRenderer();

	APITableWidget getSynapseAPICallRenderer();

	TableOfContentsWidget getTableOfContentsRenderer();

	WikiSubpagesWidget getWikiSubpagesRenderer();

	WikiFilesPreviewWidget getWikiFilesPreviewRenderer();

	EntityListWidget getEntityListRenderer();

	ShinySiteWidget getShinySiteRenderer();

	JoinTeamWidget getJoinTeamWidget();

	SubmitToEvaluationWidget getEvaluationSubmissionWidget();

	EmptyWidget getEmptyWidget();

	ButtonLinkWidget getButtonLinkWidget();

	VideoWidget getVideoWidget();

	TableQueryResultWikiWidget getSynapseTableQueryResultWikiWidget();

	RegisterChallengeTeamWidget getRegisterChallengeTeamWidget();

	ChallengeTeamsWidget getChallengeTeamsWidget();

	ChallengeParticipantsWidget getChallengeParticipantsWidget();

	BiodallianceWidget getBiodallianceRenderer();

	CytoscapeWidget getCytoscapeRenderer();

	SynapseTableFormWidget getSynapseTableFormWidget();

	TeamMembersWidget getTeamMembersWidget();

	TeamMemberCountWidget getTeamMemberCountWidget();

	PlotlyWidget getPlotlyWidget();

	LazyLoadWikiWidgetWrapper getLazyLoadWikiWidgetWrapper();
	//////API Table Column Editor
	APITableColumnConfigView getAPITableColumnConfigView();
	
	//////API Table Column Renderers
	APITableColumnRendererNone getAPITableColumnRendererNone();

	APITableColumnRendererUserId getAPITableColumnRendererUserId();

	APITableColumnRendererDate getAPITableColumnRendererDate();

	APITableColumnRendererLink getAPITableColumnRendererLink();

	APITableColumnRendererSynapseID getAPITableColumnRendererSynapseID();

	APITableColumnRendererEntityIdAnnotations getAPITableColumnRendererEntityAnnotations();

	APITableColumnRendererCancelControl getAPITableColumnRendererCancelControl();
	
	// Other widgets
	UserBadge getUserBadgeWidget();

	VersionTimer getVersionTimer();

	Md5Link getMd5Link();

	QuestionContainerWidget getQuestionContainerWidget();

	SynapseAlert getSynapseAlertWidget();

	EntityRefProvEntryView getEntityRefEntry();

	URLProvEntryView getURLEntry();

	ProvenanceListWidget getProvenanceListWidget();

	PreviewWidget getPreviewWidget();

	UserBadgeItem getUserBadgeItem();
	
	// TableEntity V2
	ColumnModelsView createNewColumnModelsView();

	ImportTableViewColumnsButton getImportTableViewColumnsButton();

	ColumnModelsWidget createNewColumnModelsWidget();

	ColumnModelTableRowViewer createNewColumnModelTableRowViewer();

	ColumnModelTableRowEditorWidget createColumnModelEditorWidget();

	TableEntityWidget createNewTableEntityWidget();

	RowWidget createRowWidget();

	TablePageWidget createNewTablePageWidget();

	QueryResultEditorWidget createNewQueryResultEditorWidget();
	
	// TableEntity V2 cells
	StringRendererCell createStringRendererCell();

	StringEditorCell createStringEditorCell();

	EntityIdCellEditor createEntityIdCellEditor();

	EntityIdCellRenderer createEntityIdCellRenderer();

	EnumCellEditor createEnumCellEditor();

	EnumFormCellEditor createEnumFormCellEditor();

	BooleanCellEditor createBooleanCellEditor();

	BooleanFormCellEditor createBooleanFormCellEditor();

	DateCellEditor createDateCellEditor();

	DateCellRenderer createDateCellRenderer();

	DoubleCellEditor createDoubleCellEditor();

	IntegerCellEditor createIntegerCellEditor();

	LinkCellRenderer createLinkCellRenderer();

	FileCellEditor createFileCellEditor();

	FileCellRenderer createFileCellRenderer();

	UserIdCellRenderer createUserIdCellRenderer();

	UserIdCellEditor createUserIdCellEditor();
	// Asynchronous
	AsynchronousProgressWidget creatNewAsynchronousProgressWidget();

	UserTeamBadge getUserTeamBadgeWidget();

	TeamBadge getTeamBadgeWidget();

	BigTeamBadge getBigTeamBadgeWidget();

	ChallengeBadge getChallengeBadgeWidget();

	ProjectBadge getProjectBadgeWidget();

	EntityTreeItem getEntityTreeItemWidget();

	MoreTreeItem getMoreTreeWidget();

	UserGroupListWidget getUserGroupListWidget();

	UserGroupSuggestionProvider getUserGroupSuggestOracleImpl();

	TableListWidget getTableListWidget();

	Uploader getUploaderWidget();

	CookieProvider getCookieProvider();

	KeyboardNavigationHandler createKeyboardNavigationHandler();

	Header getHeader();

	Footer getFooter();

	SortableTableHeader createSortableTableHeader();

	StaticTableHeader createStaticTableHeader();

	EvaluationSubmitter getEvaluationSubmitter();

	RegisterTeamDialog getRegisterTeamDialog();

	AnnotationEditor getAnnotationEditor();

	FileHistoryRowView getFileHistoryRow();

	FileHistoryWidget getFileHistoryWidget();

	JoinTeamConfigEditor getJoinTeamConfigEditor();

	ModifiedCreatedByWidget getModifiedCreatedByWidget();

	FileHandleLink getFileHandleLink();

	VerificationSubmissionWidget getVerificationSubmissionWidget();

	VerificationSubmissionModalViewImpl getVerificationSubmissionModalViewImpl();

	VerificationSubmissionRowViewImpl getVerificationSubmissionRowViewImpl();

	// discussion
	DiscussionThreadListItemWidget createThreadListItemWidget();

	ReplyWidget createReplyWidget();

	TopicRowWidget getTopicRowWidget();

	RefreshAlert getRefreshAlert();

	ReplyCountAlert getReplyCountAlert();

	DiscussionThreadCountAlert getDiscussionThreadCountAlert();

	// docker
	DockerRepoWidget createNewDockerRepoWidget();

	DockerCommitRowWidget createNewDockerCommitRowWidget();

	LoginWidget getLoginWidget();

	FileClientsHelp getFileClientsHelp();

	LoadMoreWidgetContainer getLoadMoreProjectsWidgetContainer();

	RadioWidget createNewRadioWidget();

	EntityListRowBadge getEntityListRowBadge();

	CancelControlWidget getCancelControlWidget();

	FacetColumnResultSliderRangeWidget getFacetColumnResultSliderRangeWidget();

	FacetColumnResultRangeWidget getFacetColumnResultRangeWidget();

	FacetColumnResultValuesWidget getFacetColumnResultValuesWidget();

	FacetColumnResultDateRangeWidget getFacetColumnResultDateRangeWidget();

	DiscussionTabView getDiscussionTabView();

	ForumWidget getForumWidget();

	DockerTabView getDockerTabView();

	DockerRepoListWidget getDockerRepoListWidget();

	Breadcrumb getBreadcrumb();

	SynapseClientAsync getSynapseClientAsync();

	SynapseJavascriptClient getSynapseJavascriptClient();

	StuAlert getStuAlert();

	FilesTabView getFilesTabView();

	FileTitleBar getFileTitleBar();

	BasicTitleBar getBasicTitleBar();

	EntityMetadata getEntityMetadata();

	FilesBrowser getFilesBrowser();

	WikiPageWidget getWikiPageWidget();

	DiscussionThreadListWidget getDiscussionThreadListWidget();

	ChallengeTabView getChallengeTabView();

	ChallengeWidget getChallengeWidget();

	TablesTabView getTablesTabView();

	QueryTokenProvider getQueryTokenProvider();

	SettingsPresenter getSettingsPresenter();

	AccessControlListModalWidget getAccessControlListModalWidget();

	RenameEntityModalWidget getRenameEntityModalWidget();

	EditFileMetadataModalWidget getEditFileMetadataModalWidget();

	EditProjectMetadataModalWidget getEditProjectMetadataModalWidget();

	EntityFinder getEntityFinder();

	UploadDialogWidget getUploadDialogWidget();

	WikiMarkdownEditor getWikiMarkdownEditor();

	ProvenanceEditorWidget getProvenanceEditorWidget();

	StorageLocationWidget getStorageLocationWidget();

	EvaluationEditorModal getEvaluationEditorModal();

	SelectTeamModal getSelectTeamModal();

	ApproveUserAccessModal getApproveUserAccessModal();

	ACTRevokeUserAccessModal getACTRevokeUserAccessModal();

	ChallengeClientAsync getChallengeClientAsync();

	UserProfileClientAsync getUserProfileClientAsync();

	DataAccessClientAsync getDataAccessClientAsync();

	MultipartFileUploadClientAsync getMultipartFileUploadClientAsync();

	DiscussionForumClientAsync getDiscussionForumClientAsync();

	DockerClientAsync getDockerClientAsync();

	JiraClientAsync getJiraClientAsync();

	LinkedInServiceAsync getLinkedInServiceAsync();

	StackConfigServiceAsync getStackConfigServiceAsync();

	SubscriptionClientAsync getSubscriptionClientAsync();

	UserAccountServiceAsync getUserAccountServiceAsync();

	EntityIdCellRendererImpl getEntityIdCellRenderer();

	UserIdCellRendererImpl getUserIdCellRenderer();

	CreateDataAccessRequestWizard getCreateDataAccessRequestWizard();

	ManagedACTAccessRequirementWidget getManagedACTAccessRequirementWidget();

	ACTAccessRequirementWidget getACTAccessRequirementWidget();

	LockAccessRequirementWidget getLockAccessRequirementWidget();

	TermsOfUseAccessRequirementWidget getTermsOfUseAccessRequirementWidget();

	FileHandleWidget getFileHandleWidget();

	CreateAccessRequirementWizard getCreateAccessRequirementWizard();

	ProfileCertifiedValidatedWidget getProfileCertifiedValidatedWidget();

	ACTDataAccessSubmissionWidget getACTDataAccessSubmissionWidget();

	OpenSubmissionWidget getOpenSubmissionWidget();

	DateTimeUtils getDateTimeUtils();

	AccessorGroupWidget getAccessorGroupWidget();

	AccessRequirementWidget getAccessRequirementWidget();

	SelfSignAccessRequirementWidget getSelfSignAccessRequirementWidget();

	SubjectWidget getSubjectWidget();

	TeamMemberRowWidget getTeamMemberRowWidget();
  
	RequestBuilderWrapper getRequestBuilder();
}
