package org.sagebionetworks.web.client;

import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.presenter.AccountPresenter;
import org.sagebionetworks.web.client.presenter.BulkPresenterProxy;
import org.sagebionetworks.web.client.presenter.CertificatePresenter;
import org.sagebionetworks.web.client.presenter.ChallengeOverviewPresenter;
import org.sagebionetworks.web.client.presenter.ChangeUsernamePresenter;
import org.sagebionetworks.web.client.presenter.ComingSoonPresenter;
import org.sagebionetworks.web.client.presenter.DownPresenter;
import org.sagebionetworks.web.client.presenter.EntityPresenter;
import org.sagebionetworks.web.client.presenter.HelpPresenter;
import org.sagebionetworks.web.client.presenter.HomePresenter;
import org.sagebionetworks.web.client.presenter.LoginPresenter;
import org.sagebionetworks.web.client.presenter.NewAccountPresenter;
import org.sagebionetworks.web.client.presenter.PeopleSearchPresenter;
import org.sagebionetworks.web.client.presenter.PresenterProxy;
import org.sagebionetworks.web.client.presenter.ProfilePresenter;
import org.sagebionetworks.web.client.presenter.ProjectsHomePresenter;
import org.sagebionetworks.web.client.presenter.QuizPresenter;
import org.sagebionetworks.web.client.presenter.SearchPresenter;
import org.sagebionetworks.web.client.presenter.SynapseWikiPresenter;
import org.sagebionetworks.web.client.presenter.TeamPresenter;
import org.sagebionetworks.web.client.presenter.TeamSearchPresenter;
import org.sagebionetworks.web.client.presenter.TrashPresenter;
import org.sagebionetworks.web.client.presenter.WikiPresenter;
import org.sagebionetworks.web.client.presenter.users.PasswordResetPresenter;
import org.sagebionetworks.web.client.presenter.users.RegisterAccountPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.asynch.JobTrackingWidget;
import org.sagebionetworks.web.client.widget.entity.AdministerEvaluationsList;
import org.sagebionetworks.web.client.widget.entity.EntityBadge;
import org.sagebionetworks.web.client.widget.entity.EntityTreeItem;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.client.widget.entity.MarkdownWidget;
import org.sagebionetworks.web.client.widget.entity.TutorialWizard;
import org.sagebionetworks.web.client.widget.entity.browse.EntityTreeBrowser;
import org.sagebionetworks.web.client.widget.entity.controller.EntityActionController;
import org.sagebionetworks.web.client.widget.entity.download.Uploader;
import org.sagebionetworks.web.client.widget.entity.editor.APITableConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.AttachmentConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.BookmarkConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.ButtonLinkConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.EntityListConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.ImageConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.LinkConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.OldImageConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.ProvenanceConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.QueryTableConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.ReferenceConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.ShinySiteConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.TabbedTableConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.UserTeamConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.VideoConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.YouTubeConfigEditor;
import org.sagebionetworks.web.client.widget.entity.file.Md5Link;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
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
import org.sagebionetworks.web.client.widget.entity.renderer.EmptyWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.EntityListWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.ImageWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.OldImageWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.ReferenceWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.ShinySiteWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.SubmitToEvaluationWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.TableOfContentsWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.VideoWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiFilesPreviewWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.YouTubeWidget;
import org.sagebionetworks.web.client.widget.pagination.BasicPaginationWidget;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidget;
import org.sagebionetworks.web.client.widget.table.KeyboardNavigationHandler;
import org.sagebionetworks.web.client.widget.table.TableListWidget;
import org.sagebionetworks.web.client.widget.table.v2.TableEntityWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryResultEditorWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.RowWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.TablePageWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.StringEditorCell;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.StringRendererCell;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRowEditor;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRowViewer;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsView;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsWidget;
import org.sagebionetworks.web.client.widget.team.BigTeamBadge;
import org.sagebionetworks.web.client.widget.team.JoinTeamWidget;
import org.sagebionetworks.web.client.widget.team.TeamBadge;
import org.sagebionetworks.web.client.widget.team.UserTeamBadge;
import org.sagebionetworks.web.client.widget.user.BigUserBadge;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.client.widget.user.UserGroupListWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;

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
	
	public WikiPresenter getWikiPresenter();
	
	public DownPresenter getDownPresenter();
	
	public TeamPresenter getTeamPresenter();
	
	public QuizPresenter getQuizPresenter();
	
	public CertificatePresenter getCertificatePresenter();
	
	public AccountPresenter getAccountPresenter();
	
	public NewAccountPresenter getNewAccountPresenter();
	
	public ChangeUsernamePresenter getChangeUsernamePresenter();
	
	public TrashPresenter getTrashPresenter();
	
	public TeamSearchPresenter getTeamSearchPresenter();
	
	public PeopleSearchPresenter getPeopleSearchPresenter();
	
	public EventBus getEventBus();
	
	public JiraURLHelper getJiraURLHelper();
		
	public MarkdownWidget getMarkdownWidget();
	
	public ActionMenuWidget createActionMenuWidget();
	
	public EntityActionController createEntityActionController();
	/*
	 *  Markdown Widgets
	 */
	////// Editors
	public BookmarkConfigEditor getBookmarkConfigEditor();
	public ReferenceConfigEditor getReferenceConfigEditor();
	public YouTubeConfigEditor getYouTubeConfigEditor();
	public ProvenanceConfigEditor getProvenanceConfigEditor();
	public OldImageConfigEditor getOldImageConfigEditor();
	public ImageConfigEditor getImageConfigEditor();
	public AttachmentConfigEditor getAttachmentConfigEditor();
	public LinkConfigEditor getLinkConfigEditor();
	public APITableConfigEditor getSynapseAPICallConfigEditor();
	public QueryTableConfigEditor getSynapseQueryConfigEditor();
	public TabbedTableConfigEditor getTabbedTableConfigEditor();
	public EntityTreeBrowser getEntityTreeBrowser();
	public EntityListConfigEditor getEntityListConfigEditor();
	public ShinySiteConfigEditor getShinySiteConfigEditor();
	public ButtonLinkConfigEditor getButtonLinkConfigEditor();
	public UserTeamConfigEditor getUserTeamConfigEditor();
	public VideoConfigEditor getVideoConfigEditor();
	
	////// Renderers
	public BookmarkWidget getBookmarkRenderer();
	public ReferenceWidget getReferenceRenderer();
	public YouTubeWidget getYouTubeRenderer();
	public TutorialWizard getTutorialWidgetRenderer();
	public ProvenanceWidget getProvenanceRenderer();
	public AdministerEvaluationsList getAdministerEvaluationsList();
	public OldImageWidget getOldImageRenderer();
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
	
	//////API Table Column Renderers
	public APITableColumnRendererNone getAPITableColumnRendererNone();
	public APITableColumnRendererUserId getAPITableColumnRendererUserId();
	public APITableColumnRendererDate getAPITableColumnRendererDate();
	public APITableColumnRendererLink getAPITableColumnRendererLink();
	public APITableColumnRendererSynapseID getAPITableColumnRendererSynapseID();
	public APITableColumnRendererEntityIdAnnotations getAPITableColumnRendererEntityAnnotations();
	
	// Other widgets
	public UserBadge getUserBadgeWidget();
	public BigUserBadge getBigUserBadgeWidget();
	public VersionTimer getVersionTimer();
	public Md5Link getMd5Link();
	
	// TableEntity V2
	public ColumnModelsView createNewColumnModelsView();
	public ColumnModelsWidget createNewColumnModelsWidget();
	public ColumnModelTableRowViewer createNewColumnModelTableRowViewer();
	public ColumnModelTableRowEditor createNewColumnModelTableRowEditor();
	public TableEntityWidget createNewTableEntityWidget();
	public RowWidget createRowWidget();
	public TablePageWidget createNewTablePageWidget();
	public QueryResultEditorWidget createNewQueryResultEditorWidget();
	
	// TableEntity V2 cells
	public StringRendererCell createStringRendererCell();
	public StringEditorCell createStringEditorCell();
		
	// Asynchronous
	public JobTrackingWidget creatNewAsynchronousProgressWidget();
	
	public UserTeamBadge getUserTeamBadgeWidget();
	public TeamBadge getTeamBadgeWidget();
	public BigTeamBadge getBigTeamBadgeWidget();
	
	public EntityBadge getEntityBadgeWidget();
	public EntityTreeItem getEntityTreeItemWidget();

	public UserGroupListWidget getUserGroupListWidget();
	
	public TableListWidget getTableListWidget();
	public Uploader getUploaderWidget();
	public CookieProvider getCookieProvider();

	public BasicPaginationWidget createBasicPaginationWidget();


	public KeyboardNavigationHandler createKeyboardNavigationHandler();
}
