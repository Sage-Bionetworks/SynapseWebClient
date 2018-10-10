package org.sagebionetworks.web.client.widget.entity;

import static org.sagebionetworks.repo.model.EntityBundle.ACL;
import static org.sagebionetworks.repo.model.EntityBundle.ANNOTATIONS;
import static org.sagebionetworks.repo.model.EntityBundle.BENEFACTOR_ACL;
import static org.sagebionetworks.repo.model.EntityBundle.DOI;
import static org.sagebionetworks.repo.model.EntityBundle.ENTITY;
import static org.sagebionetworks.repo.model.EntityBundle.ENTITY_PATH;
import static org.sagebionetworks.repo.model.EntityBundle.FILE_HANDLES;
import static org.sagebionetworks.repo.model.EntityBundle.FILE_NAME;
import static org.sagebionetworks.repo.model.EntityBundle.HAS_CHILDREN;
import static org.sagebionetworks.repo.model.EntityBundle.PERMISSIONS;
import static org.sagebionetworks.repo.model.EntityBundle.ROOT_WIKI_ID;
import static org.sagebionetworks.repo.model.EntityBundle.TABLE_DATA;
import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.Link;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.docker.DockerRepository;
import org.sagebionetworks.repo.model.table.Table;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.events.ChangeSynapsePlaceEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.controller.EntityActionController;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.entity.tabs.ChallengeTab;
import org.sagebionetworks.web.client.widget.entity.tabs.DiscussionTab;
import org.sagebionetworks.web.client.widget.entity.tabs.DockerTab;
import org.sagebionetworks.web.client.widget.entity.tabs.FilesTab;
import org.sagebionetworks.web.client.widget.entity.tabs.Tab;
import org.sagebionetworks.web.client.widget.entity.tabs.TablesTab;
import org.sagebionetworks.web.client.widget.entity.tabs.Tabs;
import org.sagebionetworks.web.client.widget.entity.tabs.WikiTab;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.binder.EventHandler;

public class EntityPageTop implements SynapseWidgetPresenter, IsWidget  {
	public static final String PROJECT_SETTINGS = "Project Settings";
	private EntityPageTopView view;
	private EntityBundle currentTargetEntityBundle, projectBundle, filesEntityBundle, tablesEntityBundle, dockerEntityBundle;
	private Throwable projectBundleLoadError;
	private Entity entity;
	private SynapseJavascriptClient synapseJavascriptClient;

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
	// how many tabs have been marked as visible
	private int visibleTabCount;

	private EntityActionController projectActionController;
	private ActionMenuWidget projectActionMenu;
	private EntityActionController entityActionController;
	private ActionMenuWidget entityActionMenu;
	private PlaceChanger placeChanger;
	private CookieProvider cookies;
	private EventBus eventBus;
	public boolean pushTabUrlToBrowserHistory = false;
	
	public static final int ALL_PARTS_MASK = ENTITY | ENTITY_PATH | ANNOTATIONS | PERMISSIONS | ENTITY_PATH | HAS_CHILDREN | FILE_HANDLES | ROOT_WIKI_ID | DOI | FILE_NAME | BENEFACTOR_ACL | TABLE_DATA | ACL | BENEFACTOR_ACL;
	
