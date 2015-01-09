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
import org.sagebionetworks.web.client.widget.entity.RenameEntityModalWidget;
import org.sagebionetworks.web.client.widget.entity.menu.v2.Action;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget.ActionListener;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListModalWidget;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityActionControllerImpl implements EntityActionController, ActionListener {
	
	public static final String THE = "The ";

	public static final String WAS_SUCCESSFULLY_DELETED = " was successfully deleted.";

	public static final String DELETED = "Deleted";

	public static final String ARE_YOU_SURE_YOU_WANT_TO_DELETE = "Are you sure you want to delete ";

	public static final String CONFIRM_DELETE_TITLE = "Confirm Delete";

	public static final String DELETE_PREFIX = "Delete ";
	
	public static final String RENAME_PREFIX = "Rename ";
	
	EntityActionControllerView view;
	PreflightController preflightController;
	EntityTypeProvider entityTypeProvider;
	SynapseClientAsync synapseClient;
	GlobalApplicationState globalApplicationState;
	AuthenticationController authenticationController;
	AccessControlListModalWidget accessControlListModalWidget;
	RenameEntityModalWidget renameEntityModalWidget;
	
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
			AccessControlListModalWidget accessControlListModalWidget,
			RenameEntityModalWidget renameEntityModalWidget) {
		super();
		this.view = view;
		this.accessControlListModalWidget = accessControlListModalWidget;
		this.view.addAccessControlListModalWidget(accessControlListModalWidget);
		this.preflightController = preflightController;
		this.entityTypeProvider = entityTypeProvider;
		this.synapseClient = synapseClient;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.renameEntityModalWidget = renameEntityModalWidget;
	}

	@Override
	public void configure(ActionMenuWidget actionMenu,
			EntityBundle entityBundle, EntityUpdatedHandler handler) {
		this.entityBundle = entityBundle;
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
		configureRenameAction();
	}
	
	private void configureRenameAction(){
		actionMenu.setActionVisible(Action.CHANGE_ENTITY_NAME, permissions.getCanEdit());
		actionMenu.setActionEnabled(Action.CHANGE_ENTITY_NAME, permissions.getCanEdit());
		actionMenu.setActionText(Action.CHANGE_ENTITY_NAME, RENAME_PREFIX+enityTypeDisplay);
		actionMenu.addActionListener(Action.CHANGE_ENTITY_NAME, this);
	}
	
	private void configureDeleteAction(){
		actionMenu.setActionVisible(Action.DELETE_ENTITY, permissions.getCanDelete());
		actionMenu.setActionEnabled(Action.DELETE_ENTITY, permissions.getCanDelete());
		actionMenu.setActionText(Action.DELETE_ENTITY, DELETE_PREFIX+enityTypeDisplay);
		actionMenu.addActionListener(Action.DELETE_ENTITY, this);
	}
	
	private void configureShareAction(){
		actionMenu.setActionEnabled(Action.SHARE, true);
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
		case CHANGE_ENTITY_NAME:
			onRename();
			break;	
		default:
			break;
		}
	}

	private void onRename() {
		// Validate the user can update this entity.
		preflightController.checkUpdateEntity(this.entityBundle, new Callback() {
			@Override
			public void invoke() {
				postCheckRename();
			}
		});
	}
	/**
	 * Called if the preflight check for a rename passes.
	 */
	private void postCheckRename(){
		renameEntityModalWidget.onRename(this.entity, new Callback() {
			@Override
			public void invoke() {
				entityUpdateHandler.onPersistSuccess(new EntityUpdatedEvent());
			}
		});
	}

	@Override
	public void onDeleteEntity() {
		// Confirm the delete with the user.
		view.showConfirmDialog(CONFIRM_DELETE_TITLE,ARE_YOU_SURE_YOU_WANT_TO_DELETE+this.enityTypeDisplay+" "+this.entity.getName(), new Callback() {
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
		preflightController.checkDeleteEntity(this.entityBundle, new Callback() {
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
		synapseClient.deleteEntityById(entityId, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				view.showInfo(DELETED, THE + enityTypeDisplay + WAS_SUCCESSFULLY_DELETED); 
				// Go to entity's parent
				Place gotoPlace = createDeletePlace();
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

	/**
	 * Create the delete place
	 * @return
	 */
	public Place createDeletePlace(){
		String parentId = entityBundle.getEntity().getParentId();
		Place gotoPlace = null;
		if(parentId != null && !(entityBundle.getEntity() instanceof Project)) {					
			if(entityBundle.getEntity() instanceof TableEntity) gotoPlace = new Synapse(parentId, null, EntityArea.TABLES, null);
			else gotoPlace = new Synapse(parentId);
		} else {
			gotoPlace = new Home(ClientProperties.DEFAULT_PLACE_TOKEN);
		}
		return gotoPlace;
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
