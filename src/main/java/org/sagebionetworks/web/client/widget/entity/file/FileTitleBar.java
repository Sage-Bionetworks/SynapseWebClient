package org.sagebionetworks.web.client.widget.entity.file;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

import java.util.List;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.repo.model.file.DownloadList;
import org.sagebionetworks.repo.model.file.ExternalFileHandle;
import org.sagebionetworks.repo.model.file.ExternalObjectStoreFileHandle;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.S3FileHandleInterface;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.events.DownloadListUpdatedEvent;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.clienthelp.FileClientsHelp;
import org.sagebionetworks.web.client.widget.entity.EntityBadge;
import org.sagebionetworks.web.shared.PaginatedResults;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileTitleBar implements SynapseWidgetPresenter, FileTitleBarView.Presenter {
	
	private FileTitleBarView view;
	private EntityBundle entityBundle;
	private SynapseProperties synapseProperties;
	private FileDownloadMenuItem fileDownloadMenuItem;
	private SynapseClientAsync synapseClient;
	private SynapseJavascriptClient jsClient;
	private FileClientsHelp fileClientsHelp;
	private EventBus eventBus;
	private SynapseJSNIUtils jsniUtils;
	@Inject
	public FileTitleBar(FileTitleBarView view, 
			SynapseProperties synapseProperties,
			FileDownloadMenuItem fileDownloadButton,
			SynapseClientAsync synapseClient,
			SynapseJavascriptClient jsClient,
			FileClientsHelp fileClientsHelp,
			EventBus eventBus,
			SynapseJSNIUtils jsniUtils) {
		this.view = view;
		this.synapseProperties = synapseProperties;
		this.fileDownloadMenuItem = fileDownloadButton;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.jsClient = jsClient;
		this.fileClientsHelp = fileClientsHelp;
		this.eventBus = eventBus;
		this.jsniUtils = jsniUtils;
		view.setFileDownloadMenuItem(fileDownloadButton.asWidget());
		view.setPresenter(this);
	}
	
	public void configure(EntityBundle bundle) {
		this.entityBundle = bundle;
		
		view.setVersionUIVisible(false);
		view.setExternalUrlUIVisible(false);
		view.setExternalObjectStoreUIVisible(false);
		view.setFileSize("");
		
		view.createTitlebar(bundle.getEntity());
		fileDownloadMenuItem.configure(bundle);
		
		FileHandle fileHandle = DisplayUtils.getFileHandle(entityBundle);
		boolean isFilenamePanelVisible = fileHandle != null;
		view.setFilenameContainerVisible(isFilenamePanelVisible);
		view.setEntityName(bundle.getEntity().getName());
		view.setVersion(((FileEntity)entityBundle.getEntity()).getVersionNumber());
		getLatestVersion();
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
	
	public void getLatestVersion() {
		// determine if we should show report the version (only shows if we're looking at an older version of the file).
		synapseClient.getEntityVersions(entityBundle.getEntity().getId(), 0, 1,
			new AsyncCallback<PaginatedResults<VersionInfo>>() {
				@Override
				public void onSuccess(PaginatedResults<VersionInfo> result) {
					List<VersionInfo> versions =  result.getResults();
					if (!versions.isEmpty()) {
						Long currentVersionNumber = versions.get(0).getVersionNumber();
						Long viewingVersionNumber = ((FileEntity)entityBundle.getEntity()).getVersionNumber();
						view.setVersionUIVisible(!currentVersionNumber.equals(viewingVersionNumber));
					}
				}

				@Override
				public void onFailure(Throwable caught) {
					view.showErrorMessage(caught.getMessage());
				}
			});
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
		this.entityBundle = null;		
	}

	/**
	 * Does nothing. Use asWidget(Entity)
	 */
	@Override
	public Widget asWidget() {
		return view.asWidget();
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
		Long synapseStorageLocationId = Long.valueOf(synapseProperties.getSynapseProperty("org.sagebionetworks.portal.synapse_storage_id"));
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
		view.setExternalObjectStoreUIVisible(true);
		view.setExternalObjectStoreInfo(externalFileHandle.getEndpointUrl(), externalFileHandle.getBucket(), externalFileHandle.getFileKey());
		view.setFileLocation("| External Object Store");
	}
	
	@Override
	public void onAddToDownloadList() {
		// TODO: add special popup to report how many items are in the current download list, and link to download list.
		FileEntity entity = (FileEntity)entityBundle.getEntity();
		jsClient.addFileToDownloadList(entity.getDataFileHandleId(), entity.getId(), new AsyncCallback<DownloadList>() {
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
			@Override
			public void onSuccess(DownloadList result) {
				jsniUtils.sendAnalyticsEvent(AddToDownloadList.DOWNLOAD_ACTION_EVENT_NAME, AddToDownloadList.FILES_ADDED_TO_DOWNLOAD_LIST_EVENT_NAME, "1");
				view.showAddedToDownloadListAlert(entity.getName() + EntityBadge.ADDED_TO_DOWNLOAD_LIST);
				eventBus.fireEvent(new DownloadListUpdatedEvent());
			}
		});
	}
	
	@Override
	public void onProgrammaticDownloadOptions() {
		FileEntity entity = (FileEntity)entityBundle.getEntity();
		fileClientsHelp.configureAndShow(entity.getId(), entity.getVersionNumber());
	}
}
