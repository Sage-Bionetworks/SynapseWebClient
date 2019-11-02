package org.sagebionetworks.web.client.widget.entity.download;

import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static org.sagebionetworks.repo.model.util.ModelConstants.VALID_ENTITY_NAME_REGEX;
import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import java.util.List;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.attachment.UploadResult;
import org.sagebionetworks.repo.model.attachment.UploadStatus;
import org.sagebionetworks.repo.model.file.ExternalGoogleCloudUploadDestination;
import org.sagebionetworks.repo.model.file.ExternalObjectStoreUploadDestination;
import org.sagebionetworks.repo.model.file.ExternalS3UploadDestination;
import org.sagebionetworks.repo.model.file.ExternalUploadDestination;
import org.sagebionetworks.repo.model.file.S3UploadDestination;
import org.sagebionetworks.repo.model.file.UploadDestination;
import org.sagebionetworks.repo.model.file.UploadType;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.ContentTypeUtils;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.SynapseJavascriptFactory.OBJECT_TYPE;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.callback.MD5Callback;
import org.sagebionetworks.web.client.events.CancelHandler;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.UploadSuccessHandler;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.dialog.AddAttachmentHelper;
import org.sagebionetworks.web.client.widget.upload.MultipartUploader;
import org.sagebionetworks.web.client.widget.upload.ProgressingFileUploadHandler;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.exceptions.ConflictException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.exceptions.UnauthorizedException;
import com.google.common.util.concurrent.FutureCallback;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * This Uploader class supports 2 use cases: B. File Entity, newer client browser: Direct multipart
 * upload to S3, using a PUT to presigned URLs. C. Upload to the NCI proxy.
 * 
 * Case B will be the most common case.
 */
public class Uploader implements UploaderView.Presenter, SynapseWidgetPresenter, ProgressingFileUploadHandler {

	public static final long OLD_BROWSER_MAX_SIZE = (long) ClientProperties.MB * 5; // 5MB
	private UploaderView view;
	private UploadSuccessHandler successHandler;
	private CancelHandler cancelHandler;
	private Entity entity;
	private String parentEntityId, currentFileParentEntityId;
	// set if we are uploading to an existing file entity
	private String entityId;
	private CallbackP<String> fileHandleIdCallback;
	private SynapseClientAsync synapseClient;
	private SynapseJSNIUtils synapseJsniUtils;
	private GlobalApplicationState globalAppState;
	private GWTWrapper gwt;
	MultipartUploader multiPartUploader;
	AuthenticationController authenticationController;

	private String[] fileNames;
	private String[] currentFilePath;
	private int currentFilePathElement;
	private int currIndex;
	private JavaScriptObject fileList;
	private NumberFormat percentFormat;
	private boolean fileHasBeenUploaded = false;
	private UploadType currentUploadType;
	private String currentExternalUploadUrl;
	private Long storageLocationId;
	private S3DirectUploader s3DirectUploader;
	private String bucketName, endpointUrl, keyPrefixUUID;
	private SynapseJavascriptClient jsClient;
	private SynapseProperties synapseProperties;
	private EventBus eventBus;

	@Inject
	public Uploader(UploaderView view, SynapseClientAsync synapseClient, SynapseJSNIUtils synapseJsniUtils, GWTWrapper gwt, AuthenticationController authenticationController, MultipartUploader multiPartUploader, GlobalApplicationState globalAppState, S3DirectUploader s3DirectUploader, SynapseJavascriptClient jsClient, SynapseProperties synapseProperties, EventBus eventBus) {

		this.view = view;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.synapseJsniUtils = synapseJsniUtils;
		this.gwt = gwt;
		this.percentFormat = gwt.getNumberFormat("##");
		this.authenticationController = authenticationController;
		this.globalAppState = globalAppState;
		this.multiPartUploader = multiPartUploader;
		this.s3DirectUploader = s3DirectUploader;
		this.jsClient = jsClient;
		this.synapseProperties = synapseProperties;
		this.eventBus = eventBus;
		view.setPresenter(this);
	}