	@Inject
	public EntityPageTop(EntityPageTopView view, 
			SynapseClientAsync synapseClient,
			Tabs tabs,
			EntityMetadata projectMetadata,
			WikiTab wikiTab,
			FilesTab filesTab,
			TablesTab tablesTab,
			ChallengeTab adminTab,
			DiscussionTab discussionTab,
			DockerTab dockerTab,
			EntityActionController projectActionController,
			ActionMenuWidget projectActionMenu,
			EntityActionController entityActionController,
			ActionMenuWidget entityActionMenu,
			CookieProvider cookies,
			SynapseJavascriptClient synapseJavascriptClient,
			GlobalApplicationState globalAppState,
			EventBus eventBus) {
		this.view = view;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.tabs = tabs;
		this.wikiTab = wikiTab;
		this.filesTab = filesTab;
		this.tablesTab = tablesTab;
		this.adminTab = adminTab;
		this.discussionTab = discussionTab;
		this.dockerTab = dockerTab;
		this.projectMetadata = projectMetadata;
		this.projectActionController = projectActionController;
		this.projectActionMenu = projectActionMenu;
		this.entityActionController = entityActionController;
		this.entityActionMenu = entityActionMenu;
		this.cookies = cookies;
		this.synapseJavascriptClient = synapseJavascriptClient;
		this.placeChanger = globalAppState.getPlaceChanger();
		this.eventBus = eventBus;
		
		initTabs();
		view.setTabs(tabs.asWidget());
		view.setProjectMetadata(projectMetadata.asWidget());

		projectActionMenu.addControllerWidget(projectActionController.asWidget());
		view.setProjectActionMenu(projectActionMenu.asWidget());

		entityActionMenu.addControllerWidget(entityActionController.asWidget());
		view.setEntityActionMenu(entityActionMenu.asWidget());
		
		projectMetadata.setAnnotationsTitleText("Project Annotations");
		
		view.getEventBinder().bindEventHandlers(this, eventBus);
	}

	/**
	 * Event fired by ButtonLinkWidget.
	 * @param event
	 */
	@EventHandler
	public void onChangeSynapsePlace(ChangeSynapsePlaceEvent event) {
		Synapse place = event.getPlace();
		if (entity.getId().equals(place.getEntityId())) {
			area = place.getArea();
			setCurrentAreaToken(place.getAreaToken());
			pushTabUrlToBrowserHistory = true;
			reconfigureCurrentArea();
		} else {
			placeChanger.goTo(place);
		}
	}
	
	public CallbackP<String> getEntitySelectedCallback(final EntityArea newArea) {
		return newEntityId -> {
			area = newArea;
			// always the current version from tab entity click
			Long version = null;
			// SWC-4023: clear out the initial area tokens when a new entity is selected
			clearAreaTokens();
			// SWC-3919: on tab entity click, push tab url to browser history
			pushTabUrlToBrowserHistory = true;
			configureEntity(newEntityId, version);
		};
	}
	
	private void clearAreaTokens() {
		tablesAreaToken = null;
		dockerAreaToken = null;
		discussionAreaToken = null;
		wikiAreaToken = null;
	}

	private void initTabs() {
		tabs.addTab(wikiTab.asTab());
		tabs.addTab(filesTab.asTab());
		tabs.addTab(tablesTab.asTab());
		tabs.addTab(adminTab.asTab());
		tabs.addTab(discussionTab.asTab());
		tabs.addTab(dockerTab.asTab());

		filesTab.setEntitySelectedCallback(getEntitySelectedCallback(EntityArea.FILES));
		tablesTab.setEntitySelectedCallback(getEntitySelectedCallback(EntityArea.TABLES));
		dockerTab.setEntitySelectedCallback(getEntitySelectedCallback(EntityArea.DOCKER));
		
		// lazy init tabs, and show project information (if set)
		wikiTab.setTabClickedCallback(tab -> {
			area = EntityArea.WIKI;
			configureWikiTab();
			projectMetadata.setVisible(true);
		});
		adminTab.setTabClickedCallback(tab -> {
			area = EntityArea.CHALLENGE;
			configureAdminTab();
			projectMetadata.setVisible(true);
		});

		discussionTab.setTabClickedCallback(tab -> {
			//SWC-4078: if already on tab, reset to top level thread list.
			if (EntityArea.DISCUSSION.equals(area)) {
				discussionAreaToken = null;
				discussionTab.asTab().setContentStale(true);
			}

			area = EntityArea.DISCUSSION;
			configureDiscussionTab();
			projectMetadata.setVisible(true);
		});
		filesTab.setTabClickedCallback(tab -> {
			//SWC-4078: if already on tab, reset to project level.
			if (EntityArea.FILES.equals(area)) {
				filesEntityBundle = projectBundle;
				filesTab.asTab().setContentStale(true);
			}
			area = EntityArea.FILES;
			configureFilesTab();
			projectMetadata.setVisible(projectBundle != null && filesEntityBundle.getEntity() instanceof Project);
		});
		tablesTab.setTabClickedCallback(tab -> {
			//SWC-4078: if already on tab, reset to project level.
			if (EntityArea.TABLES.equals(area)) {
				tablesEntityBundle = projectBundle;
				tablesTab.asTab().setContentStale(true);
			}

			area = EntityArea.TABLES;
			configureTablesTab();
			projectMetadata.setVisible(projectBundle != null && tablesEntityBundle.getEntity() instanceof Project);
		});
		dockerTab.setTabClickedCallback(tab -> {
			//SWC-4078: if already on tab, reset to project level.
			if (EntityArea.DOCKER.equals(area)) {
				dockerEntityBundle = projectBundle;
				dockerTab.asTab().setContentStale(true);
			}

			area = EntityArea.DOCKER;
			configureDockerTab();
			projectMetadata.setVisible(projectBundle != null && dockerEntityBundle.getEntity() instanceof Project);
		});
	}

