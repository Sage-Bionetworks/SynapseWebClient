package org.sagebionetworks.web.client.widget.entity.controller;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityDeletedEvent;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.menu.v2.Action;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget.ActionListener;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class EntityActionControllerImpl implements EntityActionController, EntityActionControllerView.Presenter, ActionListener {
	
	public static final String DELETE_PREFIX = "Delete ";
	public static final String ENTITY = "Entity";
	public static final String TABLE = "Table";
	public static final String FOLDER = "Folder";
	public static final String FILE = "File";
	public static final String PROJECT = "Project";
	
	EntityActionControllerView view;
	PreflightController preflightController;
	SynapseClientAsync synapseClient;
	GlobalApplicationState globalApplicationState;
	
	String displayType;
	EntityBundle entityBundle;
	boolean isUserAuthenticated;

	@Override
	public void configure(ActionMenuWidget actionMenu,
			EntityBundle entityBundle, boolean isUserAuthenticated) {
		UserEntityPermissions permissions = entityBundle.getPermissions();
		this.entityBundle = entityBundle;
		this.isUserAuthenticated = isUserAuthenticated;
		displayType = getTypeName(entityBundle.getEntity());
		// If the user can delete then enable the delete button
		actionMenu.setActionVisible(Action.DELETE_ENTITY, permissions.getCanDelete());
		actionMenu.setActionText(Action.DELETE_ENTITY, DELETE_PREFIX+displayType);
		actionMenu.addActionListener(Action.DELETE_ENTITY, this);
	}

	@Override
	public void onAction(Action action) {
		switch(action){
		case DELETE_ENTITY:
			onDeleteEntity();
			break;
		default:
			break;
		}
	}

	@Override
	public void onDeleteEntity() {
		// Confirm the delete with the user.
		view.showConfirmDialog("Are you sure you want to delete "+this.displayType+" "+this.entityBundle.getEntity().getName(), Action.DELETE_ENTITY);
	}

	@Override
	public void onConfirmAction(Action action) {
		switch(action){
		case DELETE_ENTITY:
			onConfirmedDeleteEntity();
			break;
		default:
			break;
		}
	}

	@Override
	public void onConfirmedDeleteEntity() {
		// The user has confirmed the delete so do the pre-flight test
		preflightController.preflightDeleteEntity(this.entityBundle, new Callback() {
			@Override
			public void invoke() {
				// TODO Auto-generated method stub
				
			}
		});
		
	}

	/**
	 * Get a display type for this entity.
	 * @param entity
	 * @return
	 */
	public static String getTypeName(Entity entity){
		if(entity instanceof Project){
			return PROJECT;
		}else if(entity instanceof FileEntity){
			return FILE;
		}else if(entity instanceof Folder){
			return FOLDER;
		}else if(entity instanceof TableEntity){
			return TABLE;
		}else{
			return ENTITY;
		}
	}

	@Override
	public void onPreflightCheckedDeleteEntity() {
		final String entityId = this.entityBundle.getEntity().getId();
		final String parentId = this.entityBundle.getEntity().getParentId();
		synapseClient.deleteEntityById(entityId, new AsyncCallback<Void>() {
			
			@Override
			public void onSuccess(Void result) {
				view.showInfo(" Deleted", "The " + displayType + " was successfully deleted."); 
				// Go to entity's parent
				Place gotoPlace = null;
				if(parentId != null && !Project.class.getName().equals(entityBundle.getEntity().getEntityType())) {					
					if(entityBundle.getEntity() instanceof TableEntity) gotoPlace = new Synapse(parentId, null, EntityArea.TABLES, null);
					else gotoPlace = new Synapse(parentId);
				} else {
					gotoPlace = new Home(ClientProperties.DEFAULT_PLACE_TOKEN);
				}
					
				globalApplicationState.getPlaceChanger().goTo(gotoPlace);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState,isUserAuthenticated, view)) {
					view.showErrorMessage(DisplayConstants.ERROR_ENTITY_DELETE_FAILURE);			
				}
			}
		});
		
	}
}
