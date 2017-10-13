package org.sagebionetworks.web.client;

import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.presenter.ACTAccessApprovalsPresenter;
import org.sagebionetworks.web.client.presenter.ACTDataAccessSubmissionDashboardPresenter;
import org.sagebionetworks.web.client.presenter.ACTDataAccessSubmissionsPresenter;
import org.sagebionetworks.web.client.presenter.ACTPresenter;
import org.sagebionetworks.web.client.presenter.AccessRequirementsPresenter;
import org.sagebionetworks.web.client.presenter.AccountPresenter;
import org.sagebionetworks.web.client.presenter.BulkPresenterProxy;
import org.sagebionetworks.web.client.presenter.CertificatePresenter;
import org.sagebionetworks.web.client.presenter.ChallengeOverviewPresenter;
import org.sagebionetworks.web.client.presenter.ChangeUsernamePresenter;
import org.sagebionetworks.web.client.presenter.ComingSoonPresenter;
import org.sagebionetworks.web.client.presenter.DownPresenter;
import org.sagebionetworks.web.client.presenter.EntityPresenter;
import org.sagebionetworks.web.client.presenter.ErrorPresenter;
import org.sagebionetworks.web.client.presenter.HelpPresenter;
import org.sagebionetworks.web.client.presenter.HomePresenter;
import org.sagebionetworks.web.client.presenter.LoginPresenter;
import org.sagebionetworks.web.client.presenter.MapPresenter;
import org.sagebionetworks.web.client.presenter.NewAccountPresenter;
import org.sagebionetworks.web.client.presenter.PeopleSearchPresenter;
import org.sagebionetworks.web.client.presenter.PresenterProxy;
import org.sagebionetworks.web.client.presenter.ProfilePresenter;
import org.sagebionetworks.web.client.presenter.ProjectsHomePresenter;
import org.sagebionetworks.web.client.presenter.QuestionContainerWidget;
import org.sagebionetworks.web.client.presenter.QuizPresenter;
import org.sagebionetworks.web.client.presenter.SearchPresenter;
import org.sagebionetworks.web.client.presenter.SettingsPresenter;
import org.sagebionetworks.web.client.presenter.SignedTokenPresenter;
import org.sagebionetworks.web.client.presenter.SubscriptionPresenter;
import org.sagebionetworks.web.client.presenter.SynapseForumPresenter;
import org.sagebionetworks.web.client.presenter.SynapseStandaloneWikiPresenter;
import org.sagebionetworks.web.client.presenter.SynapseWikiPresenter;
import org.sagebionetworks.web.client.presenter.TeamPresenter;
import org.sagebionetworks.web.client.presenter.TeamSearchPresenter;
import org.sagebionetworks.web.client.presenter.TrashPresenter;
import org.sagebionetworks.web.client.presenter.users.PasswordResetPresenter;
import org.sagebionetworks.web.client.presenter.users.RegisterAccountPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.FileHandleWidget;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.RadioWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.ACTAccessRequirementWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.AccessRequirementWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.LockAccessRequirementWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.ManagedACTAccessRequirementWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.SelfSignAccessRequirementWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.SubjectWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.TermsOfUseAccessRequirementWidget;
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
import org.sagebionetworks.web.client.widget.entity.ChallengeBadge;
import org.sagebionetworks.web.client.widget.entity.EditFileMetadataModalWidget;
import org.sagebionetworks.web.client.widget.entity.EditProjectMetadataModalWidget;
import org.sagebionetworks.web.client.widget.entity.EntityListRowBadge;
import org.sagebionetworks.web.client.widget.entity.EntityMetadata;
import org.sagebionetworks.web.client.widget.entity.EntityTreeItem;
import org.sagebionetworks.web.client.widget.entity.FileHistoryRowView;
import org.sagebionetworks.web.client.widget.entity.FileHistoryWidget;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.client.widget.entity.MarkdownWidget;
import org.sagebionetworks.web.client.widget.entity.ModifiedCreatedByWidget;
import org.sagebionetworks.web.client.widget.entity.MoreTreeItem;
import org.sagebionetworks.web.client.widget.entity.PreviewWidget;
import org.sagebionetworks.web.client.widget.entity.ProjectBadge;
import org.sagebionetworks.web.client.widget.entity.RegisterTeamDialog;
import org.sagebionetworks.web.client.widget.entity.RenameEntityModalWidget;
import org.sagebionetworks.web.client.widget.entity.TutorialWizard;
import org.sagebionetworks.web.client.widget.entity.WikiMarkdownEditor;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.entity.act.ACTRevokeUserAccessModal;
import org.sagebionetworks.web.client.widget.entity.act.ApproveUserAccessModal;
import org.sagebionetworks.web.client.widget.entity.act.UserBadgeItem;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationEditor;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import org.sagebionetworks.web.client.widget.entity.browse.EntityTreeBrowser;
import org.sagebionetworks.web.client.widget.entity.browse.FilesBrowser;
import org.sagebionetworks.web.client.widget.entity.controller.EntityActionController;
import org.sagebionetworks.web.client.widget.entity.controller.EntityRefProvEntryView;
import org.sagebionetworks.web.client.widget.entity.controller.ProvenanceEditorWidget;
import org.sagebionetworks.web.client.widget.entity.controller.ProvenanceListWidget;
import org.sagebionetworks.web.client.widget.entity.controller.StorageLocationWidget;
import org.sagebionetworks.web.client.widget.entity.controller.StuAlert;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.controller.URLProvEntryView;
import org.sagebionetworks.web.client.widget.entity.download.UploadDialogWidget;
import org.sagebionetworks.web.client.widget.entity.download.Uploader;
import org.sagebionetworks.web.client.widget.entity.editor.APITableColumnConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.APITableConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.AttachmentConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.BookmarkConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.ButtonLinkConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.CytoscapeConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.EntityListConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.ImageConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.ImageLinkConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.LeaderboardConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.LinkConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.PlotlyConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.PreviewConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.ProvenanceConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.QueryTableConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.ReferenceConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.ShinySiteConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.TabbedTableConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.TableQueryResultWikiEditor;
import org.sagebionetworks.web.client.widget.entity.editor.UserTeamConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.VideoConfigEditor;
import org.sagebionetworks.web.client.widget.entity.file.BasicTitleBar;
import org.sagebionetworks.web.client.widget.entity.file.FileTitleBar;
import org.sagebionetworks.web.client.widget.entity.file.Md5Link;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableColumnRendererCancelControl;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableColumnRendererDate;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableColumnRendererEntityIdAnnotations;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableColumnRendererLink;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableColumnRendererNone;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableColumnRendererSynapseID;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableColumnRendererUserId;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.AttachmentPreviewWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.BookmarkWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.ButtonLinkWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.CancelControlWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.ChallengeParticipantsWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.ChallengeTeamsWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.CytoscapeWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.EmptyWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.EntityListWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.ImageWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.PlotlyWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.ReferenceWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.RegisterChallengeTeamWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.ShinySiteWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.SubmitToEvaluationWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.SynapseTableFormWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.TableOfContentsWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.TeamMemberCountWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.TeamMemberRowWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.TeamMembersWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.VideoWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiFilesPreviewWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesWidget;
import org.sagebionetworks.web.client.widget.entity.tabs.ChallengeTabView;
import org.sagebionetworks.web.client.widget.entity.tabs.DiscussionTabView;
import org.sagebionetworks.web.client.widget.entity.tabs.DockerTabView;
import org.sagebionetworks.web.client.widget.entity.tabs.FilesTabView;
import org.sagebionetworks.web.client.widget.entity.tabs.TablesTabView;
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
import org.sagebionetworks.web.client.widget.table.v2.results.QueryResultEditorWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.RowWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.SortableTableHeader;
import org.sagebionetworks.web.client.widget.table.v2.results.StaticTableHeader;
import org.sagebionetworks.web.client.widget.table.v2.results.TablePageWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.TableQueryResultWikiWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.BooleanCellEditor;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.BooleanFormCellEditor;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.DateCellEditor;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.DateCellRenderer;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.DoubleCellEditor;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EntityIdCellEditor;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EntityIdCellRenderer;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EntityIdCellRendererImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EnumCellEditor;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EnumFormCellEditor;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.FileCellEditor;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.FileCellRenderer;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.IntegerCellEditor;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.LinkCellRenderer;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.StringEditorCell;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.StringRendererCell;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.UserIdCellEditor;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.UserIdCellRenderer;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.UserIdCellRendererImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.facets.FacetColumnResultDateRangeWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.facets.FacetColumnResultRangeWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.facets.FacetColumnResultSliderRangeWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.facets.FacetColumnResultValuesWidget;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRowEditorWidget;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRowViewer;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsView;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsWidget;
import org.sagebionetworks.web.client.widget.table.v2.schema.ImportTableViewColumnsButton;
import org.sagebionetworks.web.client.widget.team.BigTeamBadge;
import org.sagebionetworks.web.client.widget.team.JoinTeamConfigEditor;
import org.sagebionetworks.web.client.widget.team.JoinTeamWidget;
import org.sagebionetworks.web.client.widget.team.SelectTeamModal;
import org.sagebionetworks.web.client.widget.team.TeamBadge;
import org.sagebionetworks.web.client.widget.team.UserTeamBadge;
import org.sagebionetworks.web.client.widget.upload.FileHandleLink;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.client.widget.user.UserGroupListWidget;
import org.sagebionetworks.web.client.widget.verification.VerificationSubmissionModalViewImpl;
import org.sagebionetworks.web.client.widget.verification.VerificationSubmissionRowViewImpl;
import org.sagebionetworks.web.client.widget.verification.VerificationSubmissionWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;
import com.google.gwt.user.client.rpc.XsrfTokenServiceAsync;

