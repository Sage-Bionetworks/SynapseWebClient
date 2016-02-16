package org.sagebionetworks.web.client.widget.entity;

import static org.sagebionetworks.repo.model.EntityBundle.ACCESS_REQUIREMENTS;
import static org.sagebionetworks.repo.model.EntityBundle.ANNOTATIONS;
import static org.sagebionetworks.repo.model.EntityBundle.DOI;
import static org.sagebionetworks.repo.model.EntityBundle.ENTITY;
import static org.sagebionetworks.repo.model.EntityBundle.FILE_HANDLES;
import static org.sagebionetworks.repo.model.EntityBundle.PERMISSIONS;
import static org.sagebionetworks.repo.model.EntityBundle.ROOT_WIKI_ID;
import static org.sagebionetworks.repo.model.EntityBundle.TABLE_DATA;
import static org.sagebionetworks.repo.model.EntityBundle.UNMET_ACCESS_REQUIREMENTS;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.controller.EntityActionController;
import org.sagebionetworks.web.client.widget.entity.menu.v2.Action;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget.ActionListener;
import org.sagebionetworks.web.client.widget.entity.tabs.ChallengeTab;
import org.sagebionetworks.web.client.widget.entity.tabs.DiscussionTab;
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
	private String wikiAreaToken, tablesAreaToken, discussionAreaToken;
	private Long filesVersionNumber;
	private EntityHeader projectHeader;
	
	private Tabs tabs;
	private WikiTab wikiTab;
	private FilesTab filesTab;
	private TablesTab tablesTab;
	private ChallengeTab adminTab;
	private DiscussionTab discussionTab;
	private EntityMetadata projectMetadata;
	private SynapseClientAsync synapseClient;
	private GWTWrapper gwt;
	
	private EntityActionController controller;
	private ActionMenuWidget actionMenu;
	private boolean annotationsShown;
	
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
			EntityActionController controller,
			ActionMenuWidget actionMenu,
			GWTWrapper gwt) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.tabs = tabs;
		this.wikiTab = wikiTab;
		this.filesTab = filesTab;
		this.tablesTab = tablesTab;
		this.adminTab = adminTab;
		this.discussionTab = discussionTab;
		this.projectMetadata = projectMetadata;
		this.controller = controller;
		this.actionMenu = actionMenu;
		this.gwt = gwt;
		
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
		
		CallbackP<Boolean> showHideProjectInfoCallback = new CallbackP<Boolean>() {
			public void invoke(Boolean visible) {
				view.setProjectInformationVisible(visible);
			};
		};
		filesTab.setShowProjectInfoCallback(showHideProjectInfoCallback);
		tablesTab.setShowProjectInfoCallback(showHideProjectInfoCallback);
		
		//on tab change to these tabs, always show project info (if not null)
		CallbackP<Tab> showProjectInfoCallback = new CallbackP<Tab>() {
			public void invoke(Tab t) {
				view.setProjectInformationVisible(projectBundle != null);
			};
		};
		wikiTab.setTabClickedCallback(showProjectInfoCallback);
		adminTab.setTabClickedCallback(showProjectInfoCallback);
		discussionTab.setTabClickedCallback(showProjectInfoCallback);
	}
	
    /**
     * Update the bundle attached to this EntityPageTop. 
     *
     * @param bundle
     */
    public void configure(Entity entity, Long versionNumber, EntityHeader projectHeader, Synapse.EntityArea area, String areaToken) {
    	this.projectHeader = projectHeader;
    	this.area = area;
    	wikiAreaToken = null;
    	tablesAreaToken = null;
    	discussionAreaToken = null;
    	filesVersionNumber = versionNumber;
    	this.entity = entity;

    	//set area, if undefined
		if (area == null) {
			if (entity instanceof Project) {
				area = EntityArea.WIKI;
			} else if (entity instanceof TableEntity) {
				area = EntityArea.TABLES;
			} else { //if (entity instanceof FileEntity || entity instanceof Folder, or any other entity type)
				area = EntityArea.FILES;
			}
		}
		//go to the tab corresponding to the area stated
		if (area == EntityArea.WIKI) {
			tabs.showTab(wikiTab.asTab());
			wikiAreaToken = areaToken;
		} else if (area == EntityArea.FILES) {
			tabs.showTab(filesTab.asTab());
		} else if (area == EntityArea.TABLES) {
			tabs.showTab(tablesTab.asTab());
			tablesAreaToken = areaToken;
		} else if (area == EntityArea.ADMIN) {
			tabs.showTab(adminTab.asTab());
		} else if (area == EntityArea.DISCUSSION) {
			tabs.showTab(discussionTab.asTab());
			discussionAreaToken = areaToken;
		}
		
    	//note: the files/tables/wiki/discussion tabs rely on the project bundle, so they are configured later
    	configureProject();
	}
    
    public void configureProject() {
		int mask = ENTITY | ANNOTATIONS | PERMISSIONS | ACCESS_REQUIREMENTS | UNMET_ACCESS_REQUIREMENTS | FILE_HANDLES | ROOT_WIKI_ID | DOI | TABLE_DATA ;
		projectBundle = null;
		projectBundleLoadError = null;
		view.setProjectInformationVisible(false);
		AsyncCallback<EntityBundle> callback = new AsyncCallback<EntityBundle>() {
			@Override
			public void onSuccess(EntityBundle bundle) {
				projectBundle = bundle;
				projectMetadata.setEntityBundle(projectBundle, null);
				String wikiId = getWikiPageId(wikiAreaToken, projectBundle.getRootWikiId());
				controller.configure(actionMenu, projectBundle, true, wikiId, entityUpdateHandler);
				configureTabs();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				projectBundleLoadError = caught;
				configureTabs();
			}	
		};
		synapseClient.getEntityBundle(projectHeader.getId(), mask, callback);
    }

	private void configureTabs() {
		configureCurrentAreaTab();
		gwt.scheduleDeferred(new Callback() {
			@Override
			public void invoke() {
				configureOtherAreaTabs();
			}
		});
	}

	public void configureCurrentAreaTab() {
		if (area == null) {
			configureWikiTab();
			configureFilesTab();
		} else {
			switch (area) {
			case FILES:
				configureFilesTab();
				break;
			case WIKI:
				configureWikiTab();
				break;
			case TABLES:
				configureTablesTab();
				break;
			case ADMIN:
				configureAdminTab();
				break;
			case DISCUSSION:
				configureDiscussionTab();
				break;
			default:
			}
		}
	}

	public void configureOtherAreaTabs() {
		if (area == null) {
			configureTablesTab();
			configureAdminTab();
			configureDiscussionTab();
		} else {
			switch (area) {
			case FILES:
				configureWikiTab();
				configureTablesTab();
				configureAdminTab();
				configureDiscussionTab();
				break;
			case WIKI:
				configureFilesTab();
				configureTablesTab();
				configureAdminTab();
				configureDiscussionTab();
				break;
			case TABLES:
				configureWikiTab();
				configureFilesTab();
				configureAdminTab();
				configureDiscussionTab();
				break;
			case ADMIN:
				configureWikiTab();
				configureFilesTab();
				configureTablesTab();
				configureDiscussionTab();
				break;
			case DISCUSSION:
				configureWikiTab();
				configureFilesTab();
				configureTablesTab();
				configureAdminTab();
				break;
			default:
			}
		}
	}

    public void clearState() {
		view.clear();
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
		tablesTab.setProject(projectHeader.getId(), projectBundle, projectBundleLoadError);
		tablesTab.configure(entity, entityUpdateHandler, tablesAreaToken);
	}
	
	public void configureFilesTab() {
		filesTab.setProject(projectHeader.getId(), projectBundle, projectBundleLoadError);
		filesTab.configure(entity, entityUpdateHandler, filesVersionNumber);
	}
	
	public void configureWikiTab() {
		final boolean isWikiTabShown = (area == null && entity instanceof Project) || area == EntityArea.WIKI;
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
				//if wiki area not specified and no wiki found, show Files tab instead for projects 
				// Note: The fix for SWC-1785 was to set this check to area == null.  Prior to this change it was area != WIKI.
				if(isWikiTabShown) {
					tabs.showTab(filesTab.asTab());
				}
			}
		};
		
		wikiTab.configure(projectHeader.getId(), projectHeader.getName(), wikiId, 
				canEdit, callback);
		
		if (isWikiTabShown) {
			//initially push the configured place into the browser history
			tabs.showTab(wikiTab.asTab());
		}

		CallbackP<String> wikiReloadHandler = new CallbackP<String>(){
			@Override
			public void invoke(String wikiPageId) {
				controller.configure(actionMenu, projectBundle, true, wikiPageId, entityUpdateHandler);
			}
		};
		wikiTab.setWikiReloadHandler(wikiReloadHandler);
	}
	
	public void configureAdminTab() {
		String projectId = projectHeader.getId();
		adminTab.configure(projectId, projectHeader.getName());
	}

	public void configureDiscussionTab() {
		String projectId = projectHeader.getId();
		boolean canModerate = false;
		if (projectBundle != null) {
			canModerate = projectBundle.getPermissions().getCanModerate();
		}
		discussionTab.configure(projectId, projectHeader.getName(), discussionAreaToken, canModerate);
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
