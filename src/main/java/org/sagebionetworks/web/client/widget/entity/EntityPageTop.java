package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.IconSize;
import org.sagebionetworks.web.client.EntitySchemaCache;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;
import org.sagebionetworks.web.client.widget.handlers.AreaChangeHandler;
import org.sagebionetworks.web.shared.EntityType;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.ProjectAreaState;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityPageTop implements EntityPageTopView.Presenter, SynapseWidgetPresenter  {

	private EntityPageTopView view;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	private AuthenticationController authenticationController;
	private EntitySchemaCache schemaCache;
	private EntityTypeProvider entityTypeProvider;
	private IconsImageBundle iconsImageBundle;
	private WidgetRegistrar widgetRegistrar;
	private EntityUpdatedHandler entityUpdateHandler;
	private GlobalApplicationState globalApplicationState;
	private EntityBundle bundle;
	private String entityTypeDisplay;
	private EventBus bus;
	private JSONObjectAdapter jsonObjectAdapter;
	private Long versionNumber;
	private Synapse.EntityArea area;
	private String areaToken;
	private EntityHeader projectHeader;
	private AreaChangeHandler areaChangedHandler;
	private ProjectAreaState projectAreaState;
	
	@Inject
	public EntityPageTop(EntityPageTopView view, 
			SynapseClientAsync synapseClient,
			NodeModelCreator nodeModelCreator,
			AuthenticationController authenticationController,
			EntitySchemaCache schemaCache,
			EntityTypeProvider entityTypeProvider,
			IconsImageBundle iconsImageBundle,
			WidgetRegistrar widgetRegistrar,
			GlobalApplicationState globalApplicationState,
			EventBus bus, JSONObjectAdapter jsonObjectAdapter) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.authenticationController = authenticationController;
		this.schemaCache = schemaCache;
		this.entityTypeProvider = entityTypeProvider;
		this.iconsImageBundle = iconsImageBundle;
		this.widgetRegistrar = widgetRegistrar;
		this.bus = bus;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.globalApplicationState = globalApplicationState;	
		
		this.projectAreaState = new ProjectAreaState();
		view.setPresenter(this);
	}

    /**
     * Update the bundle attached to this EntityPageTop. Consider calling refresh()
     * to notify an attached view.
     *
     * @param bundle
     */
    public void configure(EntityBundle bundle, Long versionNumber, EntityHeader projectHeader, Synapse.EntityArea area, String areaToken) {
    	this.bundle = bundle;
    	this.versionNumber = versionNumber;
    	this.projectHeader = projectHeader;
    	this.area = area;
    	this.areaToken = areaToken;
    	
    	// reset state for newly visited project
    	if(!projectHeader.getId().equals(projectAreaState.getProjectId())) {
    		projectAreaState = new ProjectAreaState();
    		projectAreaState.setProjectId(projectHeader.getId());
    	}
    	
    	// For non-project entities, record them as the last file area place 
    	String entityId = bundle.getEntity().getId();
    	if(!projectHeader.getId().equals(entityId)) {
    		EntityHeader lastFileAreaEntity = new EntityHeader();
    		lastFileAreaEntity.setId(entityId);
    		lastFileAreaEntity.setVersionNumber(versionNumber);
    		projectAreaState.setLastFileAreaEntity(lastFileAreaEntity);
    	}
    	
    	// record last wiki state
    	if(area == EntityArea.WIKI) {
    		projectAreaState.setLastWikiSubToken(areaToken);
    	}
    	
    	// default area is the base wiki page if we are navigating to the project
    	if(area == null && entityId.equals(projectAreaState.getProjectId())) {
    		projectAreaState.setLastWikiSubToken(null);
    	}
	}
    
	@SuppressWarnings("unchecked")
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
		sendDetailsToView(bundle.getPermissions().getCanChangePermissions(), bundle.getPermissions().getCanEdit(), area, areaToken, projectHeader);
	}
		
	@Override
	public void fireEntityUpdatedEvent() {
		EntityUpdatedEvent event = new EntityUpdatedEvent();
		entityUpdateHandler.onPersistSuccess(event);
		bus.fireEvent(event);
	}

	public void setEntityUpdatedHandler(EntityUpdatedHandler handler) {
		entityUpdateHandler = handler;
	}
	
	@Override 
	public boolean isLoggedIn() {
		return authenticationController.isLoggedIn();
	}

	@Override
	public String createEntityLink(String id, String version, String display) {
		return DisplayUtils.createEntityLink(id, version, display);
	}

	@Override
	public ImageResource getIconForType(String typeString) {
		EntityType type = entityTypeProvider.getEntityTypeForString(typeString);
		// try class name as some references are short names, some class names
		if(type == null)
			type = entityTypeProvider.getEntityTypeForClassName(typeString);
		if(type == null) {
			return DisplayUtils.getSynapseIconForEntity(null, IconSize.PX16, iconsImageBundle);
		}
		return DisplayUtils.getSynapseIconForEntityType(type, IconSize.PX16, iconsImageBundle);
	}

	@Override
	public void loadShortcuts(int offset, int limit, final AsyncCallback<PaginatedResults<EntityHeader>> callback) {
		if(offset == 0) {
			 callback.onSuccess(bundle.getReferencedBy());
		} else {
			synapseClient.getEntityReferencedBy(bundle.getEntity().getId(), new AsyncCallback<String>() {
				@Override
				public void onSuccess(String result) {
					PaginatedResults<EntityHeader> paginatedResults;
					try {
						paginatedResults = nodeModelCreator.createPaginatedResults(result, EntityHeader.class);
						callback.onSuccess(paginatedResults);
					} catch (JSONObjectAdapterException e) {
						onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));						
					}
				}
				@Override
				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}
			});
		}
	}

	public void setAreaChangeHandler(AreaChangeHandler handler) {
		this.areaChangedHandler = handler;
	}
	
	@Override
	public void setArea(EntityArea area, String areaToken) {
		this.area = area;
		this.areaToken = areaToken;
		if(areaChangedHandler != null) areaChangedHandler.areaChanged(area, areaToken);
	}

	@Override
	public void refreshArea(Synapse.EntityArea area, String areaToken) {
		globalApplicationState.getPlaceChanger().goTo(new Synapse(bundle.getEntity().getId(), null, area, areaToken));
	}

	@Override
	public void gotoProjectArea(EntityArea area, boolean overrideCache) {
		String entityId = projectHeader.getId();
		String areaToken = null;
		Long versionNumber = null;
		if(!overrideCache) {
			if(area == EntityArea.WIKI) {
				areaToken = projectAreaState.getLastWikiSubToken();
			} else if(area == EntityArea.FILES && projectAreaState.getLastFileAreaEntity() != null) {
				entityId = projectAreaState.getLastFileAreaEntity().getId();
				versionNumber = projectAreaState.getLastFileAreaEntity().getVersionNumber();
			} 
		}

		if(!entityId.equals(projectHeader.getId())) area = null; // don't specify area in place for non-project entities
		globalApplicationState.getPlaceChanger().goTo(new Synapse(entityId, versionNumber, area, areaToken));
	}

	@Override
	public boolean isPlaceChangeForArea(EntityArea targetTab) {
		boolean isProject = bundle.getEntity().getId().equals(projectAreaState.getProjectId());		
		if(targetTab == EntityArea.ADMIN && !isProject) {
			// admin area clicked outside of project requires goto
			return true;
		} else if(targetTab == EntityArea.FILES) {
			// files area clicked in non-project entity requires goto root of files
			// files area clicked with last-file-state requires goto
			if(!isProject || projectAreaState.getLastFileAreaEntity() != null) {				
				return true;			
			}	
		} else if(targetTab == EntityArea.WIKI && !isProject) {
			// wiki area clicked in non-project entity requires goto
			return true;			
		}
		return false;		
	}

	/*
	 * Private Methods
	 */
	private void sendDetailsToView(boolean isAdmin, boolean canEdit, Synapse.EntityArea area, String areaToken, EntityHeader projectHeader) {		
		ObjectSchema schema = schemaCache.getSchemaEntity(bundle.getEntity());
		entityTypeDisplay = DisplayUtils.getEntityTypeDisplay(schema);
		view.setEntityBundle(bundle, getUserProfile(), entityTypeDisplay, isAdmin, canEdit, versionNumber, area, areaToken, projectHeader);
	}
	
	private UserProfile getUserProfile() {
		UserSessionData sessionData = authenticationController.getCurrentUserSessionData();
		return (sessionData==null ? null : sessionData.getProfile());		
	}

}
