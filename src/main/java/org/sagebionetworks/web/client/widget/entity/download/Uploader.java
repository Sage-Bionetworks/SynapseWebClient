package org.sagebionetworks.web.client.widget.entity.download;

import java.util.List;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.attachment.UploadResult;
import org.sagebionetworks.repo.model.attachment.UploadStatus;
import org.sagebionetworks.repo.model.file.ExternalUploadDestination;
import org.sagebionetworks.repo.model.file.S3UploadDestination;
import org.sagebionetworks.repo.model.file.UploadDestination;
import org.sagebionetworks.repo.model.file.UploadType;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.events.CancelEvent;
import org.sagebionetworks.web.client.events.CancelHandler;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapsePersistable;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.dialog.AddAttachmentDialog;
import org.sagebionetworks.web.client.widget.upload.ProgressingFileUploadHandler;
import org.sagebionetworks.web.client.widget.upload.MultipartUploader;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.exceptions.ConflictException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * This Uploader class supports 3 use cases:
 * A. Legacy data types (like the Data Entity): Uploads to the FileUpload servlet, using the Form submit (POST).  @see org.sagebionetworks.web.server.servlet.FileUpload
 * B. File Entity, newer client browser: Direct multipart upload to S3, using a PUT to presigned URLs.
 * 
 * Case B will be the most common case.
 */
public class Uploader implements UploaderView.Presenter, SynapseWidgetPresenter, SynapsePersistable, ProgressingFileUploadHandler {
	
	public static final long OLD_BROWSER_MAX_SIZE = (long)ClientProperties.MB * 5; //5MB	
	private UploaderView view;
	private NodeModelCreator nodeModelCreator;
	private HandlerManager handlerManager;
	private Entity entity;
	private String parentEntityId;
	//set if we are uploading to an existing file entity
	private String entityId;
	private CallbackP<String> fileHandleIdCallback;
	private SynapseClientAsync synapseClient;
	private SynapseJSNIUtils synapseJsniUtils;
	private GWTWrapper gwt;
	MultipartUploader multiPartUploader;
	AuthenticationController authenticationController;
	
	private String[] fileNames;
	private int currIndex;
	private NumberFormat percentFormat;
	private boolean fileHasBeenUploaded = false;
	private UploadType currentUploadType;
	
	@Inject
	public Uploader(
			UploaderView view, 			
			NodeModelCreator nodeModelCreator, 
			SynapseClientAsync synapseClient,
			SynapseJSNIUtils synapseJsniUtils,
			GWTWrapper gwt,
			AuthenticationController authenticationController,
			MultipartUploader multiPartUploader
			) {
	
		this.view = view;		
		this.nodeModelCreator = nodeModelCreator;
		this.synapseClient = synapseClient;
		this.synapseJsniUtils = synapseJsniUtils;
		this.gwt = gwt;
		this.authenticationController = authenticationController;
		this.multiPartUploader = multiPartUploader;
		view.setPresenter(this);
		clearHandlers();
	}		
		
	public Widget asWidget(Entity entity) {
		return asWidget(entity, null, null, true);
	}
	
	public Widget asWidget(String parentEntityId) {
		return asWidget((Entity)null, parentEntityId, null, true);
	}
	
	public Widget asWidget(Entity entity, String parentEntityId, CallbackP<String> fileHandleIdCallback, boolean isEntity) {
		this.view.setPresenter(this);
		this.entity = entity;
		this.parentEntityId = parentEntityId;
		this.fileHandleIdCallback = fileHandleIdCallback;
		this.view.createUploadForm(isEntity, parentEntityId);
		view.resetToInitialState();
		resetUploadProgress();
		view.showUploaderUI();
		return this.view.asWidget();
	}
	
	public void clearState() {
		view.clear();
		// remove handlers
		handlerManager = new HandlerManager(this);		
		this.entity = null;
		this.parentEntityId = null;
		this.currentUploadType = null;
		resetUploadProgress();
	}

	@Override
	public Widget asWidget() {
		return null;
	}

	public void uploadFiles() {
		view.triggerUpload();
	}
	
	@Override
	public void handleUploads() {
		currentUploadType = null;
		if (fileNames == null) {
			//setup upload process.
			
			fileHasBeenUploaded = false;
			currIndex = 0;
			if ((fileNames = synapseJsniUtils.getMultipleUploadFileNames(UploaderViewImpl.FILE_FIELD_ID)) == null) {
				//no files selected.
				view.showNoFilesSelectedForUpload();
				return;
			}
		}
		
		entityId = null;
		if (entity != null) {
			entityId = entity.getId();
		}
		
		uploadBasedOnConfiguration();
	}
	
