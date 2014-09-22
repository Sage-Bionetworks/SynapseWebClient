package org.sagebionetworks.web.client.widget.entity.download;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.attachment.UploadResult;
import org.sagebionetworks.repo.model.attachment.UploadStatus;
import org.sagebionetworks.repo.model.file.ChunkRequest;
import org.sagebionetworks.repo.model.file.ChunkedFileToken;
import org.sagebionetworks.repo.model.file.State;
import org.sagebionetworks.repo.model.file.UploadDaemonStatus;
import org.sagebionetworks.repo.model.util.ContentTypeUtils;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.ClientLogger;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.ProgressCallback;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.callback.MD5Callback;
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
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.exceptions.ConflictException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xhr.client.ReadyStateChangeHandler;
import com.google.gwt.xhr.client.XMLHttpRequest;
import com.google.inject.Inject;

/**
 * This Uploader class supports 3 use cases:
 * A. Legacy data types (like the Data Entity): Uploads to the FileUpload servlet, using the Form submit (POST).  @see org.sagebionetworks.web.server.servlet.FileUpload
 * B. File Entity, older client browser: Directs users to JavaWebStart the SynapseUploader (for direct S3 upload from Java)
 * C. File Entity, newer client browser: Direct multipart upload to S3, using a PUT to presigned URLs.
 * 
 * Case C will be the most common case.
 */
public class Uploader implements UploaderView.Presenter, SynapseWidgetPresenter, SynapsePersistable {
	
	//we are dedicating 90% of the progress bar to uploading the chunks, reserving 10% for the final combining (last) step
	public static final double UPLOADING_TOTAL_PERCENT = .9d;
	public static final double COMBINING_TOTAL_PERCENT = .1d;
	public static final long OLD_BROWSER_MAX_SIZE = (long)ClientProperties.MB * 5; //5MB
	public static final long BYTES_PER_CHUNK = (long)ClientProperties.MB * 5; //5MB
	public static final int MAX_RETRY = 5;
	public static final int RETRY_DELAY = 1000;
	
	private UploaderView view;
	private NodeModelCreator nodeModelCreator;
	private HandlerManager handlerManager;
	private Entity entity;
	private String parentEntityId;
	//set if we are uploading to an existing file entity
	private String entityId;
	private JSONObjectAdapter jsonObjectAdapter;
	private CallbackP<String> fileHandleIdCallback;
	private SynapseClientAsync synapseClient;
	private SynapseJSNIUtils synapseJsniUtils;
	private GWTWrapper gwt;
	AuthenticationController authenticationController;
	private ClientLogger logger;
	
	private ChunkedFileToken token;
	NumberFormat percentFormat;
	private boolean isDirectUploadSupported;
	//string builder to capture upload information.  sends to output if any errors occur during direct upload.
	private StringBuilder uploadLog;
	
	private String[] fileNames;
	private int currIndex;
	private boolean fileHasBeenUploaded = false;
	
	@Inject
	public Uploader(
			UploaderView view, 			
			NodeModelCreator nodeModelCreator, 
			SynapseClientAsync synapseClient,
			JSONObjectAdapter jsonObjectAdapter,
			SynapseJSNIUtils synapseJsniUtils,
			GWTWrapper gwt,
			AuthenticationController authenticationController,
			ClientLogger logger
			) {
	
		this.view = view;		
		this.nodeModelCreator = nodeModelCreator;
		this.synapseClient = synapseClient;
		this.jsonObjectAdapter=jsonObjectAdapter;
		this.synapseJsniUtils = synapseJsniUtils;
		this.gwt = gwt;
		this.authenticationController = authenticationController;
		this.logger = logger;
		view.setPresenter(this);
		percentFormat = gwt.getNumberFormat("##");
		clearHandlers();
		uploadLog = new StringBuilder();
		isDirectUploadSupported = synapseJsniUtils.isDirectUploadSupported();
		if (!isDirectUploadSupported)
			disableMultipleFileUploads();
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
		this.view.createUploadForm(isEntity, parentEntityId, isDirectUploadSupported);
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
		resetUploadProgress();
	}

	@Override
	public Widget asWidget() {
		return null;
	}

	@Override
	public String getDefaultUploadActionUrl() {
		boolean isFileEntity = entity == null || entity instanceof FileEntity;
		String uploadUrl = isFileEntity ? 
				//new way				
				getBaseFileHandleUrl(): 
				//old way
				getOldUploadUrl();
		return uploadUrl;
	}
	
	public void uploadFiles() {
		view.triggerUpload();
	}
	
