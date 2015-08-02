package org.sagebionetworks.web.client.widget.entity.controller;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.extras.bootbox.client.callback.PromptCallback;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.Link;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.doi.Doi;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.SelectedHandler;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.EvaluationSubmitter;
import org.sagebionetworks.web.client.widget.entity.MarkdownEditorWidget;
import org.sagebionetworks.web.client.widget.entity.RenameEntityModalWidget;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import org.sagebionetworks.web.client.widget.entity.download.UploadDialogWidget;
import org.sagebionetworks.web.client.widget.entity.menu.v2.Action;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget.ActionListener;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListModalWidget;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.BadRequestException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.UnauthorizedException;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityActionControllerImpl implements EntityActionController, ActionListener {
	
	public static final String MOVE_PREFIX = "Move ";

	public static final String THE = "The ";

	public static final String WAS_SUCCESSFULLY_DELETED = " was successfully deleted.";

	public static final String DELETED = "Deleted";

	public static final String ARE_YOU_SURE_YOU_WANT_TO_DELETE = "Are you sure you want to delete ";

	public static final String CONFIRM_DELETE_TITLE = "Confirm Delete";

	public static final String DELETE_PREFIX = "Delete ";
	
	public static final String RENAME_PREFIX = "Rename ";

	EntityActionControllerView view;
	PreflightController preflightController;
	SynapseClientAsync synapseClient;
	GlobalApplicationState globalApplicationState;
	AuthenticationController authenticationController;
	AccessControlListModalWidget accessControlListModalWidget;
	RenameEntityModalWidget renameEntityModalWidget;
	EntityFinder entityFinder;
	EvaluationSubmitter submitter;
	
	EntityBundle entityBundle;
	String wikiPageId;
	Entity entity;
	UserEntityPermissions permissions;
	String enityTypeDisplay;
	boolean isUserAuthenticated;
	ActionMenuWidget actionMenu;
	EntityUpdatedHandler entityUpdateHandler;
	UploadDialogWidget uploader;
	MarkdownEditorWidget wikiEditor;
	ProvenanceEditorWidget provenanceEditor;
	StorageLocationWidget storageLocationEditor;
	
	@Inject
	public EntityActionControllerImpl(EntityActionControllerView view,
			PreflightController preflightController,
			SynapseClientAsync synapseClient,
			GlobalApplicationState globalApplicationState,
			AuthenticationController authenticationController,
			AccessControlListModalWidget accessControlListModalWidget,
			RenameEntityModalWidget renameEntityModalWidget,
			EntityFinder entityFinder,
			EvaluationSubmitter submitter,
			UploadDialogWidget uploader,
			MarkdownEditorWidget wikiEditor,
			ProvenanceEditorWidget provenanceEditor,
			StorageLocationWidget storageLocationEditor) {
		super();
		this.view = view;
		this.accessControlListModalWidget = accessControlListModalWidget;
		this.view.addAccessControlListModalWidget(accessControlListModalWidget);
		this.preflightController = preflightController;
		this.synapseClient = synapseClient;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.renameEntityModalWidget = renameEntityModalWidget;
		this.entityFinder = entityFinder;
		this.submitter = submitter;
		this.uploader = uploader;
		this.wikiEditor = wikiEditor;
		this.provenanceEditor = provenanceEditor;
		this.storageLocationEditor = storageLocationEditor;
		this.view.addMarkdownEditorModalWidget(wikiEditor.asWidget());
		this.view.addProvenanceEditorModalWidget(provenanceEditor.asWidget());
		this.view.addStorageLocationModalWidget(storageLocationEditor.asWidget());
	}

	@Override
	public void configure(ActionMenuWidget actionMenu,
			EntityBundle entityBundle, String wikiPageId, EntityUpdatedHandler handler) {
		this.entityBundle = entityBundle;
		this.wikiPageId = wikiPageId;
		this.entityUpdateHandler = handler;
		this.permissions = entityBundle.getPermissions();
		this.actionMenu = actionMenu;
		this.entity = entityBundle.getEntity();
		this.isUserAuthenticated = authenticationController.isLoggedIn();
		this.enityTypeDisplay = EntityType.getEntityTypeForClass(entityBundle.getEntity().getClass()).getDisplayName();
		this.accessControlListModalWidget.configure(entity, permissions.getCanChangePermissions());
		actionMenu.addControllerWidget(this.submitter.asWidget());
		actionMenu.addControllerWidget(uploader.asWidget());
		if (!isUserAuthenticated) {
			actionMenu.setToolsButtonVisible(false);
			if (permissions.getCanPublicRead()) {
				configureAnnotations();
				configureFileHistory();
			}
		} else {
			actionMenu.setToolsButtonVisible(true);
			// Setup the actions
			configureDeleteAction();
			configureShareAction();
			configureRenameAction();
			configureEditWiki();
			configureViewWikiSource();
			configureAddWikiSubpage();
			configureMove();
			configureLink();
			configureSubmit();
			configureAnnotations();
			configureFileHistory();
			configureFileUpload();
			configureProvenance();
			configureChangeStorageLocation();
			configureCreateDOI();
		}
	}
	
	private void configureProvenance() {
		if(entityBundle.getEntity() instanceof FileEntity ){
			actionMenu.setActionVisible(Action.EDIT_PROVENANCE, permissions.getCanEdit());
			actionMenu.setActionEnabled(Action.EDIT_PROVENANCE, permissions.getCanEdit());
			actionMenu.addActionListener(Action.EDIT_PROVENANCE, this);
		} else {
			actionMenu.setActionVisible(Action.EDIT_PROVENANCE, false);
			actionMenu.setActionEnabled(Action.EDIT_PROVENANCE, false);
		}
	}
	
	private void configureChangeStorageLocation() {
		if(entityBundle.getEntity() instanceof Folder || entityBundle.getEntity() instanceof Project){
			actionMenu.setActionVisible(Action.CHANGE_STORAGE_LOCATION, permissions.getCanEdit());
			actionMenu.setActionEnabled(Action.CHANGE_STORAGE_LOCATION, permissions.getCanEdit());
			actionMenu.addActionListener(Action.CHANGE_STORAGE_LOCATION, this);
		} else {
			actionMenu.setActionVisible(Action.CHANGE_STORAGE_LOCATION, false);
			actionMenu.setActionEnabled(Action.CHANGE_STORAGE_LOCATION, false);
		}
	}
	
	private void configureCreateDOI() {
		boolean canEdit = permissions.getCanEdit();
		actionMenu.setActionVisible(Action.CREATE_DOI, false);
		actionMenu.setActionEnabled(Action.CREATE_DOI, false);
		if (canEdit) {
			actionMenu.addActionListener(Action.CREATE_DOI, this);
			synapseClient.getEntityDoi(entity.getId(), getVersion(), new AsyncCallback<Doi>() {
				@Override
				public void onFailure(Throwable caught) {
					boolean isNotFound = caught instanceof NotFoundException;
					//show command if not found
					actionMenu.setActionVisible(Action.CREATE_DOI, isNotFound);
					actionMenu.setActionEnabled(Action.CREATE_DOI, isNotFound);
				}
				public void onSuccess(Doi result) {
					//if there's a Doi, then continue to not show command
				};
			});
		}
	}
	
	private Long getVersion(){
		Long version = null;
		Entity entity = entityBundle.getEntity();
		if (entity instanceof Versionable) {
			version = ((Versionable)entity).getVersionNumber();
		}
		return version;
	}
	
	private void onCreateDOI() {
		synapseClient.createDoi(entity.getId(), getVersion(), new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void v) {
				view.showInfo(DisplayConstants.DOI_REQUEST_SENT_TITLE, DisplayConstants.DOI_REQUEST_SENT_MESSAGE);
				entityUpdateHandler.onPersistSuccess(new EntityUpdatedEvent());
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		});
	}

	
	private void configureFileUpload() {
		if(entityBundle.getEntity() instanceof FileEntity ){
			actionMenu.setActionVisible(Action.UPLOAD_NEW_FILE, permissions.getCanCertifiedUserEdit());
			actionMenu.setActionEnabled(Action.UPLOAD_NEW_FILE, permissions.getCanCertifiedUserEdit());
			actionMenu.addActionListener(Action.UPLOAD_NEW_FILE, this);
		}else{
			actionMenu.setActionVisible(Action.UPLOAD_NEW_FILE, false);
			actionMenu.setActionEnabled(Action.UPLOAD_NEW_FILE, false);
		}
	}

	private void configureEditWiki(){
		if(isWikiableType(entityBundle.getEntity())){
			actionMenu.setActionVisible(Action.EDIT_WIKI_PAGE, permissions.getCanEdit());
			actionMenu.setActionEnabled(Action.EDIT_WIKI_PAGE, permissions.getCanEdit());
			actionMenu.addActionListener(Action.EDIT_WIKI_PAGE, this);
		}else{
			actionMenu.setActionVisible(Action.EDIT_WIKI_PAGE, false);
			actionMenu.setActionEnabled(Action.EDIT_WIKI_PAGE, false);
		}
	}
	
	private void configureViewWikiSource(){
		//only visible if entity may have a wiki, and user can't Edit the wiki
		if(isWikiableType(entityBundle.getEntity())){
			actionMenu.setActionVisible(Action.VIEW_WIKI_SOURCE, !permissions.getCanEdit());
			actionMenu.setActionEnabled(Action.VIEW_WIKI_SOURCE, !permissions.getCanEdit());
			actionMenu.addActionListener(Action.VIEW_WIKI_SOURCE, this);
		}else{
			actionMenu.setActionVisible(Action.VIEW_WIKI_SOURCE, false);
			actionMenu.setActionEnabled(Action.VIEW_WIKI_SOURCE, false);
		}
	}

	
	private void configureAddWikiSubpage(){
		if(entityBundle.getEntity() instanceof Project){
			actionMenu.setActionVisible(Action.ADD_WIKI_SUBPAGE, permissions.getCanEdit());
			actionMenu.setActionEnabled(Action.ADD_WIKI_SUBPAGE, permissions.getCanEdit());
			actionMenu.addActionListener(Action.ADD_WIKI_SUBPAGE, this);
		}else{
			actionMenu.setActionVisible(Action.ADD_WIKI_SUBPAGE, false);
			actionMenu.setActionEnabled(Action.ADD_WIKI_SUBPAGE, false);
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
	
	private void configureLink(){
		if(isLinkType(entityBundle.getEntity())){
			actionMenu.setActionVisible(Action.CREATE_LINK, true);
			actionMenu.setActionEnabled(Action.CREATE_LINK, true);
			actionMenu.addActionListener(Action.CREATE_LINK, this);
		}else{
			actionMenu.setActionVisible(Action.CREATE_LINK, false);
			actionMenu.setActionEnabled(Action.CREATE_LINK, false);
		}
	}
	
	private void configureAnnotations(){
		actionMenu.setActionVisible(Action.TOGGLE_ANNOTATIONS, true);
		actionMenu.setActionEnabled(Action.TOGGLE_ANNOTATIONS, true);
		actionMenu.addActionListener(Action.TOGGLE_ANNOTATIONS, this);
	}
	
	@Override
	public void onAnnotationsToggled(boolean shown) {
		if(shown){
			actionMenu.setActionIcon(Action.TOGGLE_ANNOTATIONS, IconType.TOGGLE_DOWN);
		}else{
			actionMenu.setActionIcon(Action.TOGGLE_ANNOTATIONS, IconType.TOGGLE_RIGHT);
		}
	}
	

	private void configureFileHistory(){
		if(entityBundle.getEntity() instanceof FileEntity){
			actionMenu.setActionVisible(Action.TOGGLE_FILE_HISTORY, true);
			actionMenu.setActionEnabled(Action.TOGGLE_FILE_HISTORY, true);
			actionMenu.addActionListener(Action.TOGGLE_FILE_HISTORY, this);
		}else{
			actionMenu.setActionVisible(Action.TOGGLE_FILE_HISTORY, false);
			actionMenu.setActionEnabled(Action.TOGGLE_FILE_HISTORY, false);
		}
	}
	
	@Override
	public void onFileHistoryToggled(boolean shown) {
		if(shown){
			actionMenu.setActionIcon(Action.TOGGLE_FILE_HISTORY, IconType.TOGGLE_DOWN);
		}else{
			actionMenu.setActionIcon(Action.TOGGLE_FILE_HISTORY, IconType.TOGGLE_RIGHT);
		}
	}
	
	private void configureSubmit(){
		if(isSubmittableType(entityBundle.getEntity())){
			actionMenu.setActionVisible(Action.SUBMIT_TO_CHALLENGE, true);
			actionMenu.setActionEnabled(Action.SUBMIT_TO_CHALLENGE, true);
			actionMenu.addActionListener(Action.SUBMIT_TO_CHALLENGE, this);
		}else{
			actionMenu.setActionVisible(Action.SUBMIT_TO_CHALLENGE, false);
			actionMenu.setActionEnabled(Action.SUBMIT_TO_CHALLENGE, false);
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
	
	/**
	 * Can a link to this type be created?
	 * @param entity
	 * @return
	 */
	public boolean isLinkType(Entity entity){
		if(entity instanceof Link){
			return false;
		}
		return true;
	}
	
	/**
	 * Can an entity of this type be submitted to a challenge?
	 * @param entity
	 * @return
	 */
	public boolean isSubmittableType(Entity entity){
		if(entity instanceof TableEntity){
			return false;
		}
		return entity instanceof Versionable;
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
		case EDIT_WIKI_PAGE:
			onEditWiki();
			break;
		case VIEW_WIKI_SOURCE:
			onViewWikiSource();
			break;
		case ADD_WIKI_SUBPAGE:
			onAddWikiSubpage();
			break;
		case MOVE_ENTITY:
			onMove();
			break;
		case CREATE_LINK:
			onLink();
			break;
		case SUBMIT_TO_CHALLENGE:
			onSubmit();
			break;
		case UPLOAD_NEW_FILE:
			onUploadFile();
			break;
		case EDIT_PROVENANCE:
			onEditProvenance();
			break;
		case CHANGE_STORAGE_LOCATION:
			onChangeStorageLocation();
			break;
		case CREATE_DOI:
			onCreateDOI();
			break;
		default:
			break;
		}
	}
	

	private void onChangeStorageLocation() {
		storageLocationEditor.configure(this.entityBundle, entityUpdateHandler);
		storageLocationEditor.show();
	}
	
	private void onEditProvenance() {
		provenanceEditor.configure(this.entityBundle, entityUpdateHandler);
		provenanceEditor.show();
	}
	
	private void onUploadFile() {
		// Validate the user can upload to this entity.
		preflightController.checkUploadToEntity(this.entityBundle, new Callback() {
			@Override
			public void invoke() {
				postCheckUploadFile();
			}
		});
	}
	
	private void postCheckUploadFile(){
		uploader.configure(DisplayConstants.TEXT_UPLOAD_FILE_OR_LINK, entityBundle.getEntity(), null, entityUpdateHandler, null, true);
		uploader.disableMultipleFileUploads();
		uploader.setUploaderLinkNameVisible(false);
		uploader.show();
	}

	private void onSubmit() {
		// Validate the user can update this entity.
		preflightController.checkUpdateEntity(this.entityBundle, new Callback() {
			@Override
			public void invoke() {
				postOnSubmit();
			}
		});
	}
	
	private void postOnSubmit(){
		this.submitter.configure(this.entityBundle.getEntity(), null);
	}
	
	private void onLink() {
		// Validate the user can update this entity.
		preflightController.checkUpdateEntity(this.entityBundle, new Callback() {
			@Override
			public void invoke() {
				postCheckLink();
			}
		});
	}
	
	private void postCheckLink(){
		entityFinder.configure(false, new SelectedHandler<Reference>() {					
			@Override
			public void onSelected(Reference selected) {
				if(selected.getTargetId() != null) {
					createLink(selected.getTargetId());
					entityFinder.hide();
				} else {
					view.showErrorMessage(DisplayConstants.PLEASE_MAKE_SELECTION);
				}
			}
		});
		entityFinder.show();
	}
	
	/**
	 * Create a link with the given target as a parent.
	 * @param target
	 */
	public void createLink(String target){
		Link link = new Link();
		link.setParentId(target);
		Reference ref = new Reference();
		ref.setTargetId(entityBundle.getEntity().getId());
		link.setLinksTo(ref); // links to this entity
		link.setLinksToClassName(entityBundle.getEntity().getEntityType());
		link.setName(entityBundle.getEntity().getName()); // copy name of this entity as default
		link.setEntityType(Link.class.getName());
		synapseClient.createEntity(link, new AsyncCallback<Entity>() {
			
			@Override
			public void onSuccess(Entity result) {
				view.showInfo(DisplayConstants.TEXT_LINK_SAVED, DisplayConstants.TEXT_LINK_SAVED);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				if(caught instanceof BadRequestException) {
					view.showErrorMessage(DisplayConstants.ERROR_CANT_MOVE_HERE);
					return;
				}
				if(caught instanceof NotFoundException) {
					view.showErrorMessage(DisplayConstants.ERROR_NOT_FOUND);
					return;
				}
				if (caught instanceof UnauthorizedException) {
					view.showErrorMessage(DisplayConstants.ERROR_NOT_AUTHORIZED);
					return;
				}
				view.showErrorMessage(caught.getMessage());
				
			}
		});
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
	
	private void onViewWikiSource() {
		WikiPageKey key = new WikiPageKey(this.entityBundle.getEntity().getId(), ObjectType.ENTITY.name(), wikiPageId);
		synapseClient.getV2WikiPageAsV1(key, new AsyncCallback<WikiPage>() {
			@Override
			public void onSuccess(WikiPage page) {
				view.showInfoDialog("Wiki Source", page.getMarkdown());
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		});	
	}
	
	private void onEditWiki() {
		// Validate the user can update this entity.
		preflightController.checkUpdateEntity(this.entityBundle, new Callback() {
			@Override
			public void invoke() {
				postCheckEditWiki();
			}
		});
	}
	
	private void postCheckEditWiki(){
		//markdown editor will create a wiki if it does not already exist
		WikiPageKey key = new WikiPageKey(this.entityBundle.getEntity().getId(), ObjectType.ENTITY.name(), wikiPageId);
		wikiEditor.configure(key, new CallbackP<WikiPage>() {
			@Override
			public void invoke(WikiPage param) {
				entityUpdateHandler.onPersistSuccess(new EntityUpdatedEvent());
			}
		});
	}

	private void onAddWikiSubpage() {
		// Validate the user can update this entity.
		preflightController.checkUpdateEntity(this.entityBundle, new Callback() {
			@Override
			public void invoke() {
				postCheckAddWikiSubpage();
			}
		});
	}
	
	private void postCheckAddWikiSubpage(){
		if (entityBundle.getRootWikiId() == null) {
			createWikiPage("Root");
		} else {
			view.showPromptDialog(DisplayConstants.ENTER_PAGE_TITLE, new PromptCallback() {
				@Override
				public void callback(String result) {
					createWikiPage(result);
				}
			});
		}
	}
	
	public void createWikiPage(final String name) {
		if (DisplayUtils.isDefined(name)) {
			WikiPage page = new WikiPage();
			page.setParentWikiId(wikiPageId);
			page.setTitle(name);
	        synapseClient.createV2WikiPageWithV1(entityBundle.getEntity().getId(), ObjectType.ENTITY.name(), page, new AsyncCallback<WikiPage>() {
	            @Override
	            public void onSuccess(WikiPage result) {
	                view.showInfo("'" + name + "' Page Added", "");
	                entityUpdateHandler.onPersistSuccess(new EntityUpdatedEvent());
	            }
	            @Override
	            public void onFailure(Throwable caught) {
	                if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view))
	                    view.showErrorMessage(DisplayConstants.ERROR_PAGE_CREATION_FAILED + ": " + caught.getMessage());
	            }
	        });
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
