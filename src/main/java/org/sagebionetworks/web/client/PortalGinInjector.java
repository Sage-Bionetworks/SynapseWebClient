package org.sagebionetworks.web.client;

import org.sagebionetworks.web.client.presenter.BCCOverviewPresenter;
import org.sagebionetworks.web.client.presenter.ComingSoonPresenter;
import org.sagebionetworks.web.client.presenter.EntityPresenter;
import org.sagebionetworks.web.client.presenter.GovernancePresenter;
import org.sagebionetworks.web.client.presenter.HomePresenter;
import org.sagebionetworks.web.client.presenter.LoginPresenter;
import org.sagebionetworks.web.client.presenter.ProfilePresenter;
import org.sagebionetworks.web.client.presenter.ProjectsHomePresenter;
import org.sagebionetworks.web.client.presenter.SearchPresenter;
import org.sagebionetworks.web.client.presenter.SettingsPresenter;
import org.sagebionetworks.web.client.presenter.WikiPresenter;
import org.sagebionetworks.web.client.presenter.users.PasswordResetPresenter;
import org.sagebionetworks.web.client.presenter.users.RegisterAccountPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.client.widget.entity.editor.APITableConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.ImageConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.LinkConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.ProvenanceConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.YouTubeConfigEditor;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableColumnRendererDate;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableColumnRendererEntityIdAnnotations;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableColumnRendererNone;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableColumnRendererSynapseID;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableColumnRendererUserId;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.ImageWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.YouTubeWidget;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidget;

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
	
	public HomePresenter getHomePresenter();

	public EntityPresenter getEntityPresenter();
	
	public ProjectsHomePresenter getProjectsHomePresenter();
	
	public LoginPresenter getLoginPresenter();
	
	public AuthenticationController getAuthenticationController();
	
	public PasswordResetPresenter getPasswordResetPresenter();
	
	public RegisterAccountPresenter getRegisterAccountPresenter();

	public ProfilePresenter getProfilePresenter();

	public SettingsPresenter getSettingsPresenter();
	
	public ComingSoonPresenter getComingSoonPresenter();
	
	public BCCOverviewPresenter getBCCOverviewPresenter();
	
	public GovernancePresenter getGovernancePresenter();
	
	public SearchPresenter getSearchPresenter();
	
	public BCCSignup getBCCSignup();
	
	public WikiPresenter getWikiPresenter();
	
	public EventBus getEventBus();
	
	public JiraURLHelper getJiraURLHelper();

	// Widgets
	////// Editors
	public YouTubeConfigEditor getYouTubeConfigEditor();
	public ProvenanceConfigEditor getProvenanceConfigEditor();
	public ImageConfigEditor getImageConfigEditor();
	public LinkConfigEditor getLinkConfigEditor();
	public APITableConfigEditor getSynapseAPICallConfigEditor();


	////// Renderers
	public YouTubeWidget getYouTubeRenderer();
	public ProvenanceWidget getProvenanceRenderer();
	public ImageWidget getImageRenderer();
	public APITableWidget getSynapseAPICallRenderer();
	
	//////API Table Column Renderers
	public APITableColumnRendererNone getAPITableColumnRendererNone();
	public APITableColumnRendererUserId getAPITableColumnRendererUserId();
	public APITableColumnRendererDate getAPITableColumnRendererDate();
	public APITableColumnRendererSynapseID getAPITableColumnRendererSynapseID();
	public APITableColumnRendererEntityIdAnnotations getAPITableColumnRendererEntityAnnotations();
}