	/**
	 * Update the bundle attached to this EntityPageTop. 
	 *
	 * @param bundle
	 */
	public void configure(EntityBundle targetEntityBundle, Long versionNumber, EntityHeader projectHeader, Synapse.EntityArea initArea, String areaToken) {
		this.currentTargetEntityBundle = targetEntityBundle;
		pushTabUrlToBrowserHistory = false;
		this.projectHeader = projectHeader;
		this.area = initArea;
		this.initialAreaToken = areaToken;
		wikiAreaToken = null;
		tablesAreaToken = null;
		discussionAreaToken = null;
		dockerAreaToken = null;
		filesVersionNumber = versionNumber;
		this.entity = targetEntityBundle.getEntity();

		//note: the files/tables/wiki/discussion/docker tabs rely on the project bundle, so they are configured later
		configureProject();
		initDefaultTabPlaces();
	}
	
	public void initDefaultTabPlaces() {
		//initialize each tab place
		if (projectHeader != null) {
			String projectName = projectHeader.getName();
			String projectId = projectHeader.getId();
			Long versionNumber = null;
			String areaToken = null;
			
			wikiTab.asTab().setEntityNameAndPlace(projectName, new Synapse(projectId, versionNumber, EntityArea.WIKI, areaToken));	
			filesTab.asTab().setEntityNameAndPlace(projectName, new Synapse(projectId, versionNumber, EntityArea.FILES, areaToken));
			tablesTab.asTab().setEntityNameAndPlace(projectName, new Synapse(projectId, versionNumber, EntityArea.TABLES, areaToken));
			adminTab.asTab().setEntityNameAndPlace(projectName, new Synapse(projectId, versionNumber, EntityArea.CHALLENGE, areaToken));
			discussionTab.asTab().setEntityNameAndPlace(projectName, new Synapse(projectId, versionNumber, EntityArea.DISCUSSION, areaToken));
			dockerTab.asTab().setEntityNameAndPlace(projectName, new Synapse(projectId, versionNumber, EntityArea.DOCKER, areaToken));
		}
	}
	
	public void configureProject() {
		view.setProjectLoadingVisible(true);
		hideTabs();
		projectBundle = null;
		projectBundleLoadError = null;
		projectMetadata.setVisible(false);
		AsyncCallback<EntityBundle> callback = new AsyncCallback<EntityBundle>() {
			@Override
			public void onSuccess(EntityBundle bundle) {
				view.setProjectLoadingVisible(false);
				// by default, all tab entity bundles point to the project entity bundle
				projectBundle = filesEntityBundle = tablesEntityBundle = dockerEntityBundle = bundle;
				projectMetadata.configure(projectBundle, null, projectActionMenu);
				
				initAreaToken();
				showSelectedTabs();
				updateEntityBundle(currentTargetEntityBundle, filesVersionNumber);
			}

			@Override
			public void onFailure(Throwable caught) {
				view.setProjectLoadingVisible(false);
				projectBundleLoadError = caught;
				updateEntityBundle(currentTargetEntityBundle, filesVersionNumber);
				showSelectedTabs();
			}
		};
		if (projectHeader.getId().equals(currentTargetEntityBundle.getEntity().getId())) {
			callback.onSuccess(currentTargetEntityBundle);
		} else {
			synapseJavascriptClient.getEntityBundle(projectHeader.getId(), ALL_PARTS_MASK, callback);	
		}
	}

