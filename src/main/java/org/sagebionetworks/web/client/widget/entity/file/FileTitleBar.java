package org.sagebionetworks.web.client.widget.entity.file;

import java.util.List;

import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.repo.model.download.AddBatchOfFilesToDownloadListResponse;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.file.CloudProviderFileHandleInterface;
import org.sagebionetworks.repo.model.file.ExternalFileHandle;
import org.sagebionetworks.repo.model.file.ExternalObjectStoreFileHandle;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.GoogleCloudFileHandle;
import org.sagebionetworks.repo.model.file.S3FileHandle;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.events.DownloadListUpdatedEvent;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.clienthelp.FileClientsHelp;
import org.sagebionetworks.web.client.widget.entity.EntityBadge;
import org.sagebionetworks.web.client.widget.entity.VersionHistoryWidget;
import org.sagebionetworks.web.client.widget.entity.menu.v2.Action;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;

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
	private GlobalApplicationState globalAppState;
	private AuthenticationController authController;
	private CookieProvider cookies;
	private PopupUtilsView popupUtils;
	private ActionMenuWidget actionMenuWidget;
	private VersionHistoryWidget versionHistoryWidget;

	@Inject
	public FileTitleBar(FileTitleBarView view, SynapseProperties synapseProperties, FileDownloadMenuItem fileDownloadButton, SynapseJavascriptClient jsClient, FileClientsHelp fileClientsHelp, EventBus eventBus, SynapseJSNIUtils jsniUtils, GlobalApplicationState globalAppState, AuthenticationController authController, CookieProvider cookies, PopupUtilsView popupUtils) {
		this.view = view;
		this.synapseProperties = synapseProperties;
		this.fileDownloadMenuItem = fileDownloadButton;
		this.jsClient = jsClient;
		this.fileClientsHelp = fileClientsHelp;
		this.eventBus = eventBus;
		this.jsniUtils = jsniUtils;
		this.globalAppState = globalAppState;
		this.authController = authController;
		this.cookies = cookies;
		this.popupUtils = popupUtils;
		view.setFileDownloadMenuItem(fileDownloadButton.asWidget());
		view.setPresenter(this);
	}

	public void configure(EntityBundle bundle, ActionMenuWidget actionMenu, VersionHistoryWidget versionHistoryWidget) {
		this.entityBundle = bundle;
		this.actionMenuWidget = actionMenu;
		this.versionHistoryWidget = versionHistoryWidget;
		view.setCanDownload(entityBundle.getPermissions().getCanDownload());
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
		view.setActionMenu(actionMenuWidget);
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

		versionHistoryWidget.registerVisibilityChangeListener(visible ->
				view.setVersionHistoryLinkText((visible ? "Hide" : "Show") + " Version History"));
	}

	public void getLatestVersion() {
		// determine if we should report the version as "Current"
		jsClient.getEntityVersions(entityBundle.getEntity().getId(), 0, 1, new AsyncCallback<List<VersionInfo>>() {
			@Override
			public void onSuccess(List<VersionInfo> results) {
				if (!results.isEmpty()) {
					Long currentVersionNumber = results.get(0).getVersionNumber();
					Long viewingVersionNumber = ((FileEntity) entityBundle.getEntity()).getVersionNumber();
					view.setVersionUICurrentVisible(currentVersionNumber.equals(viewingVersionNumber));
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
		if (!authController.isLoggedIn()) {
			view.showErrorMessage("You will need to sign in to add a file to the Download List.");
			globalAppState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
		} else {
			// TODO: add special popup to report how many items are in the current download list, and link to
			// download list.
			FileEntity entity = (FileEntity) entityBundle.getEntity();

			jsClient.addFileToDownloadListV2(entity.getId(), entity.getVersionNumber(), new AsyncCallback<AddBatchOfFilesToDownloadListResponse>() {
				@Override
				public void onFailure(Throwable caught) {
					view.showErrorMessage(caught.getMessage());
				}
				public void onSuccess(AddBatchOfFilesToDownloadListResponse result) {
					String href = "#!DownloadCart:0";
					popupUtils.showInfo(entity.getName() + EntityBadge.ADDED_TO_DOWNLOAD_LIST, href, DisplayConstants.VIEW_DOWNLOAD_LIST);
					eventBus.fireEvent(new DownloadListUpdatedEvent());
				};
			});
		}
	}

	@Override
	public void toggleShowVersionHistory() {
		this.actionMenuWidget.onAction(Action.SHOW_VERSION_HISTORY);
	}

	@Override
	public void onProgrammaticDownloadOptions() {
		FileEntity entity = (FileEntity) entityBundle.getEntity();
		fileClientsHelp.configureAndShow(entity.getId(), entity.getVersionNumber());
	}
}