/**
 * The root portal dependency injection root.
 * 
 * @author jmhill
 *
 */
@GinModules(PortalGinModule.class)
public interface PortalGinInjector extends Ginjector {

	public BulkPresenterProxy getBulkPresenterProxy();
	
	public GlobalApplicationState getGlobalApplicationState();
	
	public PresenterProxy<HomePresenter, Home> getHomePresenter();
	
	public EntityPresenter getEntityPresenter();
	
	public ProjectsHomePresenter getProjectsHomePresenter();
	
	public LoginPresenter getLoginPresenter();
	
	public AuthenticationController getAuthenticationController();
	
	public PasswordResetPresenter getPasswordResetPresenter();
	
	public RegisterAccountPresenter getRegisterAccountPresenter();

	public ProfilePresenter getProfilePresenter();

	public ComingSoonPresenter getComingSoonPresenter();
	
	public ChallengeOverviewPresenter getChallengeOverviewPresenter();
	
	public HelpPresenter getHelpPresenter();
	
	public SearchPresenter getSearchPresenter();
	
	public SynapseWikiPresenter getSynapseWikiPresenter();
	
	public DownPresenter getDownPresenter();
	
	public TeamPresenter getTeamPresenter();
	
	public MapPresenter getMapPresenter();
	
	public QuizPresenter getQuizPresenter();
	
