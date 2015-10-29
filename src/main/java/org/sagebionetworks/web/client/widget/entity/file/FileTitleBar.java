package org.sagebionetworks.web.client.widget.entity.file;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.EntityTypeUtils;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.S3FileHandleInterface;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileTitleBar implements FileTitleBarView.Presenter, SynapseWidgetPresenter {
	
	private FileTitleBarView view;
	private AuthenticationController authenticationController;
	private EntityUpdatedHandler entityUpdatedHandler;
	private EntityBundle entityBundle;
	private SynapseClientAsync synapseClient;
	private GlobalApplicationState globalAppState;
	
	@Inject
	public FileTitleBar(FileTitleBarView view, AuthenticationController authenticationController,
			SynapseClientAsync synapseClient, GlobalApplicationState globalAppState) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.synapseClient = synapseClient;
		this.globalAppState = globalAppState;
		view.setPresenter(this);
	}	
	
	public void configure(EntityBundle bundle) {		
		view.setPresenter(this);
		this.entityBundle = bundle;

		// Get EntityType
		EntityType entityType = EntityTypeUtils.getEntityTypeForClass(bundle.getEntity().getClass());
		view.createTitlebar(bundle, entityType, authenticationController);
	}
	
	/**
	 * For unit testing. call asWidget with the new Entity for the view to be in sync.
	 * @param bundle
	 */
	public void setEntityBundle(EntityBundle bundle) {
		this.entityBundle = bundle;
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
		return view.asWidget();
	}
    
	@Override
	public void fireEntityUpdatedEvent(EntityUpdatedEvent event) {
		if (entityUpdatedHandler != null)
			entityUpdatedHandler.onPersistSuccess(event);
	}
	
	public void setEntityUpdatedHandler(EntityUpdatedHandler handler) {
		this.entityUpdatedHandler = handler;
	}

	@Override
	public boolean isUserLoggedIn() {
		return authenticationController.isLoggedIn();
	}

	
	public static boolean isDataPossiblyWithin(FileEntity fileEntity) {
		String dataFileHandleId = fileEntity.getDataFileHandleId();
		return (dataFileHandleId != null && dataFileHandleId.length() > 0);
	}

	
	public void queryForSftpLoginInstructions(String url) {
		synapseClient.getHost(url, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String host) {
				//update the download login dialog message
				view.setLoginInstructions(DisplayConstants.DOWNLOAD_CREDENTIALS_REQUIRED + SafeHtmlUtils.htmlEscape(host));
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		});
	}

	@Override
	public void setS3Description() {
		FileHandle fileHandle = DisplayUtils.getFileHandle(entityBundle);
		if (fileHandle instanceof S3FileHandleInterface){
			S3FileHandleInterface s3FileHandle = (S3FileHandleInterface)fileHandle;
			Long synapseStorageLocationId = Long.valueOf(globalAppState.getSynapseProperty("org.sagebionetworks.portal.synapse_storage_id"));
			// Uploads to Synapse Storage often do not get their storage location field back-filled,
			// so null also indicates a Synapse-Stored file
			if (s3FileHandle.getStorageLocationId() == null || 
					synapseStorageLocationId.equals(s3FileHandle.getStorageLocationId())) {
				view.setFileLocation("| Synapse Storage");				
			} else {
				String description = "| s3://" + s3FileHandle.getBucketName() + "/";
				if (s3FileHandle.getKey() != null) {
					description += s3FileHandle.getKey();
				};
				view.setFileLocation(description);
			}
		}
	}


	/*
	 * Private Methods
	 */
}
