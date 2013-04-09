package org.sagebionetworks.web.client;

import org.sagebionetworks.web.client.presenter.BCCOverviewPresenterProxy;
import org.sagebionetworks.web.client.presenter.ComingSoonPresenterProxy;
import org.sagebionetworks.web.client.presenter.EntityPresenterProxy;
import org.sagebionetworks.web.client.presenter.GovernancePresenterProxy;
import org.sagebionetworks.web.client.presenter.HomePresenterProxy;
import org.sagebionetworks.web.client.presenter.LoginPresenterProxy;
import org.sagebionetworks.web.client.presenter.ProfilePresenterProxy;
import org.sagebionetworks.web.client.presenter.ProjectsHomePresenterProxy;
import org.sagebionetworks.web.client.presenter.SearchPresenterProxy;
import org.sagebionetworks.web.client.presenter.SettingsPresenterProxy;
import org.sagebionetworks.web.client.presenter.SynapseWikiPresenterProxy;
import org.sagebionetworks.web.client.presenter.WikiPresenterProxy;
import org.sagebionetworks.web.client.presenter.users.PasswordResetPresenterProxy;
import org.sagebionetworks.web.client.presenter.users.RegisterAccountPresenterProxy;
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
import org.sagebionetworks.web.client.widget.entity.renderer.OldImageWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.ShinySiteWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.TableOfContentsWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiFilesPreviewWidget;
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
	
	public HomePresenterProxy getHomePresenter();

	public EntityPresenterProxy getEntityPresenter();
	
	public ProjectsHomePresenterProxy getProjectsHomePresenter();
	
	public LoginPresenterProxy getLoginPresenter();
	
	public AuthenticationController getAuthenticationController();
	
	public PasswordResetPresenterProxy getPasswordResetPresenter();
	
	public RegisterAccountPresenterProxy getRegisterAccountPresenter();

	public ProfilePresenterProxy getProfilePresenter();

	public SettingsPresenterProxy getSettingsPresenter();
	
	public ComingSoonPresenterProxy getComingSoonPresenter();
	
	public BCCOverviewPresenterProxy getBCCOverviewPresenter();
	
	public GovernancePresenterProxy getGovernancePresenter();
	
	public SearchPresenterProxy getSearchPresenter();
	
	public SynapseWikiPresenterProxy getSynapseWikiPresenter();
	
	public WikiPresenterProxy getWikiPresenter();
	
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
	public WikiFilesPreviewWidget getWikiFilesPreviewRenderer();
	public EntityListWidget getEntityListRenderer();
	public ShinySiteWidget getShinySiteRenderer(); 
	
	//////API Table Column Renderers
	public APITableColumnRendererNone getAPITableColumnRendererNone();
	public APITableColumnRendererUserId getAPITableColumnRendererUserId();
	public APITableColumnRendererDate getAPITableColumnRendererDate();
	public APITableColumnRendererSynapseID getAPITableColumnRendererSynapseID();
	public APITableColumnRendererEntityIdAnnotations getAPITableColumnRendererEntityAnnotations();
	
	// Other widgets
	public UserBadge getUserBadgeWidget();
	
}