	public Widget configure(Entity entity, String parentEntityId, CallbackP<String> fileHandleIdCallback, boolean isEntity) {
		this.view.setPresenter(this);
		this.entity = entity;
		this.entityId = entity != null ? entity.getId() : null;
		this.parentEntityId = parentEntityId;
		this.currentFileParentEntityId = parentEntityId;
		this.fileHandleIdCallback = fileHandleIdCallback;
		this.view.createUploadForm(isEntity, parentEntityId);
		view.resetToInitialState();
		fileList = null;
		resetUploadProgress();
		view.showUploaderUI();

		globalAppState.setDropZoneHandler(fileList -> {
			handleUploads(fileList);
		});

		// async load upload destinations (and update view)
		queryForUploadDestination();
		return this.view.asWidget();
	}

	public void clearState() {
		view.clear();
		// remove handlers
		this.cancelHandler = null;
		this.successHandler = null;
		this.entity = null;
		this.parentEntityId = null;
		this.currentFileParentEntityId = null;
		this.currentUploadType = null;
		this.currentExternalUploadUrl = null;
		bucketName = null;
		keyPrefixUUID = null;
		endpointUrl = null;
		globalAppState.clearDropZoneHandler();
		resetUploadProgress();
	}

	@Override
	public Widget asWidget() {
		return null;
	}

	public void uploadFiles() {
		view.triggerUpload();
	}

	public String[] getSelectedFileNames() {
		return synapseJsniUtils.getMultipleUploadFileNames(fileList);
	}

	public String getSelectedFilesText() {
		String[] selectedFiles = getSelectedFileNames();
		if (selectedFiles == null)
			return "";
		else if (selectedFiles.length == 1) {
			return selectedFiles[0];
		} else {
			return selectedFiles.length + " files";
		}
	}

	@Override
	public void handleUploads() {
		// field validation
		if (fileList == null) {
			// setup upload process.
			fileList = synapseJsniUtils.getFileList(UploaderViewImpl.FILE_FIELD_ID);
			fileHasBeenUploaded = false;
			currIndex = 0;
			if ((fileNames = getSelectedFileNames()) == null) {
				// no files selected.
				view.hideLoading();
				view.showErrorMessage(DisplayConstants.NO_FILES_SELECTED_FOR_UPLOAD_MESSAGE);
				view.enableUpload();
				return;
			}
		}
		this.handleUploads(fileList);
	}

	public void handleUploads(JavaScriptObject fileList) {
		this.fileList = fileList;
		view.setSelectedFilenames(getSelectedFilesText());
		fileNames = synapseJsniUtils.getMultipleUploadFileNames(fileList);
		if (currentUploadType == UploadType.SFTP) {
			// must also have credentials
			String username = view.getExternalUsername();
			String password = view.getExternalPassword();
			if (!DisplayUtils.isDefined(username) || !DisplayUtils.isDefined(password)) {
				view.hideLoading();
				view.showErrorMessage(DisplayConstants.CREDENTIALS_REQUIRED_MESSAGE);
				view.enableUpload();
				return;
			}
		}
		this.uploadBasedOnConfiguration();
	}

	public void updateUploadBannerView(String banner) {
		if (DisplayUtils.isDefined(banner))
			view.showUploadingBanner(banner);
		else
			view.showUploadingToSynapseStorage();
	}