	public CertificatePresenter getCertificatePresenter();
	
	public AccountPresenter getAccountPresenter();
	
	public NewAccountPresenter getNewAccountPresenter();
	
	public SignedTokenPresenter getSignedTokenPresenter();
	
	public ErrorPresenter getErrorPresenter();
	
	public ChangeUsernamePresenter getChangeUsernamePresenter();
	
	public TrashPresenter getTrashPresenter();
	
	public TeamSearchPresenter getTeamSearchPresenter();
	
	public PeopleSearchPresenter getPeopleSearchPresenter();
	
	public SynapseStandaloneWikiPresenter getSynapseStandaloneWikiPresenter();
	
	public EventBus getEventBus();
	
	public JiraURLHelper getJiraURLHelper();
		
	public MarkdownWidget getMarkdownWidget();
	
	public ActionMenuWidget createActionMenuWidget();
	
	public EntityActionController createEntityActionController();
	public ACTPresenter getACTPresenter();
	public AccessRequirementsPresenter getAccessRequirementsPresenter();
	public ACTDataAccessSubmissionsPresenter getACTDataAccessSubmissionsPresenter();
	public ACTDataAccessSubmissionDashboardPresenter getACTDataAccessSubmissionDashboardPresenter();
	public SynapseForumPresenter getSynapseForumPresenter();
	public SubscriptionPresenter getSubscriptionPresenter();
	public ACTAccessApprovalsPresenter getACTAccessApprovalsPresenter();
	
