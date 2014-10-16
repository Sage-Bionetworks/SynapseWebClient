package org.sagebionetworks.web.client.widget.entity.browse;

import org.sagebionetworks.repo.model.AutoGenFactory;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.UploadView;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.EntityAccessRequirementsWidget;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FilesBrowser implements FilesBrowserView.Presenter, SynapseWidgetPresenter {
	
	private FilesBrowserView view;
	private String configuredEntityId;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	private AdapterFactory adapterFactory;
	private AutoGenFactory autogenFactory;
	private EntityUpdatedHandler entityUpdatedHandler;
	GlobalApplicationState globalApplicationState;
	AuthenticationController authenticationController;
	CookieProvider cookies;
	EntityAccessRequirementsWidget accessRequirementsWidget;
	boolean canAddChild, canCertifiedUserAddChild;
	
	@Inject
	public FilesBrowser(FilesBrowserView view,
			SynapseClientAsync synapseClient,
			NodeModelCreator nodeModelCreator, AdapterFactory adapterFactory,
			AutoGenFactory autogenFactory,
			GlobalApplicationState globalApplicationState,
			AuthenticationController authenticationController,
			CookieProvider cookies,
			EntityAccessRequirementsWidget accessRequirementsWidget) {
		this.view = view;		
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.adapterFactory = adapterFactory;
		this.autogenFactory = autogenFactory;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.cookies = cookies;
		this.accessRequirementsWidget = accessRequirementsWidget;
		view.setPresenter(this);
	}	
	
	/**
	 * Configure tree view with given entityId's children as start set
	 * @param entityId
	 */
	public void configure(String entityId, boolean canAddChild, boolean canCertifiedUserAddChild) {
		this.configuredEntityId = entityId;
		this.canAddChild = canAddChild;
		this.canCertifiedUserAddChild = canCertifiedUserAddChild;
		view.configure(entityId, canCertifiedUserAddChild);
	}
	
	public void refresh() {
		if (configuredEntityId != null)
			view.configure(configuredEntityId, canCertifiedUserAddChild);
	}
	
	public void clearState() {
		view.clear();
		// remove handlers
		this.entityUpdatedHandler = null;		
	}
	
	@Override
	public void fireEntityUpdatedEvent() {
		if (entityUpdatedHandler != null)
			entityUpdatedHandler.onPersistSuccess(new EntityUpdatedEvent());
	}
	
	public void setEntityUpdatedHandler(EntityUpdatedHandler handler) {
		this.entityUpdatedHandler = handler;
	}

	
	@Override
	public Widget asWidget() {
		view.setPresenter(this);		
		return view.asWidget();
	}

	@Override
	public void uploadButtonClicked() {
		uploadButtonClickedStep1(accessRequirementsWidget, configuredEntityId, view, synapseClient, cookies, authenticationController, isCertificationRequired(canAddChild, canCertifiedUserAddChild));
	}
	
	public static boolean isCertificationRequired(boolean canAddChild, boolean canCertifiedUserAddChild) {
		return !canAddChild && canCertifiedUserAddChild;
	}
	
	//any access requirements to accept?
	public static void uploadButtonClickedStep1(
			EntityAccessRequirementsWidget accessRequirementsWidget, 
			final String entityId, 
			final UploadView view,
			final SynapseClientAsync synapseClient,
			final CookieProvider cookies,
			final AuthenticationController authenticationController,
			final boolean isCertificationRequired) {
		CallbackP<Boolean> callback = new CallbackP<Boolean>() {
			@Override
			public void invoke(Boolean accepted) {
				if (accepted)
					uploadButtonClickedStep2(entityId, view, synapseClient, cookies, authenticationController, isCertificationRequired);
			}
		};
		accessRequirementsWidget.showUploadAccessRequirements(entityId, callback);
	}

	//is this a certified user?
	public static void uploadButtonClickedStep2(
			final String entityId, 
			final UploadView view,
			SynapseClientAsync synapseClient,
			final CookieProvider cookies,
			AuthenticationController authenticationController,
			final boolean isCertificationRequired) {

		AsyncCallback<String> userCertifiedCallback = new AsyncCallback<String>() {
			@Override
			public void onSuccess(String passingRecord) {
				view.showUploadDialog(entityId);
			}

			@Override
			public void onFailure(Throwable t) {
				if (t instanceof NotFoundException) {
					view.showQuizInfoDialog(isCertificationRequired, new Callback() {
						@Override
						public void invoke() {
							// remind me later clicked
							view.showUploadDialog(entityId);
						}
					});
				} else
					view.showErrorMessage(t.getMessage());
			}
		};
		synapseClient.getCertifiedUserPassingRecord(authenticationController.getCurrentUserPrincipalId(), userCertifiedCallback);
	}
	
	@Override
	public void addFolderClicked() {
		createFolder();
	}
	
	public void createFolder() {
		Entity folder = createNewEntity(Folder.class.getName(), configuredEntityId);
		String entityJson;
		try {
			entityJson = folder.writeToJSONObject(adapterFactory.createNew()).toJSONString();
			synapseClient.createOrUpdateEntity(entityJson, null, true, new AsyncCallback<String>() {
				@Override
				public void onSuccess(String newId) {
					view.showFolderEditDialog(newId);
				}
				
				@Override
				public void onFailure(Throwable caught) {
					if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view))
						view.showErrorMessage(DisplayConstants.ERROR_FOLDER_CREATION_FAILED);
				}			
			});
		} catch (JSONObjectAdapterException e) {			
			view.showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);		
		}
	}
	
	@Override
	public void deleteFolder(String folderEntityId, boolean skipTrashCan) {
		synapseClient.deleteEntityById(folderEntityId, skipTrashCan, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void na) {
				//folder is deleted when folder creation is canceled.  refresh the tree for updated information 
				view.refreshTreeView(configuredEntityId);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(DisplayConstants.ERROR_FOLDER_DELETE_FAILED);
			}			
		});
	}
	
	public void updateFolderName(final Folder folder) {
		try {
			String entityJson = folder.writeToJSONObject(adapterFactory.createNew()).toJSONString();
			synapseClient.updateEntity(entityJson, new AsyncCallback<EntityWrapper>() {
				@Override
				public void onSuccess(EntityWrapper result) {
					view.showInfo("Folder '" + folder.getName() + "' Added", "");
					view.refreshTreeView(configuredEntityId);
				}
				@Override
				public void onFailure(Throwable caught) {
					view.showErrorMessage(DisplayConstants.ERROR_FOLDER_RENAME_FAILED);
				}
			});
		} catch (JSONObjectAdapterException e) {			
			view.showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);		
		}

	}
	
	@Override
	public void updateFolderName(final String newFolderName, String folderEntityId) {
		synapseClient.getEntity(folderEntityId, new AsyncCallback<EntityWrapper>() {
			@Override
			public void onSuccess(EntityWrapper result) {
				try {
					Folder folder = nodeModelCreator.createJSONEntity(result.getEntityJson(), Folder.class);
					folder.setName(newFolderName);
					updateFolderName(folder);
				} catch (JSONObjectAdapterException e) {			
					view.showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);		
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view))
					view.showErrorMessage(DisplayConstants.ERROR_FOLDER_CREATION_FAILED);
			}			
		});
	}
	
	
	/*
	 * Private Methods
	 */
	private Entity createNewEntity(String className, String parentId) {
		Entity entity = (Entity) autogenFactory.newInstance(className);
		entity.setParentId(parentId);
		entity.setEntityType(className);		
		return entity;
	}

}