	public void queryForUploadDestination() {
		enableMultipleFileUploads();
		storageLocationId = null;
		if (parentEntityId == null && entity == null) {
			currentUploadType = UploadType.S3;
			view.showUploadingToSynapseStorage();
		} else {
			// we have a parent entity, check to see where we are suppose to upload the file(s)
			String uploadDestinationsEntityId = parentEntityId != null ? parentEntityId : entity.getId();
			jsClient.getUploadDestinations(uploadDestinationsEntityId, new AsyncCallback<List<UploadDestination>>() {
				public void onSuccess(List<UploadDestination> uploadDestinations) {

					if (uploadDestinations == null || uploadDestinations.isEmpty()) {
						currentUploadType = UploadType.S3;
						view.showUploadingToSynapseStorage();

					} else if (uploadDestinations.get(0) instanceof S3UploadDestination) {
						currentUploadType = UploadType.S3;
						storageLocationId = uploadDestinations.get(0).getStorageLocationId();
						updateUploadBannerView(uploadDestinations.get(0).getBanner());

					} else if (uploadDestinations.get(0) instanceof ExternalGoogleCloudUploadDestination) {
						currentUploadType = UploadType.GOOGLECLOUDSTORAGE;
						storageLocationId = uploadDestinations.get(0).getStorageLocationId();
						updateUploadBannerView(uploadDestinations.get(0).getBanner());

					} else if (uploadDestinations.get(0) instanceof ExternalUploadDestination) {
						ExternalUploadDestination externalUploadDestination = (ExternalUploadDestination) uploadDestinations.get(0);
						storageLocationId = externalUploadDestination.getStorageLocationId();
						currentUploadType = externalUploadDestination.getUploadType();
						if (currentUploadType == UploadType.SFTP) {
							currentExternalUploadUrl = externalUploadDestination.getUrl();
							getSftpHost(currentExternalUploadUrl, externalUploadDestination.getBanner());
							disableMultipleFileUploads();
						} else {
							onFailure(new org.sagebionetworks.web.client.exceptions.IllegalArgumentException("Unsupported external upload type: " + externalUploadDestination.getUploadType()));
						}

					} else if (uploadDestinations.get(0) instanceof ExternalS3UploadDestination) {
						ExternalS3UploadDestination externalUploadDestination = (ExternalS3UploadDestination) uploadDestinations.get(0);
						storageLocationId = externalUploadDestination.getStorageLocationId();
						currentUploadType = externalUploadDestination.getUploadType();
						String banner = externalUploadDestination.getBanner();
						if (!DisplayUtils.isDefined(banner)) {
							banner = "Uploading to S3: " + externalUploadDestination.getBucket();
							if (externalUploadDestination.getBaseKey() != null)
								banner += "/" + externalUploadDestination.getBaseKey();
						}
						updateUploadBannerView(banner);
						// direct to s3(-like) storage
					} else if (uploadDestinations.get(0) instanceof ExternalObjectStoreUploadDestination) {
						ExternalObjectStoreUploadDestination externalUploadDestination = (ExternalObjectStoreUploadDestination) uploadDestinations.get(0);
						storageLocationId = externalUploadDestination.getStorageLocationId();
						currentUploadType = externalUploadDestination.getUploadType();
						String banner = externalUploadDestination.getBanner();
						endpointUrl = externalUploadDestination.getEndpointUrl();
						bucketName = externalUploadDestination.getBucket();
						keyPrefixUUID = externalUploadDestination.getKeyPrefixUUID();
						if (!DisplayUtils.isDefined(banner)) {
							banner = "Uploading to " + endpointUrl + " " + bucketName;
							if (keyPrefixUUID != null)
								banner += "/" + keyPrefixUUID;
						}
						view.showUploadingToS3DirectStorage(endpointUrl, banner);
					} else {
						// unsupported upload destination type
						onFailure(new org.sagebionetworks.web.client.exceptions.IllegalArgumentException("Unsupported upload destination: " + uploadDestinations.get(0).getClass().getName()));
					}
				};

				@Override
				public void onFailure(Throwable caught) {
					uploadError(caught.getMessage(), caught);
				}
			});
		}
	}