	@Override
	public void handleUploads() {
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
		
		boolean isFileEntity = entity == null || entity instanceof FileEntity;				 
		if (isFileEntity && isDirectUploadSupported) {
			//use case C from above
			directUploadStep1(fileNames[currIndex]);
		} else {
			//use case A and B from above
			//uses the default action url
			//if using this method, block if file size is > MAX_SIZE
			try {
				checkFileSize();
			} catch (Exception e) {
				view.showErrorMessage(e.getMessage());
				fireCancelEvent();
				return;
			}				
			view.submitForm();
		}
	}
	
	public void checkFileSize() throws IllegalArgumentException{
		long fileSize = (long)synapseJsniUtils.getFileSize(UploaderViewImpl.FILE_FIELD_ID, currIndex);
		//check
		if (fileSize > OLD_BROWSER_MAX_SIZE) {
			throw new IllegalArgumentException(DisplayConstants.LARGE_FILE_ON_UNSUPPORTED_BROWSER);
		}
	}
	
	public String fixDefaultContentType(String type, String fileName) {
		String contentType = type;
		String lowercaseFilename = fileName.toLowerCase();
		if (type == null || type.trim().length() == 0) {
			if (ContentTypeUtils.isRecognizedCodeFileName(fileName)) {
				contentType = ContentTypeUtils.PLAIN_TEXT;
			}
			else if (lowercaseFilename.endsWith(".tsv") || lowercaseFilename.endsWith(".tab")) {
				contentType = WebConstants.TEXT_TAB_SEPARATED_VALUES;
			}
		}
		return contentType;
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
	
	/**
	 * Get the md5
	 * @param fileName
	 */
	public void directUploadStep2(final String fileName) {
		this.token = null;
		uploadLog = new StringBuilder();
		uploadLog.append(gwt.getUserAgent() + "\n" + gwt.getAppVersion() + "\nDirectly uploading " + fileName + " - calculating MD5\n");
		
		//get the chunked file request (includes token)
		//get the content type
		final String contentType = fixDefaultContentType(synapseJsniUtils.getContentType(UploaderViewImpl.FILE_FIELD_ID, currIndex), fileName);
		synapseJsniUtils.getFileMd5(UploaderViewImpl.FILE_FIELD_ID, currIndex, new MD5Callback() {
			
			@Override
			public void setMD5(String hexValue) {
				uploadLog.append("MD5=" + hexValue+"\ncontentType="+contentType+"\n");
				directUploadStep3(fileName, contentType, hexValue);
			}
		});
	}
	public void directUploadStep3(final String fileName, final String contentType, String md5){
		try {
			synapseClient.getChunkedFileToken(fileName, contentType, md5, new AsyncCallback<String>() {
				@Override
				public void onSuccess(String result) {
					try {
						token = nodeModelCreator.createJSONEntity(result, ChunkedFileToken.class);
						long fileSize = (long)synapseJsniUtils.getFileSize(UploaderViewImpl.FILE_FIELD_ID, currIndex);
						long totalChunkCount = getChunkCount(fileSize);
						if (!fileHasBeenUploaded)
							view.showProgressBar();
						uploadLog.append("fileSize="+fileSize + " totalChunkCount=" + totalChunkCount+"\n");
						directUploadStep4(contentType, 1, 1, totalChunkCount, fileSize, new ArrayList<String>());
					} catch (JSONObjectAdapterException e) {
						onFailure(e);
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
	
	public long getChunkCount(long fileSize) {
		return (long)Math.ceil((double)fileSize / (double)BYTES_PER_CHUNK);
	}
	/**
	 * 
	 * @param currentChunkNumber The chunk number that should be uploaded
	 * @param currentAttempt This is our nth attempt at uploading this chunk (starting at 1, trying up to MAX_RETRY times)
	 * @param totalChunkCount The total number of chunks to complete upload of the file
	 */
	public void directUploadStep4(final String contentType, final int currentChunkNumber, final int currentAttempt, final long totalChunkCount, final long fileSize, final List<String> requestList){
		//get the presigned upload url
		//and upload the file
		try {
			//create a request for each chunk, and try to upload each one
			uploadLog.append("directUploadStep4: currentChunkNumber="+currentChunkNumber + " currentAttempt=" + currentAttempt+"\n");
			ChunkRequest request = new ChunkRequest();
			request.setChunkedFileToken(token);
			request.setChunkNumber((long) currentChunkNumber);
			JSONObjectAdapter json = jsonObjectAdapter.createNew();
			request.writeToJSONObject(json);
			final String requestJson = json.toJSONString();
			synapseClient.getChunkedPresignedUrl(requestJson, new AsyncCallback<String>() {
				@Override
				public void onSuccess(String urlString) {
					XMLHttpRequest xhr = gwt.createXMLHttpRequest();
					if (xhr != null) {
						xhr.setOnReadyStateChange(new ReadyStateChangeHandler() {
							@Override
							public void onReadyStateChange(XMLHttpRequest xhr) {
								uploadLog.append("XMLHttpRequest.setOnReadyStateChange:  readyState="+xhr.getReadyState() + " status=" + xhr.getStatus()+"\n");
								if (xhr.getReadyState() == 4) { //XMLHttpRequest.DONE=4, posts suggest this value is not resolved in some browsers
									if (xhr.getStatus() == 200) { //OK
										uploadLog.append("XMLHttpRequest.setOnReadyStateChange: OK\n");
										chunkUploadSuccess(requestJson, contentType, currentChunkNumber, totalChunkCount, fileSize, requestList);
									} else {
										uploadLog.append("XMLHttpRequest.setOnReadyStateChange: Failure\n");
										chunkUploadFailure(contentType, currentChunkNumber, currentAttempt, totalChunkCount, fileSize, requestList, xhr.getStatusText());
									}
								}
							}
						});
					}
					ByteRange range = getByteRange(currentChunkNumber, fileSize);
					uploadLog.append("directUploadStep2: uploading file chunk.  ByteRange="+range.getStart()+"-"+range.getEnd()+" \n");
					synapseJsniUtils.uploadFileChunk(contentType, currIndex, UploaderViewImpl.FILE_FIELD_ID, range.getStart(), range.getEnd(), urlString, xhr, new ProgressCallback() {
						@Override
						public void updateProgress(double value) {
							//Note:  0 <= value <= 1
							//And we need to add this to the chunks that have already been uploaded.  And divide by the total chunk count
							double currentProgress = ((((double)(currentChunkNumber-1)) + value)/((double)totalChunkCount) * UPLOADING_TOTAL_PERCENT + currIndex)/ (double) fileNames.length;
							String progressText = percentFormat.format(currentProgress*100.0) + "%";
							view.updateProgress(currentProgress, progressText);
						}
					});
				}
				@Override
				public void onFailure(Throwable t) {
					chunkUploadFailure(contentType, currentChunkNumber, currentAttempt, totalChunkCount, fileSize, requestList, t.getMessage());
				}
			});
		} catch (RestServiceException e) {
			chunkUploadFailure(contentType, currentChunkNumber, currentAttempt, totalChunkCount, fileSize, requestList, e.getMessage());
		} catch (JSONObjectAdapterException e) {
			view.showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);
			fireCancelEvent();
		}
	}
	
	/**
	 * Called when a chunk is successfully uploaded
	 * @param requestJson
	 * @param contentType
	 * @param currentChunkNumber
	 * @param totalChunkCount
	 * @param fileSize
	 * @param requestList
	 */
	public void chunkUploadSuccess(String requestJson, String contentType, int currentChunkNumber, long totalChunkCount, long fileSize, List<String> requestList){
		//are there more chunks to upload?
		requestList.add(requestJson);
		if (currentChunkNumber >= totalChunkCount)
			directUploadStep5(requestList, 1);
		else
			directUploadStep4(contentType, currentChunkNumber+1, 1, totalChunkCount, fileSize, requestList);
	}
	
	/**
	 * Called when chunk upload fails
	 * @param contentType
	 * @param currentChunkNumber
	 * @param currentAttempt
	 * @param totalChunkCount
	 * @param fileSize
	 * @param requestList
	 */
	public void chunkUploadFailure(final String contentType, final int currentChunkNumber, final int currentAttempt, final long totalChunkCount, final long fileSize, final List<String> requestList, String detailedMessage) {
		if (currentAttempt >= MAX_RETRY)
			uploadError("Exceeded the maximum number of attempts to upload a single file chunk. " + detailedMessage);
		else { //retry
			//sleep for a second on the client, then try again.
			gwt.scheduleExecution(new Callback() {
				@Override
				public void invoke() {
					directUploadStep4(contentType, currentChunkNumber, currentAttempt+1, totalChunkCount, fileSize, requestList);
				}
			}, RETRY_DELAY);
		}
	}
	
	public class ByteRange {
		private long start, end;
		public ByteRange(long start, long end) {
			this.start = start;
			this.end = end;
		}
		public long getEnd() {
			return end;
		}
		public long getStart() {
			return start;
		}
	}
	
	public ByteRange getByteRange(int currentChunkNumber, Long fileSize) {
		long startByte = (currentChunkNumber-1) * BYTES_PER_CHUNK;
		long endByte = currentChunkNumber * BYTES_PER_CHUNK - 1;
		if (endByte >= fileSize)
			endByte = fileSize-1;
		return new ByteRange(startByte, endByte);
	}
	
	public void directUploadStep5(final List<String> requestList, final int currentAttempt){
		uploadLog.append("directUploadStep5: currentAttempt=" + currentAttempt+"\n");
		//complete the file upload, and refresh
		try {
			//start the daemon to complete the file upload, and continue to check back until it's complete
			synapseClient.combineChunkedFileUpload(requestList, new AsyncCallback<String>() {
				@Override
				public void onSuccess(String result) {
					try {
						UploadDaemonStatus status = nodeModelCreator.createJSONEntity(result, UploadDaemonStatus.class);
						//if it's already done, then finish.  Otherwise keep checking back until it's complete.
						processDaemonStatus(status,  requestList, currentAttempt);
					} catch (JSONObjectAdapterException e) {
						onFailure(e);
					}
				}
				@Override
				public void onFailure(Throwable caught) {
					combineChunksUploadFailure(requestList, currentAttempt, caught.getMessage());
				}
			});
		} catch (RestServiceException e) {
			uploadError(e.getMessage());
		}
	}
	
	public void processDaemonStatus(UploadDaemonStatus status, List<String> requestList, int currentAttempt){
		State state = status.getState();
		if (State.COMPLETED == state) {
			if (currIndex + 1 == fileNames.length)
				view.updateProgress(.99d, "99%");
			setFileEntityFileHandle(status.getFileHandleId());
		}
		else if (State.PROCESSING == state){
			//still processing.  update the progress bar and check again later
			double currentProgress = ((((status.getPercentComplete()*.01d) * COMBINING_TOTAL_PERCENT) + UPLOADING_TOTAL_PERCENT) + currIndex) / fileNames.length;
			String progressText = percentFormat.format(currentProgress*100.0) + "%";
			view.updateProgress(currentProgress, progressText);
			checkStatusAgainLater(status.getDaemonId(), entityId, parentEntityId, requestList, currentAttempt);
		}
		else if (State.FAILED == state) {
			combineChunksUploadFailure(requestList, currentAttempt, status.getErrorMessage());
		}
	}
	
	public void combineChunksUploadFailure(List<String> requestList, int currentAttempt, String errorMessage) {
		if (currentAttempt >= MAX_RETRY)
			uploadError("Exceeded the maximum number of attempts to combine all of the parts. " + errorMessage);
		else //retry
			directUploadStep5(requestList, currentAttempt+1);
	}
	
	public void checkStatusAgainLater(final String daemonId, final String entityId, final String parentEntityId, final List<String> requestList, final int currentAttempt) {
		//in one second, do a web service call to check the status again
		Timer t = new Timer() {
		      public void run() {
		    	  try {
					synapseClient.getUploadDaemonStatus(daemonId, new AsyncCallback<String>() {
						@Override
						public void onSuccess(String result) {
							try {
								UploadDaemonStatus status = nodeModelCreator.createJSONEntity(result, UploadDaemonStatus.class);
								// if it's already done, then finish. Otherwise keep checking back until it's complete.
								processDaemonStatus(status, requestList, currentAttempt);
							} catch (JSONObjectAdapterException e) {
								onFailure(e);
							}
						}
						@Override
						public void onFailure(Throwable caught) {
							uploadError(caught.getMessage());
						}
					});
				} catch (RestServiceException e) {
					uploadError(e.getMessage());
				}       
		      }
		    };
	    t.schedule(1000);
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
				//upload result has file handle id if successful
				String fileHandleId = uploadResult.getMessage();
				setFileEntityFileHandle(fileHandleId);
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

	public int getDisplayHeight() {
		return view.getDisplayHeight();
	}
	
	public int getDisplayWidth() {
		return view.getDisplayWidth();
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
		uploadLog.append(message+"\n");
		String details = "";
		if (message != null && message.length() > 0)
			details = "  \n" + message;
		view.showErrorMessage(DisplayConstants.ERROR_UPLOAD + details);
		//send full log to server logs
		logger.errorToRepositoryServices(uploadLog.toString());
		//and to the console
		synapseJsniUtils.consoleError(uploadLog.toString());
		uploadLog = new StringBuilder();
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

	private String getBaseFileHandleUrl() {
		return gwt.getModuleBaseURL() + WebConstants.FILE_HANDLE_UPLOAD_SERVLET;
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
	
	/**
	 * for testing purposes only
	 * @param isDirectUploadSupported
	 */
	public void setDirectUploadSupported(boolean isDirectUploadSupported) {
		this.isDirectUploadSupported = isDirectUploadSupported;
	}
}
