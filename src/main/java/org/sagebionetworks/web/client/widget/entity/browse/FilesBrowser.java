package org.sagebionetworks.web.client.widget.entity.browse;

import org.sagebionetworks.repo.model.AutoGenFactory;
import org.sagebionetworks.repo.model.Data;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.Locationable;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.shared.EntityWrapper;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FilesBrowser implements FilesBrowserView.Presenter, SynapseWidgetPresenter {
	
	private FilesBrowserView view;
	private HandlerManager handlerManager = new HandlerManager(this);
	private String configuredEntityId;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	private AdapterFactory adapterFactory;
	private AutoGenFactory autogenFactory;
	
	@Inject
	public FilesBrowser(FilesBrowserView view, SynapseClientAsync synapseClient, NodeModelCreator nodeModelCreator,
			AdapterFactory adapterFactory, AutoGenFactory autogenFactory) {
		this.view = view;		
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.adapterFactory = adapterFactory;
		this.autogenFactory = autogenFactory;
		
		view.setPresenter(this);
	}	
	
	/**
	 * Configure tree view with given entityId's children as start set
	 * @param entityId
	 */
	public void configure(String entityId) {
		this.configuredEntityId = entityId;		
		view.configure(entityId);
	}
	
	public void configure(String entityId, String title) {
		this.configuredEntityId = entityId;
		view.configure(entityId, title);
	}
	
	public void clearState() {
		view.clear();
		// remove handlers
		handlerManager = new HandlerManager(this);		
	}
	
	@Override
	public void fireEntityUpdatedEvent() {
		handlerManager.fireEvent(new EntityUpdatedEvent());
	}
	
	public void addEntityUpdatedHandler(EntityUpdatedHandler handler) {
		handlerManager.addHandler(EntityUpdatedEvent.getType(), handler);
	}

	
	@Override
	public Widget asWidget() {
		view.setPresenter(this);		
		return view.asWidget();
	}

	@Override
	public void createFolder(final String name) {
		Entity folder = createNewEntity(Folder.class.getName(), configuredEntityId);
		folder.setName(name);
		String entityJson;
		try {
			entityJson = folder.writeToJSONObject(adapterFactory.createNew()).toJSONString();
			synapseClient.createOrUpdateEntity(entityJson, null, true, new AsyncCallback<String>() {
				@Override
				public void onSuccess(String newId) {
					view.showInfo("Folder '" + name + "' Added", "");
					view.refreshTreeView(configuredEntityId);
				}
				
				@Override
				public void onFailure(Throwable caught) {
					view.showErrorMessage(DisplayConstants.ERROR_FOLDER_CREATION_FAILED);
				}			
			});
		} catch (JSONObjectAdapterException e) {			
			view.showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);		
		}
	}

	@Override
	public void createEntityForUpload(final AsyncCallback<Entity> asyncCallback) {
		final Entity file = createNewEntity(Data.class.getName(), configuredEntityId);
		try {
			String entityJson = file.writeToJSONObject(adapterFactory.createNew()).toJSONString();
			synapseClient.createOrUpdateEntity(entityJson, null, true, new AsyncCallback<String>() {
				@Override
				public void onSuccess(String newId) {
					file.setId(newId);
					asyncCallback.onSuccess(file);
				}
				@Override
				public void onFailure(Throwable caught) {
					asyncCallback.onFailure(caught);
				}
			});
		} catch (JSONObjectAdapterException e) {
			view.showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);
		}
	}

	@Override
	public void renameChildToFilename(String entityId) {
		// TODO : rename given child to its uploaded file name
		synapseClient.getEntity(entityId, new AsyncCallback<EntityWrapper>() {
			@Override
			public void onSuccess(EntityWrapper result) {
				Entity entity;
				try {
					entity = nodeModelCreator.createEntity(result);
					if(entity instanceof Locationable) {
						Locationable locationable = (Locationable)entity;						
						if(locationable.getLocations() != null && locationable.getLocations().size() > 0) {																					
							String locationPath = locationable.getLocations().get(0).getPath();
							String filename = DisplayUtils.getFileNameFromLocationPath(locationPath);
							updateEntityName(entity, filename);
							return;
						}
					}
				} catch (JSONObjectAdapterException e) {
					view.showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION); 
				}
				
				view.refreshTreeView(configuredEntityId);
			}
			@Override
			public void onFailure(Throwable caught) {
				// cant rename, but file was uploaded
				view.refreshTreeView(configuredEntityId);
			}			
		});
		
	}

	/*
	 * private methods
	 */
	private void updateEntityName(Entity entity, String name) throws JSONObjectAdapterException {
		entity.setName(name);
		String entityJson = entity.writeToJSONObject(adapterFactory.createNew()).toJSONString(); 
		synapseClient.updateEntity(entityJson, new AsyncCallback<EntityWrapper>() {
			@Override
			public void onSuccess(EntityWrapper result) {
				view.refreshTreeView(configuredEntityId);
			}

			@Override
			public void onFailure(Throwable caught) {
				view.refreshTreeView(configuredEntityId);
			}
		});
	}
	
	private Entity createNewEntity(String className, String parentId) {
		Entity entity = (Entity) autogenFactory.newInstance(className);
		entity.setParentId(parentId);
		entity.setEntityType(className);		
		return entity;
	}

}