	/**
	 * Get the upload destination (based on the project settings), and continue the upload.
	 */
	public void uploadBasedOnConfiguration() {
//		//TESTING ONLY
//		uploadToSftpProxy("sftp://localhost/dir/subdir/1234");
		
		if (parentEntityId == null) {
			uploadToS3();
		} else {
			//we have a parent entity, check to see where we are suppose to upload the file(s)
			synapseClient.getUploadDestinations(parentEntityId, new AsyncCallback<List<UploadDestination>>() {
				public void onSuccess(List<UploadDestination> uploadDestinations) {
					if (uploadDestinations == null || uploadDestinations.isEmpty() || uploadDestinations.get(0) instanceof S3UploadDestination) {
						uploadToS3();
					} else if (uploadDestinations.get(0) instanceof ExternalUploadDestination){
						ExternalUploadDestination d = (ExternalUploadDestination) uploadDestinations.get(0);
						uploadToExternal(d);
					} else {
						//unsupported upload destination type
						onFailure(new org.sagebionetworks.web.client.exceptions.IllegalArgumentException("Unsupported upload destination: " + uploadDestinations.get(0).getClass().getName()));
					}
				};
				
				@Override
				public void onFailure(Throwable caught) {
					uploadError(caught.getMessage());
				}
			});
		}
	}
	
	public void uploadToExternal(ExternalUploadDestination d) {
		if (UploadType.SFTP.equals(d.getUploadType())){
			//upload to the sftp proxy!
			uploadToSftpProxy(d.getUrl());
		} else {
			//not yet supported
			uploadError("External upload destination type not yet handled: " + d.getUploadType().name());
		}
	}
	
	public void uploadToSftpProxy(final String url) {
		currentUploadType = UploadType.SFTP;
		AsyncCallback<String> endpointCallback = new AsyncCallback<String>() {
			@Override
			public void onSuccess(String sftpProxy) {
				//support gwt codeserver
				String delimiter = sftpProxy.contains("?") ? "&" : "?";
				view.submitForm(sftpProxy + delimiter + "url="+url);
			}
			@Override
			public void onFailure(Throwable caught) {
				uploadError("Unable to determine SFTP endpoint: " + caught.getMessage());
			}
		};
		synapseClient.getSynapseProperty(WebConstants.SFTP_PROXY_ENDPOINT, endpointCallback);
	}
	
	public void uploadToS3() {
		currentUploadType = UploadType.S3;
		boolean isFileEntity = entity == null || entity instanceof FileEntity;				 
		if (isFileEntity) {
			//use case B from above
			directUploadStep1(fileNames[currIndex]);
		} else {
			//use case A from above
			//uses the default action url
			//if using this method, block if file size is > MAX_SIZE
			try {
				checkFileSize();
			} catch (Exception e) {
				view.showErrorMessage(e.getMessage());
				fireCancelEvent();
				return;
			}				
			view.submitForm(getOldUploadUrl());
		}
	}
	
	public void checkFileSize() throws IllegalArgumentException{
		long fileSize = (long)synapseJsniUtils.getFileSize(UploaderViewImpl.FILE_FIELD_ID, currIndex);
		//check
		if (fileSize > OLD_BROWSER_MAX_SIZE) {
			throw new IllegalArgumentException(DisplayConstants.LARGE_FILE_ON_UNSUPPORTED_BROWSER);
		}
	}

	/**
	 * Look for a file with the same name (if we aren't uploading to an existing File already).
	 * @param fileName
	 */
	public void directUploadStep1(final String fileName) {
		if (entity != null || parentEntityId == null) {
			directUploadStep2(fileName);
		} else {
			synapseClient.getFileEntityIdWithSameName(fileName, parentEntityId, new AsyncCallback<String>() {
				@Override
				public void onSuccess(final String result) {
					//there was already a file with this name in the directory.
					
					//confirm we can overwrite
					view.showConfirmDialog("A file named \""+fileName+"\" ("+result+") already exists in this location. Do you want to replace it with the one you're uploading?", 
							new Callback() {
								@Override
								public void invoke() {
									//yes, override
									entityId = result;
									directUploadStep2(fileName);
								}
							},
							new Callback() {
								@Override
								public void invoke() {
									handleCancelledFileUpload();
								}
							});
				}
				@Override
				public void onFailure(Throwable caught) {
					if (caught instanceof NotFoundException) {
						//there was not already a file with this name in this directory.
						directUploadStep2(fileName);
					} else if (caught instanceof ConflictException) {
						//there was an entity found with same parent ID and name, but
						//it was not a File Entity.
						view.showErrorMessage("An item named \""+fileName+"\" already exists in this location. File could not be uploaded.");
						handleCancelledFileUpload();
					} else {
						uploadError(caught.getMessage());
					}
				}
			});
		}
	}
	