	public void getSftpHost(String url, final String banner) {
		synapseClient.getHost(url, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String host) {
				view.showUploadingToExternalStorage(host, banner);
			}

			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		});
	}

	/**
	 * Get the upload destination (based on the project settings), and continue the upload.
	 */
	public void uploadBasedOnConfiguration() {
		// create necessary folders based on webkitRelativePath for the current item, and set parent entity
		// id to correct parent
		// reset the current file parent entity id to the original.
		String relativePath = synapseJsniUtils.getWebkitRelativePath(fileList, currIndex);
		if (relativePath == null || relativePath.isEmpty()) {
			uploadBasedOnConfigurationAfterFolderCreation();
		} else {
			String[] path = relativePath.split("[/]");
			mkdirs(path);
		}
	}

	private void uploadBasedOnConfigurationAfterFolderCreation() {
		if (validateFileName(fileNames[currIndex])) {
			if (currentUploadType == UploadType.S3 || currentUploadType == UploadType.GOOGLECLOUDSTORAGE) {
				uploadToS3OrGoogleCloud();
			} else if (currentUploadType == UploadType.SFTP) {
				uploadToSftpProxy(currentExternalUploadUrl);
			} else {
				String message = "Unsupported external upload type specified: " + currentUploadType;
				uploadError(message, new Exception(message));
			}
		}
	}

	private boolean validateFileName(String filename) {
		boolean valid = filename.matches(VALID_ENTITY_NAME_REGEX) && (filename.trim().length() == filename.length());
		if (!valid) {
			String message = WebConstants.INVALID_ENTITY_NAME_MESSAGE;
			uploadError(message, new Exception(message));
		}
		return valid;
	}

	/**
	 * Create any missing folders based on the relative path
	 * 
	 * @param relativePath
	 */
	public void mkdirs(String[] path) {
		// current file path (remove file name)
		this.currentFilePath = new String[path.length - 1];
		for (int i = 0; i < path.length - 1; i++) {
			currentFilePath[i] = path[i];
		}
		this.currentFilePathElement = 0;
		this.currentFileParentEntityId = parentEntityId;
		mkdir();
	}

	private void mkdir() {
		// look up the name (does that entity name exist?)
		jsClient.lookupChild(currentFilePath[currentFilePathElement], currentFileParentEntityId, new AsyncCallback<String>() {
			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof NotFoundException) {
					// did not find this container, create it!
					createCurrentPathElement();
				} else {
					uploadError("Unable to create target folder structure: " + synapseJsniUtils.getWebkitRelativePath(fileList, currIndex), caught);
				}
			}

			@Override
			public void onSuccess(String parentId) {
				currentFileParentEntityId = parentId;
				mkNextDir();
			}
		});
	}

	private void createCurrentPathElement() {
		Folder f = new Folder();
		f.setName(currentFilePath[currentFilePathElement]);
		f.setParentId(currentFileParentEntityId);
		jsClient.createEntity(f).addCallback(new FutureCallback<Entity>() {
			@Override
			public void onSuccess(Entity entity) {
				// update the current file parent (container) id to this new folder.
				currentFileParentEntityId = entity.getId();
				mkNextDir();
			}

			@Override
			public void onFailure(Throwable caught) {
				uploadError("Unable to create target folder structure: " + synapseJsniUtils.getWebkitRelativePath(fileList, currIndex), caught);
			}
		}, directExecutor());
	}

	private void mkNextDir() {
		currentFilePathElement++;
		if (currentFilePathElement < currentFilePath.length) {
			mkdir();
		} else {
			// done making folders!
			uploadBasedOnConfigurationAfterFolderCreation();
		}
	}

	/**
	 * Given a sftp link, return a link that goes through the sftp proxy to do the action (GET file or
	 * POST upload form)
	 * 
	 * @param realSftpUrl
	 * @param globalAppState
	 * @return
	 */
	public static String getSftpProxyLink(String fileNameOverride, String realSftpUrl, SynapseProperties synapseProperties, GWTWrapper gwt) {
		String sftpProxy = synapseProperties.getSynapseProperty(WebConstants.SFTP_PROXY_ENDPOINT);
		if (sftpProxy != null) {
			String delimiter = sftpProxy.contains("?") ? "&" : "?";

			String escapedRealSftpUrl = gwt.encodeQueryString(realSftpUrl);
			String escapedFileNameOverride = gwt.encodeQueryString(fileNameOverride);
			return sftpProxy + delimiter + "url=" + escapedRealSftpUrl + "&filename=" + escapedFileNameOverride;
		} else {
			// unlikely state
			throw new IllegalArgumentException("Unable to determine SFTP endpoint");
		}
	}

	public void uploadToSftpProxy(final String url) {
		try {
			Callback callback = new Callback() {
				@Override
				public void invoke() {
					view.submitForm(getSftpProxyLink("", url, synapseProperties, gwt));
				}
			};
			checkForExistingFileName(fileNames[currIndex], callback);
		} catch (Exception e) {
			uploadError(e.getMessage(), e);
		}
	}

	public boolean checkFileAPISupported() {
		boolean isFileAPISupported = synapseJsniUtils.isFileAPISupported();
		if (!isFileAPISupported) {
			String message = "Multipart upload is not supported on this browser.";
			uploadError(message, new UnsupportedOperationException(message));
		}
		return isFileAPISupported;
	}

	public void uploadToS3OrGoogleCloud() {
		if (checkFileAPISupported()) {
			// use case B from above
			Callback callback = () -> directUploadStep2(fileNames[currIndex]);
			checkForExistingFileName(fileNames[currIndex], callback);
		}
	}

	/**
	 * Return the current upload type. Used for testing purposes only.
	 * 
	 * @return
	 */
	public UploadType getCurrentUploadType() {
		return currentUploadType;
	};

	/**
	 * Set the current upload type. Used for testing purposes only
	 * 
	 * @param currentUploadType
	 */
	public void setCurrentUploadType(UploadType currentUploadType) {
		this.currentUploadType = currentUploadType;
	}

	/**
	 * Get the current external upload url. Used for testing purposes only.
	 * 
	 * @return
	 */
	public String getCurrentExternalUploadUrl() {
		return currentExternalUploadUrl;
	}

	/**
	 * Set the current external upload url. Used for testing purposes only.
	 * 
	 * @return
	 */
	public void setCurrentExternalUploadUrl(String currentExternalUploadUrl) {
		this.currentExternalUploadUrl = currentExternalUploadUrl;
	}

	public void checkFileSize() throws IllegalArgumentException {
		JavaScriptObject blob = synapseJsniUtils.getFileBlob(this.currIndex, fileList);
		long fileSize = (long) synapseJsniUtils.getFileSize(blob);
		// check
		if (fileSize > OLD_BROWSER_MAX_SIZE) {
			throw new IllegalArgumentException(DisplayConstants.LARGE_FILE_ON_UNSUPPORTED_BROWSER);
		}
	}

	/**
	 * Look for a file with the same name (if we aren't uploading to an existing File already).
	 * 
	 * @param fileName
	 * @param callback Called when upload should continue. Otherwise, it is not called.
	 */
	public void checkForExistingFileName(final String fileName, final Callback callback) {
		if (entity != null || currentFileParentEntityId == null) {
			callback.invoke();
		} else {
			synapseClient.getFileEntityIdWithSameName(fileName, currentFileParentEntityId, new AsyncCallback<String>() {
				@Override
				public void onSuccess(final String result) {
					// there was already a file with this name in the directory.

					// confirm we can overwrite
					view.showConfirmDialog("A file named \"" + fileName + "\" (" + result + ") already exists in this location. Do you want to update the existing file and create a new version?", new Callback() {
						@Override
						public void invoke() {
							// yes, override
							entityId = result;
							callback.invoke();
						}
					}, new Callback() {
						@Override
						public void invoke() {
							processNextFile();
						}
					});
				}

				@Override
				public void onFailure(Throwable caught) {
					if (caught instanceof NotFoundException) {
						// there was not already a file with this name in this directory.
						callback.invoke();
					} else if (caught instanceof ConflictException) {
						// there was an entity found with same parent ID and name, but
						// it was not a File Entity.
						view.showErrorMessage("An item named \"" + fileName + "\" already exists in this location. File could not be uploaded.");
						processNextFile();
					} else {
						uploadError(caught.getMessage(), caught);
					}
				}
			});
		}
	}

	public void directUploadStep2(String fileName) {
		// use S3 direct uploader
		JavaScriptObject currentFile = synapseJsniUtils.getFileBlob(currIndex, fileList);
		String contentType = synapseJsniUtils.getContentType(fileList, currIndex);
		contentType = ContentTypeUtils.fixDefaultContentType(contentType, fileName);
		if (endpointUrl != null) {
			s3DirectUploader.configure(view.getS3DirectAccessKey(), view.getS3DirectSecretKey(), bucketName, endpointUrl);
			s3DirectUploader.uploadFile(fileName, contentType, currentFile, this, keyPrefixUUID, storageLocationId, view);
		} else {
			this.multiPartUploader.uploadFile(fileName, contentType, currentFile, this, storageLocationId, view);
		}
	}

	private void processNextFile() {
		if (currIndex + 1 == fileNames.length) {
			// uploading the last file
			if (!fileHasBeenUploaded) {
				// cancel the upload
				fireCancelEvent();
				clearState();
			} else {
				// finish upload
				view.updateProgress(.99d, "99%", "");
				uploadSuccess();
			}
		} else {
			// more files to upload
			uploadNextFile();
		}
	}

	public void setFileEntityFileHandle(String fileHandleId) {
		if (entityId != null || currentFileParentEntityId != null) {
			synapseClient.setFileEntityFileHandle(fileHandleId, entityId, currentFileParentEntityId, new AsyncCallback<String>() {
				@Override
				public void onSuccess(String entityId) {
					fileHasBeenUploaded = true;
					if (currIndex + 1 == fileNames.length) {
						// to new file handle id, or create new file entity with this file handle id
						view.hideLoading();
						refreshAfterSuccessfulUpload(entityId);
					} else {
						// more files to upload
						uploadNextFile();
					}
				}

				@Override
				public void onFailure(Throwable t) {
					uploadError(t.getMessage(), t);
				}
			});
		}
		if (fileHandleIdCallback != null) {
			fileHandleIdCallback.invoke(fileHandleId);
			uploadSuccess();
		}
	}

	public void uploadNextFile() {
		currIndex++;
		// SWC-4274: reset entity id
		Uploader.this.entityId = null;
		handleUploads();
	}

	@Override
	public void setExternalFilePath(String path, String name, Long storageLocationId) {
		if (path.trim().contains(" ")) {
			// encode for user
			path = gwt.encode(path);
			view.setExternalUrl(path);
		}

		boolean isUpdating = entityId != null || entity != null;
		if (isUpdating) {
			// existing entity
			updateExternalFileEntity(entityId, path, name, null, null, null, storageLocationId);
		} else {
			// new data, use the appropriate synapse call
			createNewExternalFileEntity(path, name, null, null, null, storageLocationId);
		}
	}

	public void setSftpExternalFilePath(final String path, final Long storageLocationId) {
		final JavaScriptObject blob = synapseJsniUtils.getFileBlob(currIndex, fileList);

		synapseJsniUtils.getFileMd5(blob, new MD5Callback() {
			@Override
			public void setMD5(String hexValue) {
				if (hexValue == null) {
					view.showErrorMessage(DisplayConstants.MD5_CALCULATION_ERROR);
					return;
				}
				boolean isUpdating = entityId != null || entity != null;
				long fileSize = (long) synapseJsniUtils.getFileSize(blob);
				String fileName = fileNames[currIndex];
				String contentType = synapseJsniUtils.getContentType(fileList, currIndex);
				contentType = ContentTypeUtils.fixDefaultContentType(contentType, fileName);
				if (isUpdating) {
					// existing entity
					updateExternalFileEntity(entityId, path, fileName, fileSize, contentType, hexValue, storageLocationId);
				} else {
					// new data, use the appropriate synapse call
					createNewExternalFileEntity(path, fileName, fileSize, contentType, hexValue, storageLocationId);
				}
			}
		});
	}

	public AsyncCallback<Entity> getExternalFileUpdatedCallback() {
		return new AsyncCallback<Entity>() {
			@Override
			public void onSuccess(Entity result) {
				entity = result;
				view.showInfo(DisplayConstants.TEXT_LINK_SUCCESS);
				if (successHandler != null) {
					successHandler.onSuccessfulUpload();
				}

				entityUpdated();
			}

			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(DisplayConstants.TEXT_LINK_FAILED + ": " + caught.getMessage());
			}
		};
	}

	public void updateExternalFileEntity(String entityId, String path, String name, Long fileSize, String contentType, String md5, Long storageLocationId) {
		try {
			synapseClient.updateExternalFile(entityId, path, name, contentType, fileSize, md5, storageLocationId, getExternalFileUpdatedCallback());
		} catch (Throwable t) {
			view.showErrorMessage(DisplayConstants.TEXT_LINK_FAILED);
		}
	}

	public void createNewExternalFileEntity(final String path, final String name, Long fileSize, String contentType, String md5, final Long storageLocationId) {
		try {
			synapseClient.createExternalFile(currentFileParentEntityId, path, name, contentType, fileSize, md5, storageLocationId, getExternalFileUpdatedCallback());
		} catch (RestServiceException e) {
			view.showErrorMessage(DisplayConstants.TEXT_LINK_FAILED);
		}
	}

	@Override
	public void disableMultipleFileUploads() {
		view.enableMultipleFileUploads(false);
	}

	public void enableMultipleFileUploads() {
		view.enableMultipleFileUploads(true);
	}

	public void setUploaderLinkNameVisible(boolean visible) {
		view.setUploaderLinkNameVisible(visible);
	}

	public void setSuccessHandler(UploadSuccessHandler successHandler) {
		this.successHandler = successHandler;
	}

	public void setCancelHandler(CancelHandler handler) {
		this.cancelHandler = handler;
	}

	public void entityUpdated() {
		eventBus.fireEvent(new EntityUpdatedEvent());
	}

	/**
	 * This method is called after the form submit is complete. Note that this is used for use case A
	 * and B (see above).
	 */
	@Override
	public void handleSubmitResult(String resultHtml) {
		if (resultHtml == null)
			resultHtml = "";
		// response from server
		// try to parse
		UploadResult uploadResult = null;
		String detailedErrorMessage = null;
		try {
			uploadResult = AddAttachmentHelper.getUploadResult(resultHtml);
			handleSubmitResult(uploadResult);
		} catch (Throwable th) {
			detailedErrorMessage = th.getMessage();
		} ;// wasn't an UplaodResult

		if (uploadResult == null) {
			if (!resultHtml.contains(DisplayConstants.UPLOAD_SUCCESS)) {
				uploadError(detailedErrorMessage, new Exception());
			} else {
				uploadSuccess();
			}
		}
	}

	public void handleSubmitResult(UploadResult uploadResult) {
		if (uploadResult.getUploadStatus() == UploadStatus.SUCCESS) {
			if (currentUploadType == null || currentUploadType.equals(UploadType.S3)) {
				// upload result has file handle id if successful
				String fileHandleId = uploadResult.getMessage();
				setFileEntityFileHandle(fileHandleId);
			} else if (UploadType.SFTP.equals(currentUploadType)) {
				// should respond with the new path
				String path = uploadResult.getMessage();
				setSftpExternalFilePath(path, storageLocationId);
			}
		} else {
			if (isJschAuthorizationError(uploadResult.getMessage())) {
				uploadError(DisplayConstants.INVALID_USERNAME_OR_PASSWORD, new UnauthorizedException(uploadResult.getMessage()));
			} else {
				uploadError("Upload result status indicated upload was unsuccessful. " + uploadResult.getMessage(), new Exception(uploadResult.getMessage()));
			}
		}
	}

	public boolean isJschAuthorizationError(String message) {
		if (message != null) {
			String lowerCaseMessage = message.toLowerCase();
			if (lowerCaseMessage.contains("jschexception") && lowerCaseMessage.contains("auth fail")) {
				return true;
			}
		}
		return false;
	}

	public void showCancelButton(boolean showCancel) {
		view.setShowCancelButton(showCancel);
	}

	@Override
	public void cancelClicked() {
		fireCancelEvent();
	}

	/*
	 * Private Methods
	 */
	private void refreshAfterSuccessfulUpload(String entityId) {
		jsClient.getEntity(entityId, OBJECT_TYPE.FileEntity, new AsyncCallback<Entity>() {
			@Override
			public void onSuccess(Entity result) {
				entity = result;
				uploadSuccess();
			}

			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		});
	}

	public void uploadError(String message, Throwable t) {
		view.showErrorMessage(DisplayConstants.ERROR_UPLOAD_TITLE, message);
		fireCancelEvent();
	}

	private void fireCancelEvent() {
		// Verified that when this method is called, the input field used for direct upload is no longer
		// available,
		// so that this effectively cancels chunked upload too (after the current chunk upload completes)
		multiPartUploader.cancelUpload();
		view.hideLoading();
		view.clear();
		globalAppState.clearDropZoneHandler();
		if (cancelHandler != null) {
			cancelHandler.onCancel();
		}
		view.resetToInitialState();
	}

	private void uploadSuccess() {
		view.showInfo(DisplayConstants.TEXT_UPLOAD_SUCCESS);
		view.clear();
		globalAppState.clearDropZoneHandler();
		view.resetToInitialState();
		resetUploadProgress();
		if (successHandler != null) {
			successHandler.onSuccessfulUpload();
		}
		entityUpdated();
	}

	private void resetUploadProgress() {
		fileNames = null;
		fileHasBeenUploaded = false;
		currIndex = 0;
	}

	/**
	 * For testing purposes
	 * 
	 * @return
	 */
	public void setFileNames(String[] fileNames) {
		this.fileNames = fileNames;
	}

	@Override
	public void updateProgress(double currentProgress, String progressText, String uploadSpeed) {
		view.showProgressBar();
		double percentOfAllFiles = calculatePercentOverAllFiles(this.fileNames.length, this.currIndex, currentProgress);
		String textOfAllFiles = percentFormat.format(percentOfAllFiles * 100.0) + "% ";
		view.updateProgress(percentOfAllFiles, textOfAllFiles, uploadSpeed);
	}

	@Override
	public void uploadSuccess(String fileHandleId) {
		this.setFileEntityFileHandle(fileHandleId);
	}

	@Override
	public void uploadFailed(String string) {
		this.uploadError(string, new Exception(string));
		jsClient.logError(new Exception(string));
	}

	/**
	 * Calculate the upload progress over all file upload given the progress of the current file. This
	 * method assumes each file contributes equally to the total upload times.
	 * 
	 * @param numberFiles Number of files to upload.
	 * @param currentFileIndex Index of the current file with zero being the first file.
	 * @param percentOfCurrentFile The percent complete for the current file. This number should be
	 *        between 0.0 and 1.0 (%/100).
	 * @return
	 */
	public static double calculatePercentOverAllFiles(int numberFiles, int currentFileIndex, double percentOfCurrentFile) {
		double percentPerFile = 1.0 / (double) numberFiles;
		double percentOfAllFiles = percentPerFile * percentOfCurrentFile + (percentPerFile * currentFileIndex);
		return percentOfAllFiles;
	}

	@Override
	public Long getStorageLocationId() {
		return this.storageLocationId;
	}
}