	/*
	 *  Markdown Widgets
	 */
	////// Editors
	public BookmarkConfigEditor getBookmarkConfigEditor();
	public ReferenceConfigEditor getReferenceConfigEditor();
	public ProvenanceConfigEditor getProvenanceConfigEditor();
	public ImageConfigEditor getImageConfigEditor();
	public ImageLinkConfigEditor getImageLinkConfigEditor();
	public AttachmentConfigEditor getAttachmentConfigEditor();
	public LinkConfigEditor getLinkConfigEditor();
	public APITableConfigEditor getSynapseAPICallConfigEditor();
	public QueryTableConfigEditor getSynapseQueryConfigEditor();
	public LeaderboardConfigEditor getLeaderboardConfigEditor();
	public TabbedTableConfigEditor getTabbedTableConfigEditor();
	public EntityTreeBrowser getEntityTreeBrowser();
	public EntityListConfigEditor getEntityListConfigEditor();
	public ShinySiteConfigEditor getShinySiteConfigEditor();
	public ButtonLinkConfigEditor getButtonLinkConfigEditor();
	public UserTeamConfigEditor getUserTeamConfigEditor();
	public VideoConfigEditor getVideoConfigEditor();
	public TableQueryResultWikiEditor getSynapseTableQueryResultEditor();
	public PreviewConfigEditor getPreviewConfigEditor();
	public BiodallianceEditor getBiodallianceEditor();
	public BiodallianceSourceEditor getBiodallianceSourceEditor();
	public CytoscapeConfigEditor getCytoscapeConfigEditor();
	public PlotlyConfigEditor getPlotlyConfigEditor();
	
	////// Renderers
	public BookmarkWidget getBookmarkRenderer();
	public ReferenceWidget getReferenceRenderer();
	public TutorialWizard getTutorialWidgetRenderer();
	public ProvenanceWidget getProvenanceRenderer();
	public AdministerEvaluationsList getAdministerEvaluationsList();
	public ImageWidget getImageRenderer();
	public AttachmentPreviewWidget getAttachmentPreviewRenderer();
	public APITableWidget getSynapseAPICallRenderer();
	public TableOfContentsWidget getTableOfContentsRenderer();
	public WikiSubpagesWidget getWikiSubpagesRenderer();
	public WikiFilesPreviewWidget getWikiFilesPreviewRenderer();
	public EntityListWidget getEntityListRenderer();
	public ShinySiteWidget getShinySiteRenderer();
	public JoinTeamWidget getJoinTeamWidget();
	public SubmitToEvaluationWidget getEvaluationSubmissionWidget();
	public EmptyWidget getEmptyWidget();
	public ButtonLinkWidget getButtonLinkWidget();
	public VideoWidget getVideoWidget();
	public TableQueryResultWikiWidget getSynapseTableQueryResultWikiWidget();
	public RegisterChallengeTeamWidget getRegisterChallengeTeamWidget();
	public ChallengeTeamsWidget getChallengeTeamsWidget();
	public ChallengeParticipantsWidget getChallengeParticipantsWidget();
	public BiodallianceWidget getBiodallianceRenderer();
	public CytoscapeWidget getCytoscapeRenderer();
	public SynapseTableFormWidget getSynapseTableFormWidget();
	public TeamMembersWidget getTeamMembersWidget();
	public TeamMemberCountWidget getTeamMemberCountWidget();
	public PlotlyWidget getPlotlyWidget();
	public LazyLoadWikiWidgetWrapper getLazyLoadWikiWidgetWrapper();
	//////API Table Column Editor
	public APITableColumnConfigView getAPITableColumnConfigView();
	
	//////API Table Column Renderers
	public APITableColumnRendererNone getAPITableColumnRendererNone();
	public APITableColumnRendererUserId getAPITableColumnRendererUserId();
	public APITableColumnRendererDate getAPITableColumnRendererDate();
	public APITableColumnRendererLink getAPITableColumnRendererLink();
	public APITableColumnRendererSynapseID getAPITableColumnRendererSynapseID();
	public APITableColumnRendererEntityIdAnnotations getAPITableColumnRendererEntityAnnotations();
	public APITableColumnRendererCancelControl getAPITableColumnRendererCancelControl();
	
	// Other widgets
	public UserBadge getUserBadgeWidget();
	public VersionTimer getVersionTimer();
	public Md5Link getMd5Link();
	public QuestionContainerWidget getQuestionContainerWidget();
	public SynapseAlert getSynapseAlertWidget();
	public EntityRefProvEntryView getEntityRefEntry();
	public URLProvEntryView getURLEntry();
	public ProvenanceListWidget getProvenanceListWidget();
	public PreviewWidget getPreviewWidget();
	public UserBadgeItem getUserBadgeItem();
	
