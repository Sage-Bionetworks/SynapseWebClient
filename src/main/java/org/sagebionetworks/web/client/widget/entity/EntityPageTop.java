package org.sagebionetworks.web.client.widget.entity;

import static org.sagebionetworks.repo.model.EntityBundle.ACCESS_REQUIREMENTS;
import static org.sagebionetworks.repo.model.EntityBundle.ACL;
import static org.sagebionetworks.repo.model.EntityBundle.ANNOTATIONS;
import static org.sagebionetworks.repo.model.EntityBundle.DOI;
import static org.sagebionetworks.repo.model.EntityBundle.ENTITY;
import static org.sagebionetworks.repo.model.EntityBundle.FILE_HANDLES;
import static org.sagebionetworks.repo.model.EntityBundle.PERMISSIONS;
import static org.sagebionetworks.repo.model.EntityBundle.ROOT_WIKI_ID;
import static org.sagebionetworks.repo.model.EntityBundle.TABLE_DATA;
import static org.sagebionetworks.repo.model.EntityBundle.UNMET_ACCESS_REQUIREMENTS;
import static org.sagebionetworks.web.client.widget.display.ProjectDisplayDialog.CHALLENGE;
import static org.sagebionetworks.web.client.widget.display.ProjectDisplayDialog.DISCUSSION;
import static org.sagebionetworks.web.client.widget.display.ProjectDisplayDialog.DOCKER;
import static org.sagebionetworks.web.client.widget.display.ProjectDisplayDialog.FILES;
import static org.sagebionetworks.web.client.widget.display.ProjectDisplayDialog.TABLES;
import static org.sagebionetworks.web.client.widget.display.ProjectDisplayDialog.WIKI;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.docker.DockerRepository;
import org.sagebionetworks.repo.model.table.Table;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.controller.EntityActionController;
import org.sagebionetworks.web.client.widget.entity.menu.v2.Action;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget.ActionListener;
import org.sagebionetworks.web.client.widget.entity.tabs.ChallengeTab;
import org.sagebionetworks.web.client.widget.entity.tabs.DiscussionTab;
import org.sagebionetworks.web.client.widget.entity.tabs.DockerTab;
import org.sagebionetworks.web.client.widget.entity.tabs.FilesTab;
import org.sagebionetworks.web.client.widget.entity.tabs.Tab;
import org.sagebionetworks.web.client.widget.entity.tabs.TablesTab;
import org.sagebionetworks.web.client.widget.entity.tabs.Tabs;
import org.sagebionetworks.web.client.widget.entity.tabs.WikiTab;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityPageTop implements EntityPageTopView.Presenter, SynapseWidgetPresenter, IsWidget  {
	private EntityPageTopView view;
	private EntityUpdatedHandler entityUpdateHandler;
	private EntityBundle projectBundle;
	private Throwable projectBundleLoadError;
	private Entity entity;
	
	private Synapse.EntityArea area;
	private String initialAreaToken;
	private String wikiAreaToken, tablesAreaToken, discussionAreaToken, dockerAreaToken;
	private Long filesVersionNumber;
	private EntityHeader projectHeader;
	
	private Tabs tabs;
	private WikiTab wikiTab;
	private FilesTab filesTab;
	private TablesTab tablesTab;
	private ChallengeTab adminTab;
	private DiscussionTab discussionTab;
	private DockerTab dockerTab;
	private EntityMetadata projectMetadata;
	private SynapseClientAsync synapseClient;
	private AuthenticationController authenticationController;
	// how many tabs have we determined the visibility state for?
	private int tabVisibilityInitializedCount;
	// how many tabs have been marked as visible
	private int visibleTabCount;
	
	private EntityActionController controller;
	private ActionMenuWidget actionMenu;
	private boolean annotationsShown;
	private CookieProvider cookies;
	private ClientCache storage;
	public static final boolean PUSH_TAB_URL_TO_BROWSER_HISTORY = false;
	@Inject
	public EntityPageTop(EntityPageTopView view, 
			SynapseClientAsync synapseClient,
			AuthenticationController authenticationController,
			Tabs tabs,
			EntityMetadata projectMetadata,
			WikiTab wikiTab,
			FilesTab filesTab,
			TablesTab tablesTab,
			ChallengeTab adminTab,
			DiscussionTab discussionTab,
			DockerTab dockerTab,
			EntityActionController controller,
			ActionMenuWidget actionMenu,
			CookieProvider cookies,
			ClientCache storage) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.authenticationController = authenticationController;
		this.tabs = tabs;
		this.wikiTab = wikiTab;
		this.filesTab = filesTab;
		this.tablesTab = tablesTab;
		this.adminTab = adminTab;
		this.discussionTab = discussionTab;
		this.dockerTab = dockerTab;
		this.projectMetadata = projectMetadata;
		this.controller = controller;
		this.actionMenu = actionMenu;
		this.cookies = cookies;
		this.storage = storage;
		
		initTabs();
		view.setTabs(tabs.asWidget());
		view.setProjectMetadata(projectMetadata.asWidget());
		view.setPresenter(this);
		
		actionMenu.addControllerWidget(controller.asWidget());
		view.setActionMenu(actionMenu.asWidget());
		
		annotationsShown = false;
		actionMenu.addActionListener(Action.TOGGLE_ANNOTATIONS, new ActionListener() {
			@Override
			public void onAction(Action action) {
				annotationsShown = !annotationsShown;
				EntityPageTop.this.controller.onAnnotationsToggled(annotationsShown);
				EntityPageTop.this.projectMetadata.setAnnotationsVisible(annotationsShown);
			}
		});
	}
	private void initTabs() {
		tabs.addTab(wikiTab.asTab());
		tabs.addTab(filesTab.asTab());
		tabs.addTab(tablesTab.asTab());
		tabs.addTab(adminTab.asTab());
		tabs.addTab(discussionTab.asTab());
		tabs.addTab(dockerTab.asTab());
		CallbackP<Boolean> showHideProjectInfoCallback = new CallbackP<Boolean>() {
			public void invoke(Boolean visible) {
				view.setProjectInformationVisible(visible);
			};
		};
		filesTab.setShowProjectInfoCallback(showHideProjectInfoCallback);
		tablesTab.setShowProjectInfoCallback(showHideProjectInfoCallback);
		dockerTab.setShowProjectInfoCallback(showHideProjectInfoCallback);
		
		// lazy init tabs, and show project information (if set)
		wikiTab.setTabClickedCallback(new CallbackP<Tab>() {
			public void invoke(Tab t) {
				configureWikiTab();
				view.setProjectInformationVisible(projectBundle != null);
			};
		});
		adminTab.setTabClickedCallback(new CallbackP<Tab>() {
			public void invoke(Tab t) {
				configureAdminTab();
				view.setProjectInformationVisible(projectBundle != null);
			};
		});
		
		discussionTab.setTabClickedCallback(new CallbackP<Tab>() {
			public void invoke(Tab t) {
				configureDiscussionTab();
				view.setProjectInformationVisible(projectBundle != null);
			};
		});
		filesTab.setTabClickedCallback(new CallbackP<Tab>() {
			public void invoke(Tab t) {
				configureFilesTab();
			};
		});
		tablesTab.setTabClickedCallback(new CallbackP<Tab>() {
			public void invoke(Tab t) {
				configureTablesTab();
			};
		});
		dockerTab.setTabClickedCallback(new CallbackP<Tab>() {
			public void invoke(Tab t) {
				configureDockerTab();
			};
		});
		
	}
	
    /**
     * Update the bundle attached to this EntityPageTop. 
     *
     * @param bundle
     */
    public void configure(Entity entity, Long versionNumber, EntityHeader projectHeader, Synapse.EntityArea initArea, String areaToken) {
    	this.projectHeader = projectHeader;
    	this.area = initArea;
    	this.initialAreaToken = areaToken;
    	wikiAreaToken = null;
    	tablesAreaToken = null;
    	discussionAreaToken = null;
    	dockerAreaToken = null;
    	filesVersionNumber = versionNumber;
    	this.entity = entity;

		//note: the files/tables/wiki/discussion/docker tabs rely on the project bundle, so they are configured later
    	configureProject();
	}
    
    public void configureProject() {
    	view.setLoadingVisible(true);
    	hideTabs();
    	int mask = ENTITY | ANNOTATIONS | PERMISSIONS | ACCESS_REQUIREMENTS | UNMET_ACCESS_REQUIREMENTS | FILE_HANDLES | ROOT_WIKI_ID | DOI | TABLE_DATA | ACL;
		projectBundle = null;
		projectBundleLoadError = null;
		view.setProjectInformationVisible(false);
		AsyncCallback<EntityBundle> callback = new AsyncCallback<EntityBundle>() {
			@Override
			public void onSuccess(EntityBundle bundle) {
				projectBundle = bundle;
				projectMetadata.setEntityBundle(projectBundle, null);
				showSelectedTabs();
			}

			@Override
			public void onFailure(Throwable caught) {
				projectBundleLoadError = caught;
				showSelectedTabs();
			}
		};
		synapseClient.getEntityBundle(projectHeader.getId(), mask, callback);
    }
    
    public void showSelectedTabs() {
    	// after all tab visilibity has been determined, then move on to configure the currently selected tab
    	tabVisibilityInitializedCount = 0;
    	visibleTabCount = 0;
    	// SWC-3137: show all tabs, until project display settings state persists.  Challenge is still dependent on content.
    	// always show the discussion tab
    	getTabVisibilityCallback(DISCUSSION, EntityArea.DISCUSSION, discussionTab.asTab()).onSuccess(true);
    	if (projectBundle == null || projectBundle.getPermissions() == null || projectBundle.getPermissions().getCanEdit()) {
    		// if user can edit, then show other tabs
	    	getTabVisibilityCallback(WIKI, EntityArea.WIKI, wikiTab.asTab()).onSuccess(true);
	    	getTabVisibilityCallback(FILES, EntityArea.FILES, filesTab.asTab()).onSuccess(true);
	    	getTabVisibilityCallback(TABLES, EntityArea.TABLES, tablesTab.asTab()).onSuccess(true);
	    	getTabVisibilityCallback(DOCKER, EntityArea.DOCKER, dockerTab.asTab()).onSuccess(true);
    	} else {
    		// otherwise only show the tabs only if content is present.
        	synapseClient.isWiki(projectHeader.getId(), getTabVisibilityCallback(WIKI, EntityArea.WIKI, wikiTab.asTab())); 
        	synapseClient.isFileOrFolder(projectHeader.getId(), getTabVisibilityCallback(FILES, EntityArea.FILES, filesTab.asTab())); 
        	synapseClient.isTable(projectHeader.getId(), getTabVisibilityCallback(TABLES, EntityArea.TABLES, tablesTab.asTab()));
        	synapseClient.isDocker(projectHeader.getId(), getTabVisibilityCallback(DOCKER, EntityArea.DOCKER, dockerTab.asTab()));
    	}
    	synapseClient.isChallenge(projectHeader.getId(), getTabVisibilityCallback(CHALLENGE, EntityArea.ADMIN, adminTab.asTab()));
	}
    
    public AsyncCallback<Boolean> getTabVisibilityCallback(final String displayArea, final EntityArea entityArea, final Tab tab) {
    	return new AsyncCallback<Boolean>() {
	    	@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
				view.setLoadingVisible(false);
			}
			@Override
			public void onSuccess(Boolean isContent) {
				view.setLoadingVisible(false);
				tabVisibilityInitializedCount++;
				boolean isShowingTab = isShowingTab(displayArea, isContent, entityArea);
				if (isShowingTab) {
					visibleTabCount++;
				}
				if (visibleTabCount > 1) {
					tabs.setNavTabsVisible(true);
				}
				
				tab.setTabListItemVisible(isShowingTab);
				configureCurrentAreaTabAfterComplete();
			}
		};
    }
    
    public void configureCurrentAreaTabAfterComplete() {
    	if (tabVisibilityInitializedCount == tabs.getTabCount()) {
    		openProjectDisplayIfNoVisibleTabs();
    		configureCurrentAreaTab();
    	}
    }
    
    public boolean isShowingTab(String displayArea, boolean areaHasContent, EntityArea associatedArea) {
    	String tag = EntityPageTop.this.authenticationController.getCurrentUserPrincipalId() + "_" + entity.getId() + "_";
    	if (Boolean.parseBoolean(storage.get(tag + displayArea))) {
    		return true;
    	} else if (areaHasContent) {
    		return true;
    	} else if (area != null && area.equals(associatedArea)) {
    		return true;
    	}
    	return false;
    }
    
    private void openProjectDisplayIfNoVisibleTabs() {
    	if (visibleTabCount == 0 && projectBundle.getPermissions().getCanEdit()) {
    		// pop up display options automatically (if the user has rights to change)
			controller.onProjectDisplay();
    	}
    }
    
    public void hideTabs() {
    	tabs.setNavTabsVisible(false);
    	wikiTab.asTab().setTabListItemVisible(false);
		filesTab.asTab().setTabListItemVisible(false);
		tablesTab.asTab().setTabListItemVisible(false);
		adminTab.asTab().setTabListItemVisible(false);
		discussionTab.asTab().setTabListItemVisible(false);
		dockerTab.asTab().setTabListItemVisible(false);
    }
    
    /**
     * Based on tab visibility, pick the area that should be displayed when no area is given.
     * @return
     */
    public EntityArea getDefaultProjectArea() {
    	if (wikiTab.asTab().isTabListItemVisible()) {
			return EntityArea.WIKI;
    	}
    	if (filesTab.asTab().isTabListItemVisible()) {
			return EntityArea.FILES;
    	}
    	if (tablesTab.asTab().isTabListItemVisible()) {
    		return EntityArea.TABLES;
    	}
    	if (discussionTab.asTab().isTabListItemVisible()) {
    		return EntityArea.DISCUSSION;
    	}
    	if (adminTab.asTab().isTabListItemVisible()) {
    		return EntityArea.ADMIN;
    	}
    	if (dockerTab.asTab().isTabListItemVisible()) {
    		return EntityArea.DOCKER;
    	}
    	return EntityArea.WIKI;
    }

	public void configureCurrentAreaTab() {
		if (entity instanceof Project) {
			view.setProjectInformationVisible(true);
		}
		
		//set area, if undefined
		if (area == null) {
			if (entity instanceof Project) {
				area = getDefaultProjectArea();
			} else if (entity instanceof Table) {
				area = EntityArea.TABLES;
			} else if (entity instanceof DockerRepository) {
				area = EntityArea.DOCKER;
			} else { //if (entity instanceof FileEntity || entity instanceof Folder, or any other entity type)
				area = EntityArea.FILES;
			}
		}
		// set area token
		switch (area) {
			case WIKI:
				wikiAreaToken = initialAreaToken;
				break;
			case TABLES:
				tablesAreaToken = initialAreaToken;
				break;
			case DISCUSSION:
				discussionAreaToken = initialAreaToken;
				break;
			case DOCKER:
				if (DisplayUtils.isInTestWebsite(cookies)) {
					dockerAreaToken = initialAreaToken;
				}
				break;
			default:
		}
		
		if (projectBundle != null) {
			String wikiId = getWikiPageId(wikiAreaToken, projectBundle.getRootWikiId());
			controller.configure(actionMenu, projectBundle, true, wikiId, entityUpdateHandler);
		}
		
		// set all content stale
		filesTab.asTab().setContentStale(true);
		wikiTab.asTab().setContentStale(true);
		tablesTab.asTab().setContentStale(true);
		adminTab.asTab().setContentStale(true);
		discussionTab.asTab().setContentStale(true);
		dockerTab.asTab().setContentStale(true);
		switch (area) {
			case FILES:
				configureFilesTab();
				tabs.showTab(filesTab.asTab(), PUSH_TAB_URL_TO_BROWSER_HISTORY);
				break;
			case WIKI:
				configureWikiTab();
				tabs.showTab(wikiTab.asTab(), PUSH_TAB_URL_TO_BROWSER_HISTORY);
				break;
			case TABLES:
				configureTablesTab();
				tabs.showTab(tablesTab.asTab(), PUSH_TAB_URL_TO_BROWSER_HISTORY);
				break;
			case ADMIN:
				configureAdminTab();
				tabs.showTab(adminTab.asTab(), PUSH_TAB_URL_TO_BROWSER_HISTORY);
				break;
			case DISCUSSION:
				configureDiscussionTab();
				tabs.showTab(discussionTab.asTab(), PUSH_TAB_URL_TO_BROWSER_HISTORY);
				break;
			case DOCKER:
				configureDockerTab();
				tabs.showTab(dockerTab.asTab(), PUSH_TAB_URL_TO_BROWSER_HISTORY);
				break;
			default:
		}
	}

    public void clearState() {
		view.clear();
		wikiTab.clear();
		this.entity = null;
	}

	@Override
	public Widget asWidget() {
		if(entity != null) {
			view.setPresenter(this);
			return view.asWidget();
		}
		return null;
	}

	public void configureTablesTab() {
		if (tablesTab.asTab().isContentStale()) {
			tablesTab.setProject(projectHeader.getId(), projectBundle, projectBundleLoadError);
			tablesTab.configure(entity, entityUpdateHandler, tablesAreaToken);
			tablesTab.asTab().setContentStale(false);
		}
	}
	
	public void configureFilesTab() {
		if (filesTab.asTab().isContentStale()) {
			filesTab.setProject(projectHeader.getId(), projectBundle, projectBundleLoadError);
			filesTab.configure(entity, entityUpdateHandler, filesVersionNumber);
			filesTab.asTab().setContentStale(false);
		}
	}
	
	public void configureWikiTab() {
		if (wikiTab.asTab().isContentStale()) {
			final boolean isWikiTabShown = area == EntityArea.WIKI;
			boolean canEdit = false;
			String wikiId = null;
			
			if (projectBundle != null) {
				canEdit = projectBundle.getPermissions().getCanCertifiedUserEdit();
				wikiId = getWikiPageId(wikiAreaToken, projectBundle.getRootWikiId());
			}
			
			final WikiPageWidget.Callback callback = new WikiPageWidget.Callback() {
				@Override
				public void pageUpdated() {
					fireEntityUpdatedEvent();
				}
				@Override
				public void noWikiFound() {
					if (isWikiTabShown && projectBundle.getRootWikiId() != null) {
						// attempted to load a wiki, but it was not found.  Show a message, and redirect to the root.
						view.showInfo("Wiki not found (id=" + wikiAreaToken + "), loading root wiki page instead.","");
						wikiTab.asTab().setContentStale(true);
						wikiAreaToken = projectBundle.getRootWikiId();
						configureWikiTab();	
					}
				}
			};
			
			wikiTab.configure(projectHeader.getId(), projectHeader.getName(), wikiId, 
					canEdit, callback);
			
			if (isWikiTabShown) {
				//initially push the configured place into the browser history
				tabs.showTab(wikiTab.asTab(), PUSH_TAB_URL_TO_BROWSER_HISTORY);
				view.setProjectInformationVisible(true);
			}
	
			CallbackP<String> wikiReloadHandler = new CallbackP<String>(){
				@Override
				public void invoke(String wikiPageId) {
					controller.configure(actionMenu, projectBundle, true, wikiPageId, entityUpdateHandler);
				}
			};
			wikiTab.setWikiReloadHandler(wikiReloadHandler);
			wikiTab.asTab().setContentStale(false);
		}
	}
	
	public void configureAdminTab() {
		if (adminTab.asTab().isContentStale()) {
			String projectId = projectHeader.getId();
			adminTab.configure(projectId, projectHeader.getName());
			adminTab.asTab().setContentStale(false);
		}
	}

	public void configureDiscussionTab() {
		if (discussionTab.asTab().isContentStale()) {
			String projectId = projectHeader.getId();
			boolean canModerate = false;
			if (projectBundle != null) {
				canModerate = projectBundle.getPermissions().getCanModerate();
			}
			discussionTab.configure(projectId, projectHeader.getName(), discussionAreaToken, canModerate);
			discussionTab.asTab().setContentStale(false);
		}
	}

	public void configureDockerTab() {
		if (dockerTab.asTab().isContentStale()) {
			dockerTab.setProject(projectHeader.getId(), projectBundle, projectBundleLoadError);
			dockerTab.configure(entity, entityUpdateHandler, dockerAreaToken);
			dockerTab.asTab().setContentStale(false);
		}
	}

	@Override
	public void fireEntityUpdatedEvent() {
		EntityUpdatedEvent event = new EntityUpdatedEvent();
		entityUpdateHandler.onPersistSuccess(event);
	}

	public void setEntityUpdatedHandler(EntityUpdatedHandler handler) {
		entityUpdateHandler = handler;
		projectMetadata.setEntityUpdatedHandler(handler);
	}
	
	public String getWikiPageId(String areaToken, String rootWikiId) {
		String wikiPageId = rootWikiId;
		if (DisplayUtils.isDefined(areaToken))
			wikiPageId = areaToken;
		return wikiPageId;
	}
}
