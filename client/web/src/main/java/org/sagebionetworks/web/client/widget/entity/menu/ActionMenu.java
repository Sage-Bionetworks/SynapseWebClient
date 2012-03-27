package org.sagebionetworks.web.client.widget.entity.menu;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseClientUtils;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.services.NodeServiceAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.SynapseWidgetView;
import org.sagebionetworks.web.client.widget.entity.EntityEditor;
import org.sagebionetworks.web.shared.EntityType;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ActionMenu implements ActionMenuView.Presenter, SynapseWidgetPresenter {
	
	private ActionMenuView view;
	private PlaceChanger placeChanger;
	private NodeServiceAsync nodeService;
	private NodeModelCreator nodeModelCreator;
	private AuthenticationController authenticationController;
	private GlobalApplicationState globalApplicationState;
	private HandlerManager handlerManager = new HandlerManager(this);
	private EntityBundle entityBundle;
	private EntityTypeProvider entityTypeProvider;
	private SynapseClientAsync synapseClient;
	private JSONObjectAdapter jsonObjectAdapter;
	private EntityEditor entityEditor;
	
	@Inject
	public ActionMenu(ActionMenuView view, NodeServiceAsync nodeService, NodeModelCreator nodeModelCreator, AuthenticationController authenticationController, EntityTypeProvider entityTypeProvider, GlobalApplicationState globalApplicationState, SynapseClientAsync synapseClient, JSONObjectAdapter jsonObjectAdapter, EntityEditor entityEditor) {
		this.view = view;
		this.nodeService = nodeService;
		this.nodeModelCreator = nodeModelCreator;
		this.authenticationController = authenticationController;
		this.entityTypeProvider = entityTypeProvider;
		this.globalApplicationState = globalApplicationState;
		this.synapseClient = synapseClient;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.entityEditor = entityEditor;
		
		view.setPresenter(this);
	}	
	
	public Widget asWidget(EntityBundle bundle, boolean isAdministrator, boolean canEdit) {		
		view.setPresenter(this);
		this.entityBundle = bundle; 		
		
		// Get EntityType
		EntityType entityType = entityTypeProvider.getEntityTypeForEntity(bundle.getEntity());
		
		view.createMenu(bundle.getEntity(), entityType, isAdministrator, canEdit);
		return view.asWidget();
	}

	@SuppressWarnings("unchecked")
	public void clearState() {
		view.clear();
		// remove handlers
		handlerManager = new HandlerManager(this);		
		this.entityBundle = null;		
	}

	/**
	 * Does nothing. Use asWidget(Entity)
	 */
	@Override
	public Widget asWidget() {
		return null;
	}

    public void setPlaceChanger(PlaceChanger placeChanger) {
    	this.placeChanger = placeChanger;
    }
    
	@Override
	public PlaceChanger getPlaceChanger() {
		return placeChanger;
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
	public void deleteEntity() {
		final String parentId = entityBundle.getEntity().getParentId();
		final EntityType entityType = entityTypeProvider.getEntityTypeForEntity(entityBundle.getEntity());
		final String entityTypeDisplay = DisplayUtils.getEntityTypeDisplay(entityBundle.getEntity());
		nodeService.deleteNode(DisplayUtils.getNodeTypeForEntity(entityBundle.getEntity()), entityBundle.getEntity().getId(), new AsyncCallback<Void>() {			
			@Override
			public void onSuccess(Void result) {				
				view.showInfo(entityTypeDisplay + " Deleted", "The " + entityTypeDisplay + " was successfully deleted."); 
				// Go to entity's parent
				Place gotoPlace = null;
				if(parentId != null && !"/project".equals(entityType.getUrlPrefix())) {
					gotoPlace = new Synapse(parentId);
				} else {
					gotoPlace = new Home(DisplayUtils.DEFAULT_PLACE_TOKEN);
				}
					
				globalApplicationState.getPlaceChanger().goTo(gotoPlace);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(DisplayConstants.ERROR_ENTITY_DELETE_FAILURE);			
			}
		});
	}	
	
	@Override
	public List<EntityType> getAddSkipTypes() {
		// Get EntityType
		EntityType entityType = entityTypeProvider.getEntityTypeForEntity(entityBundle.getEntity());
		
		List<EntityType> ignore = new ArrayList<EntityType>();
		// ignore self type children 
		ignore.add(entityType); 

		// ignore locations & previews
		for(EntityType type : entityTypeProvider.getEntityTypes()) {
			if("location".equals(type.getName())) {
				ignore.add(type);
			}
			if("preview".equals(type.getName())) {
				ignore.add(type);
			}
		}
		
		return ignore;
	}

	@Override
	public boolean isUserLoggedIn() {
		return authenticationController.getLoggedInUser() != null;
	}

	@Override
	public void createShortcut(final String addToEntityId) {
		SynapseClientUtils.createShortcut(entityBundle.getEntity(), addToEntityId, view, synapseClient, nodeService, nodeModelCreator, jsonObjectAdapter);
	}

	@Override
	public void onEdit() {
		// Edit this entity.
		entityEditor.editEntity(entityBundle, false);
	}

	
	/*
	 * Private Methods
	 */
}
