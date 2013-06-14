package org.sagebionetworks.web.client;

import org.sagebionetworks.web.client.place.Challenges;
import org.sagebionetworks.web.client.place.ComingSoon;
import org.sagebionetworks.web.client.place.Down;
import org.sagebionetworks.web.client.place.Governance;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.ProjectsHome;
import org.sagebionetworks.web.client.place.Search;
import org.sagebionetworks.web.client.place.Settings;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Wiki;
import org.sagebionetworks.web.client.place.WikiPlace;
import org.sagebionetworks.web.client.place.users.PasswordReset;
import org.sagebionetworks.web.client.place.users.RegisterAccount;
import org.sagebionetworks.web.client.presenter.ChallengeOverviewPresenter;
import org.sagebionetworks.web.client.presenter.ComingSoonPresenter;
import org.sagebionetworks.web.client.presenter.DownPresenter;
import org.sagebionetworks.web.client.presenter.EntityPresenter;
import org.sagebionetworks.web.client.presenter.GovernancePresenter;
import org.sagebionetworks.web.client.presenter.HomePresenter;
import org.sagebionetworks.web.client.presenter.LoginPresenter;
import org.sagebionetworks.web.client.presenter.PresenterProxy;
import org.sagebionetworks.web.client.presenter.ProfilePresenter;
import org.sagebionetworks.web.client.presenter.ProjectsHomePresenter;
import org.sagebionetworks.web.client.presenter.SearchPresenter;
import org.sagebionetworks.web.client.presenter.SettingsPresenter;
import org.sagebionetworks.web.client.presenter.SynapseWikiPresenter;
import org.sagebionetworks.web.client.presenter.WikiPresenter;
import org.sagebionetworks.web.client.presenter.users.PasswordResetPresenter;
import org.sagebionetworks.web.client.presenter.users.RegisterAccountPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.client.widget.entity.browse.EntityTreeBrowser;
import org.sagebionetworks.web.client.widget.entity.editor.APITableConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.AttachmentConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.EntityListConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.ImageConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.LinkConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.OldImageConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.ProvenanceConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.ShinySiteConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.TabbedTableConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.YouTubeConfigEditor;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableColumnRendererDate;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableColumnRendererEntityIdAnnotations;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableColumnRendererNone;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableColumnRendererSynapseID;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableColumnRendererUserId;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.AttachmentPreviewWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.EntityListWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.ImageWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.JoinWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.OldImageWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.ShinySiteWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.TableOfContentsWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiFilesPreviewWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.YouTubeWidget;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidget;
import org.sagebionetworks.web.client.widget.user.UserBadge;

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

	public GlobalApplicationState getGlobalApplicationState();
	
	public PresenterProxy<HomePresenter, Home> getHomePresenter();

	public PresenterProxy<EntityPresenter, Synapse> getEntityPresenter();
	
	public PresenterProxy<ProjectsHomePresenter, ProjectsHome> getProjectsHomePresenter();
	
	public PresenterProxy<LoginPresenter, LoginPlace> getLoginPresenter();
	
	public AuthenticationController getAuthenticationController();
	
	public PresenterProxy<PasswordResetPresenter, PasswordReset> getPasswordResetPresenter();
	
	public PresenterProxy<RegisterAccountPresenter, RegisterAccount> getRegisterAccountPresenter();

	public PresenterProxy<ProfilePresenter, Profile> getProfilePresenter();

	public PresenterProxy<SettingsPresenter, Settings> getSettingsPresenter();
	
	public PresenterProxy<ComingSoonPresenter, ComingSoon> getComingSoonPresenter();
	
	public PresenterProxy<ChallengeOverviewPresenter, Challenges> getChallengeOverviewPresenter();
	
	public PresenterProxy<GovernancePresenter, Governance> getGovernancePresenter();
	
	public PresenterProxy<SearchPresenter, Search> getSearchPresenter();
	
	public PresenterProxy<SynapseWikiPresenter, Wiki> getSynapseWikiPresenter();
	
	public PresenterProxy<WikiPresenter, WikiPlace> getWikiPresenter();
	
	public PresenterProxy<DownPresenter, Down> getDownPresenter();
	
	public EventBus getEventBus();
	
	public JiraURLHelper getJiraURLHelper();


	/*
	 *  Markdown Widgets
	 */
	////// Editors
	public YouTubeConfigEditor getYouTubeConfigEditor();
	public ProvenanceConfigEditor getProvenanceConfigEditor();
	public OldImageConfigEditor getOldImageConfigEditor();
	public ImageConfigEditor getImageConfigEditor();
	public AttachmentConfigEditor getAttachmentConfigEditor();
	public LinkConfigEditor getLinkConfigEditor();
	public APITableConfigEditor getSynapseAPICallConfigEditor();
	public TabbedTableConfigEditor getTabbedTableConfigEditor();
	public EntityTreeBrowser getEntityTreeBrowser();
	public EntityListConfigEditor getEntityListConfigEditor();
	public ShinySiteConfigEditor getShinySiteConfigEditor();

	////// Renderers
	public YouTubeWidget getYouTubeRenderer();
	public ProvenanceWidget getProvenanceRenderer();
	public OldImageWidget getOldImageRenderer();
	public ImageWidget getImageRenderer();
	public AttachmentPreviewWidget getAttachmentPreviewRenderer();
	public APITableWidget getSynapseAPICallRenderer();
	public TableOfContentsWidget getTableOfContentsRenderer();
	public WikiSubpagesWidget getWikiSubpagesRenderer();
	public WikiFilesPreviewWidget getWikiFilesPreviewRenderer();
	public EntityListWidget getEntityListRenderer();
	public ShinySiteWidget getShinySiteRenderer();
	public JoinWidget getJoinWidget();
	
	//////API Table Column Renderers
	public APITableColumnRendererNone getAPITableColumnRendererNone();
	public APITableColumnRendererUserId getAPITableColumnRendererUserId();
	public APITableColumnRendererDate getAPITableColumnRendererDate();
	public APITableColumnRendererSynapseID getAPITableColumnRendererSynapseID();
	public APITableColumnRendererEntityIdAnnotations getAPITableColumnRendererEntityAnnotations();
	
	// Other widgets
	public UserBadge getUserBadgeWidget();
	
}
