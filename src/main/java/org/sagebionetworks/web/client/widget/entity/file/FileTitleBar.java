package org.sagebionetworks.web.client.widget.entity.file;

import java.util.List;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.file.CloudProviderFileHandleInterface;
import org.sagebionetworks.repo.model.file.DownloadList;
import org.sagebionetworks.repo.model.file.ExternalFileHandle;
import org.sagebionetworks.repo.model.file.ExternalObjectStoreFileHandle;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.GoogleCloudFileHandle;
import org.sagebionetworks.repo.model.file.S3FileHandle;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.events.DownloadListUpdatedEvent;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.clienthelp.FileClientsHelp;
import org.sagebionetworks.web.client.widget.entity.EntityBadge;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileTitleBar implements SynapseWidgetPresenter, FileTitleBarView.Presenter {

	private FileTitleBarView view;
	private EntityBundle entityBundle;
	private SynapseProperties synapseProperties;
	private FileDownloadMenuItem fileDownloadMenuItem;
	private SynapseJavascriptClient jsClient;
	private FileClientsHelp fileClientsHelp;
	private EventBus eventBus;
	private SynapseJSNIUtils jsniUtils;

	@Inject
	public FileTitleBar(FileTitleBarView view, SynapseProperties synapseProperties, FileDownloadMenuItem fileDownloadButton, SynapseJavascriptClient jsClient, FileClientsHelp fileClientsHelp, EventBus eventBus, SynapseJSNIUtils jsniUtils) {
		this.view = view;
		this.synapseProperties = synapseProperties;
		this.fileDownloadMenuItem = fileDownloadButton;
		this.jsClient = jsClient;
		this.fileClientsHelp = fileClientsHelp;
		this.eventBus = eventBus;
		this.jsniUtils = jsniUtils;
		view.setFileDownloadMenuItem(fileDownloadButton.asWidget());
		view.setPresenter(this);
	}

	public void configure(EntityBundle bundle) {
		this.entityBundle = bundle;
		view.setCanDownload(entityBundle.getPermissions().getCanDownload());
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
		view.setVersion(((FileEntity) entityBundle.getEntity()).getVersionNumber());
		getLatestVersion();
		if (isFilenamePanelVisible) {
			if (fileHandle.getContentMd5() != null) {
				view.setMd5(fileHandle.getContentMd5());
			}
			if (fileHandle.getContentSize() != null) {
				view.setFileSize("| " + DisplayUtils.getFriendlySize(fileHandle.getContentSize().doubleValue(), true));
			}
			view.setFilename(entityBundle.getFileName());
			// don't ask for the size if it's external, just display that this is external data
			if (fileHandle instanceof ExternalFileHandle) {
				configureExternalFile((ExternalFileHandle) fileHandle);
			} else if (fileHandle instanceof CloudProviderFileHandleInterface) {
				configureCloudProviderFile((CloudProviderFileHandleInterface) fileHandle);
			} else if (fileHandle instanceof ExternalObjectStoreFileHandle) {
				configureExternalObjectStore((ExternalObjectStoreFileHandle) fileHandle);
			}
		}
	}

	public void getLatestVersion() {
		// determine if we should show report the version (only shows if we're looking at an older version
		// of the file).
		jsClient.getEntityVersions(entityBundle.getEntity().getId(), 0, 1, new AsyncCallback<List<VersionInfo>>() {
			@Override
			public void onSuccess(List<VersionInfo> results) {
				if (!results.isEmpty()) {
					Long currentVersionNumber = results.get(0).getVersionNumber();
					Long viewingVersionNumber = ((FileEntity) entityBundle.getEntity()).getVersionNumber();
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
	 * 
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

	public void configureCloudProviderFile(CloudProviderFileHandleInterface filehandle) {
		Long synapseStorageLocationId = Long.valueOf(synapseProperties.getSynapseProperty("org.sagebionetworks.portal.synapse_storage_id"));
		// Uploads to Synapse Storage often do not get their storage location field back-filled,
		// so null also indicates a Synapse-Stored file
		if (filehandle.getStorageLocationId() == null || synapseStorageLocationId.equals(filehandle.getStorageLocationId())) {
			view.setFileLocation("| Synapse Storage");
		} else if (filehandle instanceof GoogleCloudFileHandle) {
			String description = "| gs://" + filehandle.getBucketName() + "/";
			if (filehandle.getKey() != null) {
				description += filehandle.getKey();
			}
			view.setFileLocation(description);
		} else if (filehandle instanceof S3FileHandle) {
			String description = "| s3://" + filehandle.getBucketName() + "/";
			if (filehandle.getKey() != null) {
				description += filehandle.getKey();
			}
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
		// TODO: add special popup to report how many items are in the current download list, and link to
		// download list.
		FileEntity entity = (FileEntity) entityBundle.getEntity();
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
		FileEntity entity = (FileEntity) entityBundle.getEntity();
		fileClientsHelp.configureAndShow(entity.getId(), entity.getVersionNumber());
	}
}
