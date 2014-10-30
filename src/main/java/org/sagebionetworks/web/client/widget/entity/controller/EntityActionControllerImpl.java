package org.sagebionetworks.web.client.widget.entity.controller;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.menu.v2.Action;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget.ActionListener;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListEditor;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListModalWidget;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityActionControllerImpl implements EntityActionController, ActionListener {
	
	public static final String DELETE_PREFIX = "Delete ";
	
	EntityActionControllerView view;
	PreflightController preflightController;
	EntityTypeProvider entityTypeProvider;
	SynapseClientAsync synapseClient;
	GlobalApplicationState globalApplicationState;
	AuthenticationController authenticationController;
	AccessControlListModalWidget accessControlListModalWidget;
	
	EntityBundle entityBundle;
	Entity entity;
	UserEntityPermissions permissions;
	String enityTypeDisplay;
	boolean isUserAuthenticated;
	ActionMenuWidget actionMenu;
	EntityUpdatedHandler entityUpdateHandler;

	
	@Inject
	public EntityActionControllerImpl(EntityActionControllerView view,
			PreflightController preflightController,
			EntityTypeProvider entityTypeProvider,
			SynapseClientAsync synapseClient,
			GlobalApplicationState globalApplicationState,
			AuthenticationController authenticationController,
			AccessControlListModalWidget accessControlListModalWidget) {
		super();
		this.view = view;
		this.accessControlListModalWidget = accessControlListModalWidget;
		this.view.addAccessControlListModalWidget(accessControlListModalWidget);
		this.preflightController = preflightController;
		this.entityTypeProvider = entityTypeProvider;
		this.synapseClient = synapseClient;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
	}

	@Override
	public void configure(ActionMenuWidget actionMenu,
			EntityBundle entityBundle, EntityUpdatedHandler handler) {
		this.entityUpdateHandler = handler;
		this.permissions = entityBundle.getPermissions();
		this.actionMenu = actionMenu;
		this.entity = entityBundle.getEntity();
		this.isUserAuthenticated = authenticationController.isLoggedIn();
		this.enityTypeDisplay = entityTypeProvider.getEntityDispalyName(entityBundle.getEntity());
		this.accessControlListModalWidget.configure(entity, permissions.getCanChangePermissions());  
		// Setup the actions
		configureDeleteAction();
		configureShareAction();
	}
	
	private void configureDeleteAction(){
		actionMenu.setActionVisible(Action.DELETE_ENTITY, permissions.getCanDelete());
		actionMenu.setActionText(Action.DELETE_ENTITY, DELETE_PREFIX+enityTypeDisplay);
		actionMenu.addActionListener(Action.DELETE_ENTITY, this);
	}
	
	private void configureShareAction(){
		actionMenu.setActionVisible(Action.SHARE, true);
		actionMenu.addActionListener(Action.SHARE, this);
		if(permissions.getCanPublicRead()){
			actionMenu.setActionIcon(Action.SHARE, IconType.GLOBE);
		}else{
			actionMenu.setActionIcon(Action.SHARE, IconType.LOCK);
		}
	}

	@Override
	public void onAction(Action action) {
		switch(action){
		case DELETE_ENTITY:
			onDeleteEntity();
			break;
		case SHARE:
			onShare();
			break;	
		default:
			break;
		}
	}

	@Override
	public void onDeleteEntity() {
		// Confirm the delete with the user.
		view.showConfirmDialog("Confirm Delete","Are you sure you want to delete "+this.enityTypeDisplay+" "+this.entity.getName(), new Callback() {
			@Override
			public void invoke() {
				postConfirmedDeleteEntity();
			}
		});
	}

	/**
	 * Called after the user has confirmed the delete of the entity.
	 */
	public void postConfirmedDeleteEntity() {
		// The user has confirmed the delete, the next step is the preflight check.
		preflightController.preflightDeleteEntity(this.entityBundle, new Callback() {
			@Override
			public void invoke() {
				postCheckDeleteEntity();
			}
		});
		
	}

	/**
	 * After all checks have been made we can do the actual entity delete.
	 */
	public void postCheckDeleteEntity() {
		final String entityId = this.entityBundle.getEntity().getId();
		final String parentId = this.entityBundle.getEntity().getParentId();
		synapseClient.deleteEntityById(entityId, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				view.showInfo("Deleted", "The " + enityTypeDisplay + " was successfully deleted."); 
				// Go to entity's parent
				Place gotoPlace = null;
				if(parentId != null && !(entityBundle.getEntity() instanceof Project)) {					
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

	@Override
	public void onShare() {
		this.accessControlListModalWidget.showSharing(new Callback() {
			@Override
			public void invoke() {
				entityUpdateHandler.onPersistSuccess(new EntityUpdatedEvent());
			}
		});
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