	public void configureEntity(String entityId, final Long version) {
		AsyncCallback<EntityBundle> callback = new AsyncCallback<EntityBundle>() {
			@Override
			public void onSuccess(EntityBundle bundle) {
				updateEntityBundle(bundle, version);
			}

			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		};
		if (entityId != null && projectBundle != null && entityId.equals(projectBundle.getEntity().getId())) {
			callback.onSuccess(projectBundle);
		} else {
			synapseJavascriptClient.getEntityBundleForVersion(entityId, version, ALL_PARTS_MASK, callback);	
		}
	}

	public void reconfigureCurrentArea() {
		switch (area) {
		case FILES:
			configureFilesTab();
			tabs.showTab(filesTab.asTab(), pushTabUrlToBrowserHistory);
			break;
		case WIKI:
			configureWikiTab();
			tabs.showTab(wikiTab.asTab(), pushTabUrlToBrowserHistory);
			break;
		case TABLES:
			configureTablesTab();
			tabs.showTab(tablesTab.asTab(), pushTabUrlToBrowserHistory);
			break;
		case CHALLENGE:
			configureAdminTab();
			tabs.showTab(adminTab.asTab(), pushTabUrlToBrowserHistory);
			break;
		case DISCUSSION:
			configureDiscussionTab();
			tabs.showTab(discussionTab.asTab(), pushTabUrlToBrowserHistory);
			break;
		case DOCKER:
			configureDockerTab();
			tabs.showTab(dockerTab.asTab(), pushTabUrlToBrowserHistory);
			break;
		default:
		}
		pushTabUrlToBrowserHistory = false;
		//when tab reconfigured, scroll to the top
		view.scrollToTop();
	}

	public void updateEntityBundle(EntityBundle bundle, Long version) {
		this.currentTargetEntityBundle = bundle;
		entity = bundle.getEntity();
		// Redirect if Entity is a Link
		if(entity instanceof Link) {
			Reference ref = ((Link)bundle.getEntity()).getLinksTo();
			if(ref != null){
				placeChanger.goTo(new Synapse(ref.getTargetId(), ref.getTargetVersionNumber(), null, null));
			} else {
				// show error and then allow entity bundle to go to view
				view.showErrorMessage(DisplayConstants.ERROR_NO_LINK_DEFINED);
			}
		} else if (entity instanceof Project) {
			switch (area) {
			case FILES:
				fileChanged(bundle, version);
				break;
			case TABLES:
				tableChanged(bundle);
				break;
			case DOCKER:
				dockerChanged(bundle);
				break;
			default:
			}
		} else {
			if (entity instanceof FileEntity || entity instanceof Folder) {
				fileChanged(bundle, version);
			} else if (entity instanceof Table) {
				tableChanged(bundle);
			} else if (entity instanceof DockerRepository) {
				dockerChanged(bundle);
			}
		}
		boolean isCurrentVersion = version == null;
		entityActionController.configure(entityActionMenu, bundle, isCurrentVersion, null, area);
		projectMetadata.setVisible(bundle.getEntity() instanceof Project);
		reconfigureCurrentArea();
	}

	private void dockerChanged(EntityBundle bundle) {
		dockerEntityBundle = bundle;
		area = EntityArea.DOCKER;
		dockerTab.asTab().setContentStale(true);
	}

	private void tableChanged(EntityBundle bundle) {
		tablesEntityBundle = bundle;
		area = EntityArea.TABLES;
		tablesTab.asTab().setContentStale(true);
	}

	private void fileChanged(EntityBundle bundle, Long version) {
		filesEntityBundle = bundle;
		filesVersionNumber = version;
		area = EntityArea.FILES;
		filesTab.asTab().setContentStale(true);
	}