	// TableEntity V2
	public ColumnModelsView createNewColumnModelsView();
	public ImportTableViewColumnsButton getImportTableViewColumnsButton();
	public ColumnModelsWidget createNewColumnModelsWidget();
	public ColumnModelTableRowViewer createNewColumnModelTableRowViewer();
	public ColumnModelTableRowEditorWidget createColumnModelEditorWidget();
	public TableEntityWidget createNewTableEntityWidget();
	public RowWidget createRowWidget();
	public TablePageWidget createNewTablePageWidget();
	public QueryResultEditorWidget createNewQueryResultEditorWidget();
	
	// TableEntity V2 cells
	public StringRendererCell createStringRendererCell();
	public StringEditorCell createStringEditorCell();
	public EntityIdCellEditor createEntityIdCellEditor();
	public EntityIdCellRenderer createEntityIdCellRenderer();
	public EnumCellEditor createEnumCellEditor();
	public EnumFormCellEditor createEnumFormCellEditor();
	public BooleanCellEditor createBooleanCellEditor();
	public BooleanFormCellEditor createBooleanFormCellEditor();
	public DateCellEditor createDateCellEditor();
	public DateCellRenderer createDateCellRenderer();
	public DoubleCellEditor createDoubleCellEditor();
	public IntegerCellEditor createIntegerCellEditor();
	public LinkCellRenderer createLinkCellRenderer();
	public FileCellEditor createFileCellEditor();
	public FileCellRenderer createFileCellRenderer();
	public UserIdCellRenderer createUserIdCellRenderer();
	public UserIdCellEditor createUserIdCellEditor();
	// Asynchronous
	public AsynchronousProgressWidget creatNewAsynchronousProgressWidget();
	
	public UserTeamBadge getUserTeamBadgeWidget();
	public TeamBadge getTeamBadgeWidget();
	public BigTeamBadge getBigTeamBadgeWidget();
	
	public ChallengeBadge getChallengeBadgeWidget();
	
	public ProjectBadge getProjectBadgeWidget();
	public EntityTreeItem getEntityTreeItemWidget();
	public MoreTreeItem getMoreTreeWidget();
	public UserGroupListWidget getUserGroupListWidget();
	public UserGroupSuggestionProvider getUserGroupSuggestOracleImpl();
	
	public TableListWidget getTableListWidget();
	public Uploader getUploaderWidget();
	public CookieProvider getCookieProvider();

	public KeyboardNavigationHandler createKeyboardNavigationHandler();
	public Header getHeader();
	public Footer getFooter();
	
	public SortableTableHeader createSortableTableHeader();
	public StaticTableHeader createStaticTableHeader();
	public EvaluationSubmitter getEvaluationSubmitter();
	public RegisterTeamDialog getRegisterTeamDialog();
	public AnnotationEditor getAnnotationEditor();
	public FileHistoryRowView getFileHistoryRow();
	public FileHistoryWidget getFileHistoryWidget();
	
	public JoinTeamConfigEditor getJoinTeamConfigEditor();
	public ModifiedCreatedByWidget getModifiedCreatedByWidget();
	public FileHandleLink getFileHandleLink();
	public VerificationSubmissionWidget getVerificationSubmissionWidget();
	public VerificationSubmissionModalViewImpl getVerificationSubmissionModalViewImpl();
	public VerificationSubmissionRowViewImpl getVerificationSubmissionRowViewImpl();

	// discussion
	public DiscussionThreadListItemWidget createThreadListItemWidget();
	public ReplyWidget createReplyWidget();
	
	public TopicRowWidget getTopicRowWidget();
	public RefreshAlert getRefreshAlert();
	public ReplyCountAlert getReplyCountAlert();
	public DiscussionThreadCountAlert getDiscussionThreadCountAlert();

	// docker
	public DockerRepoWidget createNewDockerRepoWidget();
	public DockerCommitRowWidget createNewDockerCommitRowWidget();
	
