package org.sagebionetworks.web.client.widget.entity.menu;

import java.util.List;

import org.sagebionetworks.repo.model.AutoGenFactory;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Link;
import org.sagebionetworks.repo.model.LocationData;
import org.sagebionetworks.repo.model.Locationable;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.file.ExternalFileHandle;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.S3FileHandleInterface;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.EntityEditor;
import org.sagebionetworks.web.shared.EntityType;
import org.sagebionetworks.web.shared.exceptions.BadRequestException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.UnauthorizedException;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ActionMenu implements ActionMenuView.Presenter, SynapseWidgetPresenter {
	
	private ActionMenuView view;
	private AuthenticationController authenticationController;
	private GlobalApplicationState globalApplicationState;
	private EntityBundle entityBundle;
	private EntityTypeProvider entityTypeProvider;
	private SynapseClientAsync synapseClient;
	private JSONObjectAdapter jsonObjectAdapter;
	private EntityEditor entityEditor;
	private AutoGenFactory entityFactory;
	private boolean readOnly = false;
	private EntityUpdatedHandler entityUpdatedHandler;
	private SynapseJSNIUtils synapseJSNIUtils;
	private CookieProvider cookieProvider;
	
	@Inject
	public ActionMenu(ActionMenuView view, NodeModelCreator nodeModelCreator,
			AuthenticationController authenticationController,
			EntityTypeProvider entityTypeProvider,
			GlobalApplicationState globalApplicationState,
			SynapseClientAsync synapseClient,
			JSONObjectAdapter jsonObjectAdapter, EntityEditor entityEditor,
			AutoGenFactory entityFactory,
			SynapseJSNIUtils synapseJSNIUtils,
			CookieProvider cookieProvider) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.entityTypeProvider = entityTypeProvider;
		this.globalApplicationState = globalApplicationState;
		this.synapseClient = synapseClient;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.entityEditor = entityEditor;
		this.entityFactory = entityFactory;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.cookieProvider = cookieProvider;
		view.setPresenter(this);
	}	
	
	public Widget asWidget(EntityBundle bundle, boolean isAdministrator, boolean canEdit, boolean readOnly) {		
		view.setPresenter(this);
		this.entityBundle = bundle; 		
		this.readOnly = readOnly;

		// Get EntityType
		EntityType entityType = entityTypeProvider.getEntityTypeForEntity(bundle.getEntity());
		
		view.createMenu(bundle, entityType, authenticationController, isAdministrator, canEdit, readOnly, DisplayUtils.isInTestWebsite(cookieProvider));
		return view.asWidget();
	}

	public void clearState() {
		view.clear();
		// remove handlers
		entityUpdatedHandler = null;
		this.entityBundle = null;		
	}

	/**
	 * Does nothing. Use asWidget(Entity)
	 */
	@Override
	public Widget asWidget() {
		return null;
	}
    
	@Override
	public void fireEntityUpdatedEvent() {
		if (entityUpdatedHandler != null)
			entityUpdatedHandler.onPersistSuccess(new EntityUpdatedEvent());
	}
	
	public void setEntityUpdatedHandler(EntityUpdatedHandler handler) {
		this.entityUpdatedHandler = handler;
		entityEditor.setEntityUpdatedHandler(handler);
	}

	@Override
	public void moveEntity(String newParentId) {
		if(readOnly) {
			view.showErrorMessage(DisplayConstants.ERROR_IN_READ_ONLY_MODE);
			return;
		}
		
		final EntityType entityType = entityTypeProvider.getEntityTypeForEntity(entityBundle.getEntity());
		final String entityTypeDisplay = entityTypeProvider.getEntityDispalyName(entityType);
		
		entityBundle.getEntity().setParentId(newParentId);		
		JSONObjectAdapter adapter = jsonObjectAdapter.createNew();
		try {
			entityBundle.getEntity().writeToJSONObject(adapter);
		} catch (JSONObjectAdapterException e) {
			view.showErrorMessage(DisplayConstants.ERROR_GENERIC_NOTIFY);
		}
		
		// update the entity
		synapseClient.createOrUpdateEntity(adapter.toJSONString(), null, false, new AsyncCallback<String>() {			
			@Override
			public void onSuccess(String result) {				
				view.showInfo(entityTypeDisplay + " Moved", "The " + entityTypeDisplay + " was successfully moved."); 
				// Reload this entity
				globalApplicationState.getPlaceChanger().goTo(new Synapse(entityBundle.getEntity().getId()));
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
				}
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.getLoggedInUser())) {
					view.showErrorMessage(DisplayConstants.ERROR_ENTITY_MOVE_FAILURE);			
				}
			}
		});
	}

	@Override
	public void deleteEntity() {
		if(readOnly) {
			view.showErrorMessage(DisplayConstants.ERROR_IN_READ_ONLY_MODE);
			return;
		}
		
		final String parentId = entityBundle.getEntity().getParentId();
		final EntityType entityType = entityTypeProvider.getEntityTypeForEntity(entityBundle.getEntity());
		final String entityTypeDisplay = entityTypeProvider.getEntityDispalyName(entityType);
		synapseClient.deleteEntityById(entityBundle.getEntity().getId(), new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {				
				view.showInfo(entityTypeDisplay + " Deleted", "The " + entityTypeDisplay + " was successfully deleted."); 
				// Go to entity's parent
				Place gotoPlace = null;
				if(parentId != null && !Project.class.getName().equals(entityBundle.getEntity().getEntityType())) {
					gotoPlace = new Synapse(parentId);
				} else {
					gotoPlace = new Home(DisplayUtils.DEFAULT_PLACE_TOKEN);
				}
					
				globalApplicationState.getPlaceChanger().goTo(gotoPlace);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.getLoggedInUser())) {
					view.showErrorMessage(DisplayConstants.ERROR_ENTITY_DELETE_FAILURE);			
				}
			}
		});
	}	
	
	@Override
	public boolean isUserLoggedIn() {
		return authenticationController.getLoggedInUser() != null;
	}

	@Override
	public void onEdit() {
		// Edit this entity.
		entityEditor.editEntity(entityBundle, false);
	}

	@Override
	public void addNewChild(EntityType type, String parentId) {
		entityEditor.addNewEntity(type, parentId);
		
	}

	@Override
	public void createLink(String selectedEntityId) {			
		Link link = (Link) entityFactory.newInstance(Link.class.getName());
		link.setParentId(selectedEntityId); // user selects where to save
		Reference ref = new Reference();
		ref.setTargetId(entityBundle.getEntity().getId());
		link.setLinksTo(ref); // links to this entity
		link.setLinksToClassName(entityBundle.getEntity().getEntityType());
		link.setName(entityBundle.getEntity().getName()); // copy name of this entity as default
		link.setEntityType(Link.class.getName());		
		
		JSONObjectAdapter adapter = jsonObjectAdapter.createNew();
		try {
			link.writeToJSONObject(adapter);
		} catch (JSONObjectAdapterException e) {
			view.showErrorMessage(DisplayConstants.ERROR_GENERIC_NOTIFY);
		}		
		
		// create the link
		synapseClient.createOrUpdateEntity(adapter.toJSONString(), null, true, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				view.showInfo(DisplayConstants.TEXT_LINK_SAVED, DisplayConstants.TEXT_LINK_SAVED);
			}
			@Override
			public void onFailure(Throwable caught) {
				if(caught instanceof BadRequestException) {
					view.showErrorMessage(DisplayConstants.ERROR_CANT_SAVE_LINK_HERE);
					return;
				}
				if(caught instanceof NotFoundException) {
					view.showErrorMessage(DisplayConstants.ERROR_NOT_FOUND);
					return;
				}
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.getLoggedInUser())) {
					view.showErrorMessage(DisplayConstants.ERROR_GENERIC);
				}
			}
		});
		
	}
	
	@Override
	public void uploadToGenomespace() {
		String url = null;
		if(entityBundle.getEntity() instanceof Locationable) {
			Locationable locationable = (Locationable)entityBundle.getEntity();
			List<LocationData> locs = locationable.getLocations();
			if(locs != null && locs.size() > 0) {
				LocationData ld = locs.get(0);
				if(ld != null) {
					url = ld.getPath();
				}
				showUploadGenomeSpaceWindow(url, null);
			}
		} else if(entityBundle.getEntity() instanceof FileEntity) {
			final FileEntity fileEntity = (FileEntity)entityBundle.getEntity();
			FileHandle fileHandle = DisplayUtils.getFileHandle(entityBundle);
			if(fileHandle != null) {
				if (fileHandle instanceof ExternalFileHandle) {
					url = ((ExternalFileHandle) fileHandle).getExternalURL();
					showUploadGenomeSpaceWindow(url, null);
				}
				else if (fileHandle instanceof S3FileHandleInterface){
					synapseClient.getFileEntityTemporaryUrlForVersion(fileEntity.getId(), fileEntity.getVersionNumber(), new AsyncCallback<String>() {
						@Override
						public void onSuccess(String realUrl) {
							if(realUrl != null && !realUrl.equals(""))
								showUploadGenomeSpaceWindow(realUrl, fileEntity.getName());
						}
						@Override
						public void onFailure(Throwable caught) {
							view.showErrorMessage(DisplayConstants.ERROR_GENERIC);
						}
					});							
				}
			}
		}				

	}
	
	/*
	 * Private Methods
	 */
	private void showUploadGenomeSpaceWindow(String url, String fileName) {
		if(url == null || url.equals("")) {
			view.showErrorMessage("This entity does not contain a file to upload.");
		} else {
			if(fileName != null)
				synapseJSNIUtils.uploadUrlToGenomeSpace(url, fileName);
			else
				synapseJSNIUtils.uploadUrlToGenomeSpace(url);
		}
	}
	
}
