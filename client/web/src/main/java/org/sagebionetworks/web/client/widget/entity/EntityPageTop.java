package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Locationable;
import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.EntitySchemaCache;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.services.NodeServiceAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.users.UserData;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityPageTop implements EntityPageTopView.Presenter, SynapseWidgetPresenter  {
		
	private EntityPageTopView view;
	private NodeServiceAsync nodeService;
	private SynapseClientAsync synapseClient;
	private PlaceChanger placeChanger;
	private NodeModelCreator nodeModelCreator;
	private AuthenticationController authenticationController;
	private GlobalApplicationState globalApplicationState;
	private EntityBundle bundle;
	private String entityTypeDisplay; 
	private HandlerManager handlerManager = new HandlerManager(this);
	private EntitySchemaCache schemaCache;	
	
	// TODO : delete this variable
	private String rStudioUrl = "http://localhost:8787";
	
	@Inject
	public EntityPageTop(EntityPageTopView view, NodeServiceAsync service, SynapseClientAsync synapseClient, NodeModelCreator nodeModelCreator, AuthenticationController authenticationController, GlobalApplicationState globalApplicationState, EntitySchemaCache schemaCache) {
		this.view = view;
		this.nodeService = service;
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.schemaCache = schemaCache;
		view.setPresenter(this);
	}	

    public void setPlaceChanger(PlaceChanger placeChanger) {
    	this.placeChanger = placeChanger;
    }

    public void setBundle(EntityBundle bundle) {
    	this.bundle = bundle;
    	if(bundle != null){
    		// Let the view know
    		// TODO : add referencedBy
    		sendDetailsToView(bundle.getPermissions().getCanChangePermissions(), bundle.getPermissions().getCanEdit());
    	}
	}

	@SuppressWarnings("unchecked")
	public void clearState() {
		view.clear();
		// remove handlers
		handlerManager = new HandlerManager(this);		
		this.bundle = null;		
	}
    
	@Override
	public Widget asWidget() {
		if(bundle != null) {
			return asWidget(bundle);
		} 
		return null;
	}	
	
	public Widget asWidget(EntityBundle bundle) {
		view.setPresenter(this);				
		return view.asWidget();
	}
	
	@Override
	public PlaceChanger getPlaceChanger() {
		return globalApplicationState.getPlaceChanger();
	}

	@Override
	public void refresh() {
		// TODO : tell consumer to refresh?
		sendDetailsToView(bundle.getPermissions().getCanChangePermissions(), bundle.getPermissions().getCanEdit());
	}	

	@Override
	public void fireEntityUpdatedEvent() {
		handlerManager.fireEvent(new EntityUpdatedEvent());
	}
	
	@SuppressWarnings("unchecked")
	public void addEntityUpdatedHandler(EntityUpdatedHandler handler) {		
		handlerManager.addHandler(EntityUpdatedEvent.getType(), handler);
	}

	@Override
	public boolean isLocationable() {
		if(bundle.getEntity() instanceof Locationable) {
			return true;
		}
		return false;
	}

	@Override
	public String getRstudioUrl() {
		// TODO : get this from user's profile, or prompt
		String urlBase = rStudioUrl; 
		UserData userData = authenticationController.getLoggedInUser();
		if(userData != null) {
			urlBase += "#Synapse:" + userData.getToken() + ":" + bundle.getEntity().getId();			
		} 
		return urlBase;
	}
	
	@Override
	public void saveRStudioUrlBase(String value) {
		// TODO : save RStudio Url to profile
		rStudioUrl = value;		
	}
	
	@Override
	public String getRstudioUrlBase() {
		return rStudioUrl;
	}
	
	@Override 
	public boolean isLoggedIn() {
		return authenticationController.getLoggedInUser() != null;
	}
	
	@Override
	public String createEntityLink(String id, String version, String display) {
		return DisplayUtils.createEntityLink(id, version, display);
	}

	@Override
	public void loadShortcuts(int offset, int limit, final AsyncCallback<PaginatedResults<EntityHeader>> callback) {
		PaginatedResults<EntityHeader> references = null;
		if(offset == 0) {
			 callback.onSuccess(bundle.getReferencedBy());			 
		} else {
			synapseClient.getEntityReferencedBy(bundle.getEntity().getId(), new AsyncCallback<String>() {
				@Override
				public void onSuccess(String result) {
					PaginatedResults<EntityHeader> paginatedResults = nodeModelCreator.createPaginatedResults(result, EntityHeader.class);
					callback.onSuccess(paginatedResults);
				}
				@Override
				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}
			});
		}		
	}

	
	/*
	 * Private Methods
	 */
	private void sendDetailsToView(boolean isAdmin, boolean canEdit) {
		ObjectSchema schema = schemaCache.getSchemaEntity(bundle.getEntity());
		entityTypeDisplay = DisplayUtils.getEntityTypeDisplay(schema);
		view.setEntityBundle(bundle, entityTypeDisplay, isAdmin, canEdit);
	}

}
