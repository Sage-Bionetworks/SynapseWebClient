package org.sagebionetworks.web.client.widget.entity.controller;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.Link;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.DisplayUtils.SelectedHandler;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.RenameEntityModalWidget;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import org.sagebionetworks.web.client.widget.entity.menu.v2.Action;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget.ActionListener;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListModalWidget;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityActionControllerImpl implements EntityActionController, ActionListener {
	
	public static final String MOVE_PREFIX = "Move ";

	public static final String ADD_WIKI = "Add wiki";

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
	EntityFinder entityFinder;
	
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
			RenameEntityModalWidget renameEntityModalWidget,
			EntityFinder entityFinder) {
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
		this.entityFinder = entityFinder;
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
		configureAddWiki();
		configureMove();
	}
	
	private void configureAddWiki(){
		if(this.entityBundle.getRootWikiId() == null){
			actionMenu.setActionVisible(Action.ADD_WIKI_PAGE, permissions.getCanEdit());
			actionMenu.setActionEnabled(Action.ADD_WIKI_PAGE, permissions.getCanEdit());
			actionMenu.setActionText(Action.ADD_WIKI_PAGE, ADD_WIKI);
			actionMenu.addActionListener(Action.ADD_WIKI_PAGE, this);
		}else{
			actionMenu.setActionVisible(Action.ADD_WIKI_PAGE, false);
			actionMenu.setActionEnabled(Action.ADD_WIKI_PAGE, false);
		}
	}
	
	private void configureMove(){
		if(isMovableType(entityBundle.getEntity()) ){
			actionMenu.setActionVisible(Action.MOVE_ENTITY, permissions.getCanEdit());
			actionMenu.setActionEnabled(Action.MOVE_ENTITY, permissions.getCanEdit());
			actionMenu.setActionText(Action.MOVE_ENTITY, MOVE_PREFIX+enityTypeDisplay);
			actionMenu.addActionListener(Action.MOVE_ENTITY, this);
		}else{
			actionMenu.setActionVisible(Action.MOVE_ENTITY, false);
			actionMenu.setActionEnabled(Action.MOVE_ENTITY, false);
		}
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
	
	/**
	 * Can an entity of this type be moved?
	 * @param entity
	 * @return
	 */
	public boolean isMovableType(Entity entity){
		if(entity instanceof Project){
			return false;
		}else if(entity instanceof TableEntity){
			return false;
		}
		return true;
	}
	
	/**
	 * Can an entity of this type have a wiki?
	 * @param entity
	 * @return
	 */
	public boolean isWikiableType(Entity entity){
		if(entity instanceof TableEntity){
			return false;
		}else if(entity instanceof Link){
			return false;
		}
		return true;
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
		case ADD_WIKI_PAGE:
			onAddWiki();
			break;
		case MOVE_ENTITY:
			onMove();
			break;
		default:
			break;
		}
	}
	
	private void onMove() {
		// Validate the user can update this entity.
		preflightController.checkUpdateEntity(this.entityBundle, new Callback() {
			@Override
			public void invoke() {
				postCheckMove();
			}
		});
	}

	private void postCheckMove(){
		entityFinder.configure(false, new SelectedHandler<Reference>() {					
			@Override
			public void onSelected(Reference selected) {
				if(selected.getTargetId() != null) {
					moveEntity(selected.getTargetId());
					entityFinder.hide();
				} else {
					view.showErrorMessage(DisplayConstants.PLEASE_MAKE_SELECTION);
				}
			}
		});
		entityFinder.show();
	}
	
	/**
	 * Move the entity to the given target.
	 * @param target
	 */
	private void moveEntity(String target){
		Entity entity = entityBundle.getEntity();
		entity.setParentId(target);
		synapseClient.updateEntity(entity, new AsyncCallback<Entity>() {
			
			@Override
			public void onSuccess(Entity result) {
				entityUpdateHandler.onPersistSuccess(new EntityUpdatedEvent());
			}
			
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		});
	}
	
	private void onAddWiki() {
		// Validate the user can update this entity.
		preflightController.checkUpdateEntity(this.entityBundle, new Callback() {
			@Override
			public void invoke() {
				postCheckAddWiki();
			}
		});
	}
	
	private void postCheckAddWiki(){
		WikiPage page = new WikiPage();
		synapseClient.createV2WikiPageWithV1(this.entityBundle.getEntity(). getId(), ObjectType.ENTITY.name(), page, new AsyncCallback<WikiPage>(){

			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}

			@Override
			public void onSuccess(WikiPage result) {
				entityUpdateHandler.onPersistSuccess(new EntityUpdatedEvent());
				
			}});
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
				globalApplicationState.gotoLastPlace(gotoPlace);
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
			gotoPlace = new Profile(authenticationController.getCurrentUserPrincipalId());
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