	public void showSelectedTabs() {
		visibleTabCount = 0;
		// SWC-3137: show all tabs, until project display settings state persists.  Challenge is still dependent on content.
		// always show the discussion tab
		getTabVisibilityCallback(EntityArea.DISCUSSION, discussionTab.asTab()).onSuccess(true);
		if (projectBundle == null || projectBundle.getPermissions() == null || projectBundle.getPermissions().getCanEdit()) {
			// if user can edit, then show other tabs
			getTabVisibilityCallback(EntityArea.WIKI, wikiTab.asTab()).onSuccess(true);
			getTabVisibilityCallback(EntityArea.FILES, filesTab.asTab()).onSuccess(true);
			getTabVisibilityCallback(EntityArea.TABLES, tablesTab.asTab()).onSuccess(true);
			getTabVisibilityCallback(EntityArea.DOCKER, dockerTab.asTab()).onSuccess(true);
		} else {
			// otherwise only show the tabs only if content is present.
			synapseJavascriptClient.isWiki(projectHeader.getId(), getTabVisibilityCallback(EntityArea.WIKI, wikiTab.asTab())); 
			synapseJavascriptClient.isFileOrFolder(projectHeader.getId(), getTabVisibilityCallback(EntityArea.FILES, filesTab.asTab())); 
			synapseJavascriptClient.isTable(projectHeader.getId(), getTabVisibilityCallback(EntityArea.TABLES, tablesTab.asTab()));
			synapseJavascriptClient.isDocker(projectHeader.getId(), getTabVisibilityCallback(EntityArea.DOCKER, dockerTab.asTab()));
		}
		synapseClient.isChallenge(projectHeader.getId(), getTabVisibilityCallback(EntityArea.CHALLENGE, adminTab.asTab()));
	}

	public AsyncCallback<Boolean> getTabVisibilityCallback(final EntityArea entityArea, final Tab tab) {
		return new AsyncCallback<Boolean>() {
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
			@Override
			public void onSuccess(Boolean isContent) {
				if (isContent) {
					visibleTabCount++;
				}
				if (visibleTabCount > 1) {
					tabs.setNavTabsVisible(true);
				}

				tab.setTabListItemVisible(isContent);
			}
		};
	}

