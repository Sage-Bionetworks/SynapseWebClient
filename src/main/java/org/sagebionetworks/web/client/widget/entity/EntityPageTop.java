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
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
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

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityPageTop implements SynapseWidgetPresenter, IsWidget  {
	public static final String PROJECT_SETTINGS = "Project Settings";
	private EntityPageTopView view;
	private EntityUpdatedHandler entityUpdateHandler;
	private EntityBundle projectBundle, filesEntityBundle, tablesEntityBundle, dockerEntityBundle;
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
	public boolean pushTabUrlToBrowserHistory = false;
	
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
			GlobalApplicationState globalAppState) {
		this.view = view;
		this.synapseClient = synapseClient;
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
		
		initTabs();
		view.setTabs(tabs.asWidget());
		view.setProjectMetadata(projectMetadata.asWidget());

		projectActionMenu.addControllerWidget(projectActionController.asWidget());
		view.setProjectActionMenu(projectActionMenu.asWidget());
		projectActionMenu.setToolsButtonIcon(PROJECT_SETTINGS, IconType.GEAR);

		entityActionMenu.addControllerWidget(entityActionController.asWidget());
		view.setEntityActionMenu(entityActionMenu.asWidget());
		
		projectMetadata.setAnnotationsTitleText("Project Annotations");
	}

	public CallbackP<String> getEntitySelectedCallback(final EntityArea newArea) {
		return newEntityId -> {
			area = newArea;
			// always the current version from tab entity click
			Long version = null;
			// SWC-3919: on tab entity click, push tab url to browser history
			pushTabUrlToBrowserHistory = true;
			configureEntity(newEntityId, version);
		};
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
			area = EntityArea.ADMIN;
			configureAdminTab();
			projectMetadata.setVisible(true);
		});

		discussionTab.setTabClickedCallback(tab -> {
			area = EntityArea.DISCUSSION;
			configureDiscussionTab();
			projectMetadata.setVisible(true);
		});
		filesTab.setTabClickedCallback(tab -> {
			area = EntityArea.FILES;
			configureFilesTab();
			projectMetadata.setVisible(projectBundle != null && filesEntityBundle.getEntity() instanceof Project);
		});
		tablesTab.setTabClickedCallback(tab -> {
			area = EntityArea.TABLES;
			configureTablesTab();
			projectMetadata.setVisible(projectBundle != null && tablesEntityBundle.getEntity() instanceof Project);
		});
		dockerTab.setTabClickedCallback(tab -> {
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
	public void configure(Entity entity, Long versionNumber, EntityHeader projectHeader, Synapse.EntityArea initArea, String areaToken) {
		pushTabUrlToBrowserHistory = false;
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
		int mask = ENTITY | ANNOTATIONS | PERMISSIONS | ENTITY_PATH | HAS_CHILDREN | FILE_HANDLES | ROOT_WIKI_ID | DOI | FILE_NAME | BENEFACTOR_ACL | TABLE_DATA | ACL | BENEFACTOR_ACL;
		projectBundle = null;
		projectBundleLoadError = null;
		projectMetadata.setVisible(false);
		AsyncCallback<EntityBundle> callback = new AsyncCallback<EntityBundle>() {
			@Override
			public void onSuccess(EntityBundle bundle) {
				view.setLoadingVisible(false);
				// by default, all tab entity bundles point to the project entity bundle
				projectBundle = filesEntityBundle = tablesEntityBundle = dockerEntityBundle = bundle;
				projectMetadata.configure(projectBundle, null, projectActionMenu);
				initAreaToken();
				showSelectedTabs();
				configureEntity(entity.getId(), filesVersionNumber);
			}

			@Override
			public void onFailure(Throwable caught) {
				view.setLoadingVisible(false);
				projectBundleLoadError = caught;
				configureEntity(entity.getId(), filesVersionNumber);
				showSelectedTabs();
			}
		};
		synapseJavascriptClient.getEntityBundle(projectHeader.getId(), mask, callback);
	}

	public void configureEntity(String entityId, final Long version) {
		view.setLoadingVisible(true);
		int mask = ENTITY | ANNOTATIONS | PERMISSIONS | ENTITY_PATH | HAS_CHILDREN | FILE_HANDLES | ROOT_WIKI_ID | DOI | FILE_NAME | BENEFACTOR_ACL | TABLE_DATA | ACL | BENEFACTOR_ACL;
		AsyncCallback<EntityBundle> callback = new AsyncCallback<EntityBundle>() {
			@Override
			public void onSuccess(EntityBundle bundle) {
				updateEntityBundle(bundle, version);
				boolean isCurrentVersion = version == null;
				entityActionController.configure(entityActionMenu, bundle, isCurrentVersion, null, area, entityUpdateHandler);
				projectMetadata.setVisible(bundle.getEntity() instanceof Project);
				reconfigureCurrentArea();
				view.setLoadingVisible(false);
			}

			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
				view.setLoadingVisible(false);
			}
		};
		synapseJavascriptClient.getEntityBundleForVersion(entityId, version, mask, callback);
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
		case ADMIN:
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
			synapseClient.isWiki(projectHeader.getId(), getTabVisibilityCallback(EntityArea.WIKI, wikiTab.asTab())); 
			synapseJavascriptClient.isFileOrFolder(projectHeader.getId(), getTabVisibilityCallback(EntityArea.FILES, filesTab.asTab())); 
			synapseJavascriptClient.isTable(projectHeader.getId(), getTabVisibilityCallback(EntityArea.TABLES, tablesTab.asTab()));
			synapseJavascriptClient.isDocker(projectHeader.getId(), getTabVisibilityCallback(EntityArea.DOCKER, dockerTab.asTab()));
		}
		synapseClient.isChallenge(projectHeader.getId(), getTabVisibilityCallback(EntityArea.ADMIN, adminTab.asTab()));
	}

	public AsyncCallback<Boolean> getTabVisibilityCallback(final EntityArea entityArea, final Tab tab) {
		return new AsyncCallback<Boolean>() {
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
				view.setLoadingVisible(false);
			}
			@Override
			public void onSuccess(Boolean isContent) {
				view.setLoadingVisible(false);
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
			return EntityArea.ADMIN;
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
			projectActionController.configure(projectActionMenu, projectBundle, true, wikiId, null, entityUpdateHandler);
		}

		// set all content stale
		filesTab.asTab().setContentStale(true);
		wikiTab.asTab().setContentStale(true);
		tablesTab.asTab().setContentStale(true);
		adminTab.asTab().setContentStale(true);
		discussionTab.asTab().setContentStale(true);
		dockerTab.asTab().setContentStale(true);
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
			tablesTab.configure(tablesEntityBundle, entityUpdateHandler, tablesAreaToken, entityActionMenu);
			tablesTab.asTab().setContentStale(false);
		}
		entityActionController.configure(entityActionMenu, tablesEntityBundle, true, null, area, entityUpdateHandler);
	}

	public void configureFilesTab() {
		if (filesTab.asTab().isContentStale()) {
			filesTab.setProject(projectHeader.getId(), projectBundle, projectBundleLoadError);
			filesTab.configure(filesEntityBundle, entityUpdateHandler, filesVersionNumber, entityActionMenu);
			filesTab.asTab().setContentStale(false);
		}
		boolean isCurrentVersion = filesVersionNumber == null;
		entityActionController.configure(entityActionMenu, filesEntityBundle, isCurrentVersion, null, area, entityUpdateHandler);
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
						view.showInfo("Wiki not found (id=" + wikiAreaToken + "), loading root wiki page instead.","");
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
					entityActionController.configure(entityActionMenu, projectBundle, true, wikiPageId, area, entityUpdateHandler);
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
		entityActionController.configure(entityActionMenu, projectBundle, true, wikiId, area, entityUpdateHandler);
	}

	public void configureAdminTab() {
		if (adminTab.asTab().isContentStale()) {
			String projectId = projectHeader.getId();
			adminTab.configure(projectId, projectHeader.getName());
			adminTab.asTab().setContentStale(false);
		}
		entityActionController.configure(entityActionMenu, projectBundle, true, null, area, entityUpdateHandler);
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
		entityActionController.configure(entityActionMenu, projectBundle, true, null, area, entityUpdateHandler);
		discussionTab.updateActionMenuCommands();
	}

	public void configureDockerTab() {
		if (dockerTab.asTab().isContentStale()) {
			dockerTab.setProject(projectHeader.getId(), projectBundle, projectBundleLoadError);
			dockerTab.configure(dockerEntityBundle, entityUpdateHandler, dockerAreaToken, entityActionMenu);
			dockerTab.asTab().setContentStale(false);
		}
		entityActionController.configure(entityActionMenu, dockerEntityBundle, true, null, area, entityUpdateHandler);
	}

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