	private void directUploadStep2(String fileName) {
		this.multiPartUploader.uploadFile(fileName, UploaderViewImpl.FILE_FIELD_ID, this.currIndex, this);
	}

	private void handleCancelledFileUpload() {
		if (currIndex + 1 == fileNames.length) {
			//uploading the last file
			if (!fileHasBeenUploaded) {
				//cancel the upload
				fireCancelEvent();
				clearState();
			} else {
				//finish upload
				view.updateProgress(.99d, "99%");
				uploadSuccess();
			}
		} else {
			//more files to upload
			currIndex++;
			handleUploads();
		}
	}

	
	public void setFileEntityFileHandle(String fileHandleId) {
		if (entityId != null || parentEntityId != null) {
			try {
				synapseClient.setFileEntityFileHandle(fileHandleId, entityId, parentEntityId, new AsyncCallback<String>() {
					@Override
					public void onSuccess(String entityId) {
						fileHasBeenUploaded = true;
						if (currIndex + 1 == fileNames.length) {
							//to new file handle id, or create new file entity with this file handle id
							view.hideLoading();
							refreshAfterSuccessfulUpload(entityId);
						} else {
							//more files to upload
							currIndex++;
							handleUploads();
						}
					}
					@Override
					public void onFailure(Throwable t) {
						uploadError(t.getMessage());		
					}
				});
			} catch (RestServiceException e) {
				uploadError(e.getMessage());
			}
		}
		if (fileHandleIdCallback != null) {
			fileHandleIdCallback.invoke(fileHandleId);
			uploadSuccess();
		}
	}
	
	@Override
	public void setExternalFilePath(String path, String name) {
		if (entity==null || entity instanceof FileEntity) {
			//new data, use the appropriate synapse call
			//if we haven't created the entity yet, do that first
			if (entity == null) {
				createNewExternalFileEntity(path, name);
			}
			else {
				updateExternalFileEntity(entity.getId(), path, name);
			}
		}
		else {
			//old data
			String entityId = entity.getId();
			synapseClient.updateExternalLocationable(entityId, path, name, new AsyncCallback<EntityWrapper>() {
				
				public void onSuccess(EntityWrapper result) {
					externalLinkUpdated(result, entity.getClass());
				};
				@Override
				public void onFailure(Throwable caught) {
					view.showErrorMessage(DisplayConstants.TEXT_LINK_FAILED);
				}
			} );
		}
	}
	
	public void externalLinkUpdated(EntityWrapper result, Class<? extends Entity> entityClass) {
		try {
			entity = nodeModelCreator.createJSONEntity(result.getEntityJson(), entityClass);
			view.showInfo(DisplayConstants.TEXT_LINK_FILE, DisplayConstants.TEXT_LINK_SUCCESS);
			entityUpdated();						
		} catch (JSONObjectAdapterException e) {
			view.showErrorMessage(DisplayConstants.TEXT_LINK_FAILED);
		}
	}
	
	public void updateExternalFileEntity(String entityId, String path, String name) {
		try {
			synapseClient.updateExternalFile(entityId, path, name, new AsyncCallback<EntityWrapper>() {
				@Override
				public void onSuccess(EntityWrapper result) {
					externalLinkUpdated(result, FileEntity.class);
				}
				@Override
				public void onFailure(Throwable caught) {
					view.showErrorMessage(DisplayConstants.TEXT_LINK_FAILED);
				}
			});
		} catch (Throwable t) {
			view.showErrorMessage(DisplayConstants.TEXT_LINK_FAILED);
		}
	}
	public void createNewExternalFileEntity(final String path, final String name) {
		try {
			synapseClient.createExternalFile(parentEntityId, path, name, new AsyncCallback<EntityWrapper>() {
				@Override
				public void onSuccess(EntityWrapper result) {
					externalLinkUpdated(result, FileEntity.class);
				}
				
				@Override
				public void onFailure(Throwable caught) {
					view.showErrorMessage(DisplayConstants.TEXT_LINK_FAILED);
				}			
			});
		} catch (RestServiceException e) {			
			view.showErrorMessage(DisplayConstants.TEXT_LINK_FAILED);	
		}
	}
	
