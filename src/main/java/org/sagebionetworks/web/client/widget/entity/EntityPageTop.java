package org.sagebionetworks.web.client.widget.entity;

import static org.sagebionetworks.repo.model.EntityBundle.ACCESS_REQUIREMENTS;
import static org.sagebionetworks.repo.model.EntityBundle.ANNOTATIONS;
import static org.sagebionetworks.repo.model.EntityBundle.DOI;
import static org.sagebionetworks.repo.model.EntityBundle.ENTITY;
import static org.sagebionetworks.repo.model.EntityBundle.FILE_HANDLES;
import static org.sagebionetworks.repo.model.EntityBundle.PERMISSIONS;
import static org.sagebionetworks.repo.model.EntityBundle.ROOT_WIKI_ID;
import static org.sagebionetworks.repo.model.EntityBundle.UNMET_ACCESS_REQUIREMENTS;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.controller.EntityActionController;
import org.sagebionetworks.web.client.widget.entity.menu.v2.Action;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget.ActionListener;
import org.sagebionetworks.web.client.widget.entity.tabs.AdminTab;
import org.sagebionetworks.web.client.widget.entity.tabs.FilesTab;
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
	private EntityBundle bundle;
	private Long versionNumber;
	private Synapse.EntityArea area;
	private String areaToken;
	private EntityHeader projectHeader;
	
	private Tabs tabs;
	private WikiTab wikiTab;
	private FilesTab filesTab;
	private TablesTab tablesTab;
	private AdminTab adminTab;
	private EntityMetadata projectMetadata;
	private SynapseClientAsync synapseClient;
	
	private EntityActionController controller;
	private ActionMenuWidget actionMenu;
	boolean annotationsShown;
	
	@Inject
	public EntityPageTop(EntityPageTopView view, 
			SynapseClientAsync synapseClient,
			Tabs tabs,
			EntityMetadata projectMetadata,
			WikiTab wikiTab,
			FilesTab filesTab,
			TablesTab tablesTab,
			AdminTab adminTab,
			EntityActionController controller,
			ActionMenuWidget actionMenu
			) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.tabs = tabs;
		this.wikiTab = wikiTab;
		this.filesTab = filesTab;
		this.tablesTab = tablesTab;
		this.adminTab = adminTab;
		this.projectMetadata = projectMetadata;
		this.controller = controller;
		this.actionMenu = actionMenu;
		
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
	}

    /**
     * Update the bundle attached to this EntityPageTop. 
     *
     * @param bundle
     */
    public void configure(EntityBundle bundle, Long versionNumber, EntityHeader projectHeader, Synapse.EntityArea area, String areaToken) {
    	this.bundle = bundle;
    	this.versionNumber = versionNumber;
    	this.projectHeader = projectHeader;
    	this.area = area;
    	this.areaToken = areaToken;
    	
    	configureProject();
    	
    	//configure tabs
    	configureFilesTab();
    	configureTablesTab();
    	configureAdminTab();
    	//note: the wiki tab is always configured from the project bundle, so it is configured later (after getting the project bundle)

    	//set area, if undefined
		if (area == null) {
			if (bundle.getEntity() instanceof Project) {
				area = EntityArea.WIKI;
			} else {
				area = EntityArea.FILES;
			}
		}
		
    	//go to the tab corresponding to the area stated
		if (area == EntityArea.WIKI) {
			tabs.showTab(wikiTab.asTab());
		} else if (area == EntityArea.FILES) {
			tabs.showTab(filesTab.asTab());
		} else if (area == EntityArea.TABLES) {
			tabs.showTab(tablesTab.asTab());
		} else if (area == EntityArea.ADMIN) {
			tabs.showTab(adminTab.asTab());
		}
		
		view.setPageTitle(bundle.getEntity().getName() + " - " + bundle.getEntity().getId());
	}
    
    public void configureProject() {
    	//get the project entity bundle (unless we're looking at the project!)
    	if (projectHeader.getId().equals(bundle.getEntity().getId())) {
    		projectBundle = bundle;
    		configureFromProjectBundle();
    	} else {
    		int mask = ENTITY | ANNOTATIONS | PERMISSIONS | ACCESS_REQUIREMENTS | UNMET_ACCESS_REQUIREMENTS | FILE_HANDLES | ROOT_WIKI_ID | DOI ;
    		AsyncCallback<EntityBundle> callback = new AsyncCallback<EntityBundle>() {
    			@Override
    			public void onSuccess(EntityBundle bundle) {
					projectBundle = bundle;
					configureFromProjectBundle();
    			}
    			
    			@Override
    			public void onFailure(Throwable caught) {
    				view.showErrorMessage(caught.getMessage());
    			}	
    		};
			synapseClient.getEntityBundle(projectHeader.getId(), mask, callback);
    	}
    }
    
    private void configureFromProjectBundle() {
    	//set up owner project information
    	projectMetadata.setEntityBundle(projectBundle, versionNumber);
    	configureWikiTab();
    	controller.configure(actionMenu, projectBundle, projectBundle.getRootWikiId(), entityUpdateHandler);
    }
    
    public void clearState() {
		view.clear();
		// remove handlers
		this.bundle = null;
	}

	@Override
	public Widget asWidget() {
		if(bundle != null) {
			view.setPresenter(this);
			return view.asWidget();
		}
		return null;
	}

	@Override
	public void refresh() {
		configure(bundle, versionNumber, projectHeader, area, areaToken);
	}
	public void configureTablesTab() {
		tablesTab.configure(bundle, entityUpdateHandler, areaToken);
	}
	
	public void configureFilesTab() {
		filesTab.configure(bundle, entityUpdateHandler);
	}
	
	public void configureWikiTab() {
		final boolean canEdit = projectBundle.getPermissions().getCanCertifiedUserEdit();
		final WikiPageWidget.Callback callback = new WikiPageWidget.Callback() {
			@Override
			public void pageUpdated() {
				fireEntityUpdatedEvent();
			}
			@Override
			public void noWikiFound() {
				//if wiki area not specified and no wiki found, show Files tab instead for projects 
				// Note: The fix for SWC-1785 was to set this check to area == null.  Prior to this change it was area != WIKI.
				if(area == null) {							
					tabs.showTab(filesTab.asTab());
				}
			}
		};
		wikiTab.configure(projectBundle.getEntity().getId(), projectBundle.getRootWikiId(), 
				canEdit, callback);
		
		
		CallbackP<String> wikiReloadHandler = new CallbackP<String>(){
			@Override
			public void invoke(String wikiPageId) {
				controller.configure(actionMenu, projectBundle, wikiPageId, entityUpdateHandler);
				wikiTab.configure(projectBundle.getEntity().getId(), wikiPageId, 
						canEdit, callback);
			}
		};
		wikiTab.setWikiReloadHandler(wikiReloadHandler);
	}
	
	public void configureAdminTab() {
		String projectId = projectHeader.getId();
		adminTab.configure(projectId);
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
	
	public String getWikiPageId(Synapse.EntityArea area, String areaToken, String rootWikiId) {
		String wikiPageId = rootWikiId;
		if (Synapse.EntityArea.WIKI == area && DisplayUtils.isDefined(areaToken))
			wikiPageId = areaToken;
		return wikiPageId;
	}
}
