package org.sagebionetworks.web.client.widget.entity.browse;

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
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.shared.EntityWrapper;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FilesBrowser implements FilesBrowserView.Presenter, SynapseWidgetPresenter {
	
	private FilesBrowserView view;
	private String configuredEntityId;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	private AdapterFactory adapterFactory;
	private EntityUpdatedHandler entityUpdatedHandler;
	GlobalApplicationState globalApplicationState;
	AuthenticationController authenticationController;
	CookieProvider cookies;
	boolean isCertifiedUser,canCertifiedUserAddChild;
	private String currentFolderEntityId;
	
	@Inject
	public FilesBrowser(FilesBrowserView view,
			SynapseClientAsync synapseClient,
			NodeModelCreator nodeModelCreator, 
			AdapterFactory adapterFactory,
			GlobalApplicationState globalApplicationState,
			AuthenticationController authenticationController,
			CookieProvider cookies) {
		this.view = view;		
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.adapterFactory = adapterFactory;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.cookies = cookies;
		view.setPresenter(this);
	}	
	
	/**
	 * Configure tree view with given entityId's children as start set
	 * @param entityId
	 */
	public void configure(String entityId, boolean canCertifiedUserAddChild, boolean isCertifiedUser) {
		view.clear();
		this.configuredEntityId = entityId;
		this.isCertifiedUser = isCertifiedUser;
		this.canCertifiedUserAddChild = canCertifiedUserAddChild;
		view.configure(entityId, canCertifiedUserAddChild);
		currentFolderEntityId = null;
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
		uploadButtonClicked(configuredEntityId, view, synapseClient, authenticationController, isCertifiedUser);
	}

	/**
	 * Check for user certification passing record.  NOTE: This should be removed after certification is required 
	 * (we will check the permissions up front and block on click), 
	 * and calls to this method should be replaced with a direct call to "view.showUploadDialog(entityId);"
	 * @param entityId
	 * @param view
	 * @param synapseClient
	 * @param cookies
	 * @param authenticationController
	 * @param isCertificationRequired
	 */
	public static void uploadButtonClicked(
			final String entityId, 
			final UploadView view,
			SynapseClientAsync synapseClient,
			AuthenticationController authenticationController,
			final boolean isCertifiedUser) {
		if (isCertifiedUser)
			view.showUploadDialog(entityId);
		else
			view.showQuizInfoDialog();
	}
	
	@Override
	public void addFolderClicked() {
		if (isCertifiedUser)
			createFolder();
		else
			view.showQuizInfoDialog();
	}
	
	
	public void createFolder() {
		Folder folder = new Folder();
		folder.setParentId(configuredEntityId);
		folder.setEntityType(Folder.class.getName());
		String entityJson;
		try {
			entityJson = folder.writeToJSONObject(adapterFactory.createNew()).toJSONString();
			synapseClient.createOrUpdateEntity(entityJson, null, true, new AsyncCallback<String>() {
				@Override
				public void onSuccess(String newId) {
					currentFolderEntityId = newId;
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
	public void deleteFolder(boolean skipTrashCan) {
		synapseClient.deleteEntityById(currentFolderEntityId, skipTrashCan, new AsyncCallback<Void>() {
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
	public void updateFolderName(final String newFolderName) {
		synapseClient.getEntity(currentFolderEntityId, new AsyncCallback<EntityWrapper>() {
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
	
	/**
	 * For testing purposes
	 * @return
	 */
	public String getCurrentFolderEntityId() {
		return currentFolderEntityId;
	};
	
	/**
	 * For testing purposes
	 * @return
	 */
	public void setCurrentFolderEntityId(String currentFolderEntityId) {
		this.currentFolderEntityId = currentFolderEntityId;
	}

	public void showUploadFile() {
		view.showUploadDialog(this.configuredEntityId);
	}
}
