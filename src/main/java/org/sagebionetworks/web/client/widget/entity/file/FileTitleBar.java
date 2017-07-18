package org.sagebionetworks.web.client.widget.entity.file;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.file.ExternalFileHandle;
import org.sagebionetworks.repo.model.file.ExternalObjectStoreFileHandle;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.S3FileHandleInterface;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileTitleBar implements FileTitleBarView.Presenter, SynapseWidgetPresenter {
	
	private FileTitleBarView view;
	private AuthenticationController authenticationController;
	private EntityUpdatedHandler entityUpdatedHandler;
	private EntityBundle entityBundle;
	private GlobalApplicationState globalAppState;
	private FileDownloadButton fileDownloadButton;
	@Inject
	public FileTitleBar(FileTitleBarView view, 
			AuthenticationController authenticationController,
			GlobalApplicationState globalAppState,
			FileDownloadButton fileDownloadButton) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.globalAppState = globalAppState;
		this.fileDownloadButton = fileDownloadButton;
		view.setPresenter(this);
		view.setFileDownloadButton(fileDownloadButton.asWidget());
	}	
	
	public void configure(EntityBundle bundle) {
		view.setPresenter(this);
		this.entityBundle = bundle;
		
		view.setExternalUrlUIVisible(false);
		view.setFileSize("");
		
		view.createTitlebar(bundle.getEntity());
		fileDownloadButton.configure(bundle);
		
		FileHandle fileHandle = DisplayUtils.getFileHandle(entityBundle);
		boolean isFilenamePanelVisible = fileHandle != null;
		view.setFilenameContainerVisible(isFilenamePanelVisible);
		view.setEntityName(bundle.getEntity().getName());
		if (isFilenamePanelVisible) {
			if (fileHandle.getContentMd5() != null) {
				view.setMd5(fileHandle.getContentMd5());
			}
			if (fileHandle.getContentSize() != null) {
				view.setFileSize("| "+DisplayUtils.getFriendlySize(fileHandle.getContentSize().doubleValue(), true));
			}
			view.setFilename(entityBundle.getFileName());
			//don't ask for the size if it's external, just display that this is external data
			if (fileHandle instanceof ExternalFileHandle) {
				configureExternalFile((ExternalFileHandle)fileHandle);
			} else if (fileHandle instanceof S3FileHandleInterface){
				configureS3File((S3FileHandleInterface)fileHandle);
			} else if (fileHandle instanceof ExternalObjectStoreFileHandle) {
				configureExternalObjectStore((ExternalObjectStoreFileHandle)fileHandle);
			}
		}
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
		fileDownloadButton.setEntityUpdatedHandler(handler);
	}

	@Override
	public boolean isUserLoggedIn() {
		return authenticationController.isLoggedIn();
	}

	
	public static boolean isDataPossiblyWithin(FileEntity fileEntity) {
		String dataFileHandleId = fileEntity.getDataFileHandleId();
		return (dataFileHandleId != null && dataFileHandleId.length() > 0);
	}
	public void configureExternalFile(ExternalFileHandle externalFileHandle) {
		view.setExternalUrlUIVisible(true);
		view.setExternalUrl(externalFileHandle.getExternalURL());
		view.setFileLocation("| External Storage");
	}

	public void configureS3File(S3FileHandleInterface s3FileHandle) {
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
	
	public void configureExternalObjectStore(ExternalObjectStoreFileHandle externalFileHandle) {
		view.setExternalUrlUIVisible(true);
		view.setExternalUrl(externalFileHandle.getEndpointUrl() + "/" + externalFileHandle.getBucket() + "/" + externalFileHandle.getFileKey());
		view.setFileLocation("| External Object Store");
	}
}