	@Override
	public void disableMultipleFileUploads() {
		view.disableMultipleFileUploads();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void addCancelHandler(CancelHandler handler) {
		handlerManager.addHandler(CancelEvent.getType(), handler);
	}
	
	@Override
	public void clearHandlers() {
		handlerManager = new HandlerManager(this);
	}

	@Override
	public void addPersistSuccessHandler(EntityUpdatedHandler handler) {
		handlerManager.addHandler(EntityUpdatedEvent.getType(), handler);
	}

	public void entityUpdated() {
		handlerManager.fireEvent(new EntityUpdatedEvent());
	}
	
	/**
	 * This method is called after the form submit is complete. Note that this is used for use case A and B (see above).
	 */
	@Override
	public void handleSubmitResult(String resultHtml) {
		if(resultHtml == null) resultHtml = "";
		// response from server
		//try to parse
		UploadResult uploadResult = null;
		String detailedErrorMessage = null;
		try{
			uploadResult = AddAttachmentDialog.getUploadResult(resultHtml);
			if (uploadResult.getUploadStatus() == UploadStatus.SUCCESS) {
				if (currentUploadType == null || currentUploadType.equals(UploadType.S3)) {
					//upload result has file handle id if successful
					String fileHandleId = uploadResult.getMessage();
					setFileEntityFileHandle(fileHandleId);
				} else if (UploadType.SFTP.equals(currentUploadType)) {
					//should respond with the new path
					String path = uploadResult.getMessage();
					String fileName = fileNames[currIndex];
					setExternalFilePath(path, fileName);
				}
			}else {
				uploadError("Upload result status indicated upload was unsuccessful.");
			}
		} catch (Throwable th) {detailedErrorMessage = th.getMessage();};//wasn't an UplaodResult
		
		if (uploadResult == null) {
			if(!resultHtml.contains(DisplayConstants.UPLOAD_SUCCESS)) {
				uploadError(detailedErrorMessage);
			} else {
				uploadSuccess();
			}
		}
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
		synapseClient.getEntity(entityId, new AsyncCallback<EntityWrapper>() {
			@Override
			public void onSuccess(EntityWrapper result) {
				try {
					entity = nodeModelCreator.createEntity(result);
					uploadSuccess();
				} catch (JSONObjectAdapterException e) {
					view.showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		});
	}
	private void uploadError(String message) {
		String details = "";
		if (message != null && message.length() > 0)
			details = "  \n" + message;
		view.showErrorMessage(DisplayConstants.ERROR_UPLOAD + details);
		fireCancelEvent();
	}
	
	private void fireCancelEvent(){
		//Verified that when this method is called, the input field used for direct upload is no longer available, 
		//so that this effectively cancels chunked upload too (after the current chunk upload completes)
		view.hideLoading();
		view.clear();
		handlerManager.fireEvent(new CancelEvent());
		view.resetToInitialState();
	}
	
	private void uploadSuccess() {
		view.showInfo(DisplayConstants.TEXT_UPLOAD_FILE_OR_LINK, DisplayConstants.TEXT_UPLOAD_SUCCESS);
		view.clear();
		view.resetToInitialState();
		resetUploadProgress();
		handlerManager.fireEvent(new EntityUpdatedEvent());
	}

	private String getOldUploadUrl() {
		 String entityIdString = entity != null ? WebConstants.ENTITY_PARAM_KEY + "=" + entity.getId() : "";
		return gwt.getModuleBaseURL() + WebConstants.LEGACY_DATA_UPLOAD_SERVLET + "?" + entityIdString;
	}
	
	private void resetUploadProgress() {
		fileNames = null;
		fileHasBeenUploaded = false;
		currIndex = 0;
	}

	/**
	 * For testing purposes
	 * @return
	 */
	public String getDirectUploadFileEntityId() {
		return entityId;
	}
	
	/**
	 * For testing purposes
	 * @return
	 */
	public void setFileNames(String[] fileNames) {
		this.fileNames = fileNames;
	}
	
	@Override
	public void updateProgress(double currentProgress, String progressText) {
		view.showProgressBar();
		double percentOfAllFiles = calculatePercentOverAllFiles(this.fileNames.length, this.currIndex, currentProgress);
		String textOfAllFiles = percentFormat.format(percentOfAllFiles*100.0) + "%";
		view.updateProgress(percentOfAllFiles, textOfAllFiles);
	}

	@Override
	public void uploadSuccess(String fileHandleId) {
		this.setFileEntityFileHandle(fileHandleId);
	}

	@Override
	public void uploadFailed(String string) {
		this.uploadError(string);
	}
	
	/**
	 * Calculate the upload progress over all file upload given the progress of the current file.
	 * This method assumes each file contributes equally to the total upload times.
	 * @param numberFiles Number of files to upload.
	 * @param currentFileIndex Index of the current file with zero being the first file.
	 * @param percentOfCurrentFile The percent complete for the current file.  This number should be between 0.0 and 1.0 (%/100).
	 * @return
	 */
	public static double calculatePercentOverAllFiles(int numberFiles, int currentFileIndex, double percentOfCurrentFile){
		double percentPerFile = 1.0/(double)numberFiles;
		double percentOfAllFiles = percentPerFile*percentOfCurrentFile + (percentPerFile*currentFileIndex);
		return percentOfAllFiles;
	}
}