	public void hideTabs() {
		tabs.setNavTabsVisible(false);
		wikiTab.asTab().setTabListItemVisible(false);
		filesTab.asTab().setTabListItemVisible(false);
		filesTab.resetView();
		tablesTab.asTab().setTabListItemVisible(false);
		tablesTab.resetView();
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
			return EntityArea.CHALLENGE;
		}
		if (dockerTab.asTab().isTabListItemVisible()) {
			return EntityArea.DOCKER;
		}
		return EntityArea.WIKI;
	}

	public void initAreaToken() {
		if (entity instanceof Project) {
			projectMetadata.setVisible(true);
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
		setCurrentAreaToken(initialAreaToken);

		if (projectBundle != null) {
			String wikiId = getWikiPageId(wikiAreaToken, projectBundle.getRootWikiId());
			projectActionController.configure(projectActionMenu, projectBundle, true, wikiId, null);
			projectActionMenu.setToolsButtonIcon(PROJECT_SETTINGS, IconType.GEAR);
		}

		// set all content stale
		filesTab.asTab().setContentStale(true);
		wikiTab.asTab().setContentStale(true);
		tablesTab.asTab().setContentStale(true);
		adminTab.asTab().setContentStale(true);
		discussionTab.asTab().setContentStale(true);
		dockerTab.asTab().setContentStale(true);
	}
	public void setCurrentAreaToken(String token) {
		// set area token
		switch (area) {
			case WIKI:
				wikiAreaToken = token;
				wikiTab.asTab().setContentStale(true);
				break;
			case TABLES:
				tablesAreaToken = token;
				tablesTab.asTab().setContentStale(true);
				break;
			case DISCUSSION:
				discussionAreaToken = token;
				discussionTab.asTab().setContentStale(true);
				break;
			case DOCKER:
				if (DisplayUtils.isInTestWebsite(cookies)) {
					dockerAreaToken = token;
					dockerTab.asTab().setContentStale(true);
				}
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
			return view.asWidget();
		}
		return null;
	}

	public void configureTablesTab() {
		if (tablesTab.asTab().isContentStale()) {
			tablesTab.setProject(projectHeader.getId(), projectBundle, projectBundleLoadError);
			tablesTab.configure(tablesEntityBundle, tablesAreaToken, entityActionMenu);
			tablesTab.asTab().setContentStale(false);
		}
		entityActionController.configure(entityActionMenu, tablesEntityBundle, true, null, area);
	}

	public void configureFilesTab() {
		if (filesTab.asTab().isContentStale()) {
			filesTab.setProject(projectHeader.getId(), projectBundle, projectBundleLoadError);
			filesTab.configure(filesEntityBundle, filesVersionNumber, entityActionMenu);
			filesTab.asTab().setContentStale(false);
		}
		boolean isCurrentVersion = filesVersionNumber == null;
		entityActionController.configure(entityActionMenu, filesEntityBundle, isCurrentVersion, null, area);
	}
	
	public void fireEntityUpdatedEvent() {
		eventBus.fireEvent(new EntityUpdatedEvent());
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
					if (isWikiTabShown && projectBundle.getRootWikiId() != null && !projectBundle.getRootWikiId().equals(wikiAreaToken)) {
						// attempted to load a wiki, but it was not found.  Show a message, and redirect to the root.
						view.showInfo("Wiki not found (id=" + wikiAreaToken + "), loading root wiki page instead.");
						wikiTab.asTab().setContentStale(true);
						wikiAreaToken = projectBundle.getRootWikiId();
						configureWikiTab();	
					}
				}
			};

			wikiTab.configure(projectHeader.getId(), projectHeader.getName(), wikiId, 
					canEdit, callback, entityActionMenu);
			if (isWikiTabShown) {
				tabs.showTab(wikiTab.asTab(), false);
				projectMetadata.setVisible(true);
			}

			CallbackP<String> wikiReloadHandler = new CallbackP<String>(){
				@Override
				public void invoke(String wikiPageId) {
					// a new wiki page has been loaded.
					// update the tab link.  Note that WikiSubpageNavigationTree will push the link into the history.
					Entity project = projectBundle.getEntity();
					wikiTab.asTab().setEntityNameAndPlace(project.getName(), new Synapse(project.getId(), null, EntityArea.WIKI, wikiPageId));
					// also update the action menu to target the correct wiki page.
					entityActionController.configure(entityActionMenu, projectBundle, true, wikiPageId, area);
				}
			};
			wikiTab.setWikiReloadHandler(wikiReloadHandler);
			wikiTab.asTab().setContentStale(false);
		}

		// on configure of wiki tab, always update the entity action controller with the correct wiki page
		String wikiId = wikiAreaToken;
		if (projectBundle != null) {
			wikiId = getWikiPageId(wikiAreaToken, projectBundle.getRootWikiId());
		}
		entityActionController.configure(entityActionMenu, projectBundle, true, wikiId, area);
	}

	public void configureAdminTab() {
		if (adminTab.asTab().isContentStale()) {
			String projectId = projectHeader.getId();
			adminTab.configure(projectId, projectHeader.getName());
			adminTab.asTab().setContentStale(false);
		}
		entityActionController.configure(entityActionMenu, projectBundle, true, null, area);
	}

	public void configureDiscussionTab() {
		if (discussionTab.asTab().isContentStale()) {
			String projectId = projectHeader.getId();
			boolean canModerate = false;
			if (projectBundle != null) {
				canModerate = projectBundle.getPermissions().getCanModerate();
			}
			discussionTab.configure(projectId, projectHeader.getName(), discussionAreaToken, canModerate, entityActionMenu);
			discussionTab.asTab().setContentStale(false);
		}
		entityActionController.configure(entityActionMenu, projectBundle, true, null, area);
		discussionTab.updateActionMenuCommands();
	}

	public void configureDockerTab() {
		if (dockerTab.asTab().isContentStale()) {
			dockerTab.setProject(projectHeader.getId(), projectBundle, projectBundleLoadError);
			dockerTab.configure(dockerEntityBundle, dockerAreaToken, entityActionMenu);
			dockerTab.asTab().setContentStale(false);
		}
		entityActionController.configure(entityActionMenu, dockerEntityBundle, true, null, area);
	}

	public String getWikiPageId(String areaToken, String rootWikiId) {
		String wikiPageId = rootWikiId;
		if (DisplayUtils.isDefined(areaToken))
			wikiPageId = areaToken;
		return wikiPageId;
	}
	
	//for testing
	public String getTablesAreaToken() {
		return tablesAreaToken;
	}
}