	public LoginWidget getLoginWidget();
	public FileClientsHelp getFileClientsHelp();
	public LoadMoreWidgetContainer getLoadMoreProjectsWidgetContainer();
	public RadioWidget createNewRadioWidget();
	public EntityListRowBadge getEntityListRowBadge();
	public CancelControlWidget getCancelControlWidget();
	public FacetColumnResultSliderRangeWidget getFacetColumnResultSliderRangeWidget();
	public FacetColumnResultRangeWidget getFacetColumnResultRangeWidget();
	public FacetColumnResultValuesWidget getFacetColumnResultValuesWidget();
	public FacetColumnResultDateRangeWidget getFacetColumnResultDateRangeWidget();
	
	public DiscussionTabView getDiscussionTabView();
	public ForumWidget getForumWidget();
	public DockerTabView getDockerTabView();
	public DockerRepoListWidget getDockerRepoListWidget();
	public Breadcrumb getBreadcrumb();
	public SynapseClientAsync getSynapseClientAsync();
	public SynapseJavascriptClient getSynapseJavascriptClient();
	public StuAlert getStuAlert();
	public FilesTabView getFilesTabView();
	public FileTitleBar getFileTitleBar();
	public BasicTitleBar getBasicTitleBar();
	public EntityMetadata getEntityMetadata();
	public FilesBrowser getFilesBrowser();
	public WikiPageWidget getWikiPageWidget();
	public DiscussionThreadListWidget getDiscussionThreadListWidget();
	public ChallengeTabView getChallengeTabView();
	public ChallengeWidget getChallengeWidget();
	public TablesTabView getTablesTabView();
	public QueryTokenProvider getQueryTokenProvider();
	public SettingsPresenter getSettingsPresenter();
	public AccessControlListModalWidget getAccessControlListModalWidget();
	public RenameEntityModalWidget getRenameEntityModalWidget();
	public EditFileMetadataModalWidget getEditFileMetadataModalWidget();
	public EditProjectMetadataModalWidget getEditProjectMetadataModalWidget();
	public EntityFinder getEntityFinder();
	public UploadDialogWidget getUploadDialogWidget();
	public WikiMarkdownEditor getWikiMarkdownEditor();
	public ProvenanceEditorWidget getProvenanceEditorWidget();
	public StorageLocationWidget getStorageLocationWidget();
	public EvaluationEditorModal getEvaluationEditorModal();
	public SelectTeamModal getSelectTeamModal();
	public ApproveUserAccessModal getApproveUserAccessModal();
	public ACTRevokeUserAccessModal getACTRevokeUserAccessModal();
	public ChallengeClientAsync getChallengeClientAsync();
	public UserProfileClientAsync getUserProfileClientAsync();
	public DataAccessClientAsync getDataAccessClientAsync();
	public MultipartFileUploadClientAsync getMultipartFileUploadClientAsync();
	public DiscussionForumClientAsync getDiscussionForumClientAsync();
	public DockerClientAsync getDockerClientAsync();
	public JiraClientAsync getJiraClientAsync();
	public LinkedInServiceAsync getLinkedInServiceAsync();
	public StackConfigServiceAsync getStackConfigServiceAsync();
	public SubscriptionClientAsync getSubscriptionClientAsync();
	public UserAccountServiceAsync getUserAccountServiceAsync();
	public EntityIdCellRendererImpl getEntityIdCellRenderer();
	public UserIdCellRendererImpl getUserIdCellRenderer();
	
	public CreateDataAccessRequestWizard getCreateDataAccessRequestWizard();
	public ManagedACTAccessRequirementWidget getManagedACTAccessRequirementWidget();
	public ACTAccessRequirementWidget getACTAccessRequirementWidget();
	public LockAccessRequirementWidget getLockAccessRequirementWidget();
	public TermsOfUseAccessRequirementWidget getTermsOfUseAccessRequirementWidget();
	public FileHandleWidget getFileHandleWidget();
	public CreateAccessRequirementWizard getCreateAccessRequirementWizard();
	public ProfileCertifiedValidatedWidget getProfileCertifiedValidatedWidget();
	public ACTDataAccessSubmissionWidget getACTDataAccessSubmissionWidget();
	public OpenSubmissionWidget getOpenSubmissionWidget();
	public DateTimeUtils getDateTimeUtils();
	public AccessorGroupWidget getAccessorGroupWidget();
	public AccessRequirementWidget getAccessRequirementWidget();
	public SelfSignAccessRequirementWidget getSelfSignAccessRequirementWidget();
	
	public SubjectWidget getSubjectWidget();
	public TeamMemberRowWidget getTeamMemberRowWidget();
	public RequestBuilderWrapper getRequestBuilder();
}
