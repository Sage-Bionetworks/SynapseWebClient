package org.sagebionetworks.web.client.widget.entity.download;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.AccessRequirement;
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
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.utils.GovernanceServiceHelper;
import org.sagebionetworks.web.client.utils.RESTRICTION_LEVEL;
import org.sagebionetworks.web.client.widget.SynapsePersistable;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.dialog.AddAttachmentDialog;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.WebConstants;
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
 * B. File Entity, older client browser: Uploads to the FileHandleServlet servlet, using the Form submit (POST).  @see org.sagebionetworks.web.server.servlet.FileHandleServlet
 * C. File Entity, newer client browser: Direct multipart upload to S3, using a PUT to presigned URLs.
 * 
 * Case C will be the most common case.
 */
public class Uploader implements UploaderView.Presenter, SynapseWidgetPresenter, SynapsePersistable {
	
	//we are dedicating 90% of the progress bar to uploading the chunks, reserving 10% for the final combining (last) step
	public static final double UPLOADING_TOTAL_PERCENT = .9d;
	public static final double COMBINING_TOTAL_PERCENT = .1d;
	public static final double OLD_BROWSER_MAX_SIZE = ClientProperties.MB * 5; //5MB
	public static final double BYTES_PER_CHUNK = ClientProperties.MB * 7; //7MB
	public static final int MAX_RETRY = 3;
	
	private UploaderView view;
	private NodeModelCreator nodeModelCreator;
	private HandlerManager handlerManager;
	private Entity entity;
	private String parentEntityId;
	private List<AccessRequirement> accessRequirements;
	private JSONObjectAdapter jsonObjectAdapter;
	private boolean isDirectUploading;
	private CallbackP<String> fileHandleIdCallback;
	private SynapseClientAsync synapseClient;
	private SynapseJSNIUtils synapseJsniUtils;
	private GWTWrapper gwt;
	AuthenticationController authenticationController;
	private ChunkedFileToken token;
	private boolean isUploadRestricted;
	NumberFormat percentFormat;
	@Inject
	public Uploader(
			UploaderView view, 			
			NodeModelCreator nodeModelCreator, 
			SynapseClientAsync synapseClient,
			JSONObjectAdapter jsonObjectAdapter,
			SynapseJSNIUtils synapseJsniUtils,
			GWTWrapper gwt,
			AuthenticationController authenticationController
			) {
	
		this.view = view;		
		this.nodeModelCreator = nodeModelCreator;
		this.synapseClient = synapseClient;
		this.jsonObjectAdapter=jsonObjectAdapter;
		this.synapseJsniUtils = synapseJsniUtils;
		this.gwt = gwt;
		this.authenticationController = authenticationController;
		view.setPresenter(this);
		percentFormat = gwt.getNumberFormat("##");
		clearHandlers();
	}		
		
	public Widget asWidget(Entity entity, List<AccessRequirement> accessRequirements) {
		return asWidget(entity, null, accessRequirements, null, true);
	}
	
	public Widget asWidget(String parentEntityId, List<AccessRequirement> accessRequirements) {
		return asWidget((Entity)null, parentEntityId, accessRequirements, null, true);
	}
	
	public Widget asWidget(Entity entity, String parentEntityId, List<AccessRequirement> accessRequirements, CallbackP<String> fileHandleIdCallback, boolean isEntity) {
		this.view.setPresenter(this);
		this.entity = entity;
		this.parentEntityId = parentEntityId;
		this.fileHandleIdCallback = fileHandleIdCallback;
		this.accessRequirements = accessRequirements;
		this.view.createUploadForm(isEntity, parentEntityId);
		view.showUploaderUI();
		return this.view.asWidget();
	}

	@SuppressWarnings("unchecked")
	public void clearState() {
		view.clear();
		// remove handlers
		handlerManager = new HandlerManager(this);		
		this.entity = null;
		this.parentEntityId = null;
	}

	@Override
	public Widget asWidget() {
		return null;
	}

	public Entity getEntity() {
		return entity;
	}
	
	@Override
	public String getDefaultUploadActionUrl(boolean isRestricted) {
		isUploadRestricted = isRestricted;
		boolean isFileEntity = entity == null || entity instanceof FileEntity;
		String entityParentString = entity==null && parentEntityId != null ? WebConstants.FILE_HANDLE_FILEENTITY_PARENT_PARAM_KEY + "=" + parentEntityId + "&": "";
		String entityIdString = entity != null ? WebConstants.ENTITY_PARAM_KEY + "=" + entity.getId() + "&" : "";
		String uploadUrl = isFileEntity ? 
				//new way
				synapseJsniUtils.getBaseFileHandleUrl() + "?" + WebConstants.IS_RESTRICTED_PARAM_KEY + "=" +isRestricted + "&" +
						WebConstants.FILE_HANDLE_CREATE_FILEENTITY_PARAM_KEY  + "=" + Boolean.toString(entity == null) + "&" + entityParentString + entityIdString: 
				//old way
				gwt.getModuleBaseURL() + "upload" + "?" + 
					entityIdString +
					WebConstants.IS_RESTRICTED_PARAM_KEY + "=" +isRestricted;
		return uploadUrl;
	}
	
	@Override
	public void handleUpload(String fileName) {
		boolean isFileEntity = entity == null || entity instanceof FileEntity;
		isDirectUploading = isFileEntity && synapseJsniUtils.isDirectUploadSupported();
		if (isDirectUploading) {
			//use case C from above
			directUploadStep0(fileName);
		}
		else {
			//use case A and B from above
			//uses the default action url
			//if using this method, block if file size is > MAX_SIZE
			if (isFileEntity) {
				try {
					double fileSize = synapseJsniUtils.getFileSize(UploaderViewImpl.FILE_FIELD_ID);
					//check
					if (fileSize > OLD_BROWSER_MAX_SIZE) {
						view.showErrorMessage(DisplayConstants.LARGE_FILE_ON_UNSUPPORTED_BROWSER);
						fireCancelEvent();
						return;
					}
				} catch (Exception e) {
					view.showErrorMessage(e.getMessage());
					return;
				}
			}
			view.submitForm();
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
	 * Get the md5
	 * @param fileName
	 */
	public void directUploadStep0(final String fileName) {
		this.token = null;
		//get the chunked file request (includes token)
		//get the content type
		final String contentType = fixDefaultContentType(synapseJsniUtils.getContentType(UploaderViewImpl.FILE_FIELD_ID), fileName);
		synapseJsniUtils.getFileMd5(UploaderViewImpl.FILE_FIELD_ID, new MD5Callback() {
			
			@Override
			public void setMD5(String hexValue) {
				directUploadStep1(fileName, contentType, hexValue);
			}
		});
		
	}
	public void directUploadStep1(final String fileName, final String contentType, String md5){
		try {
			synapseClient.getChunkedFileToken(fileName, contentType, md5, new AsyncCallback<String>() {
				@Override
				public void onSuccess(String result) {
					try {
						token = nodeModelCreator.createJSONEntity(result, ChunkedFileToken.class);
						double fileSize = synapseJsniUtils.getFileSize(UploaderViewImpl.FILE_FIELD_ID);
						long totalChunkCount = (long)Math.ceil(fileSize / BYTES_PER_CHUNK);;
						view.showProgressBar();
						directUploadStep2(contentType, 1, 1, totalChunkCount, fileSize, new ArrayList<String>());
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
	

	/**
	 * 
	 * @param currentChunkNumber The chunk number that should be uploaded
	 * @param currentAttempt This is our nth attempt at uploading this chunk (starting at 1, trying up to MAX_RETRY times)
	 * @param totalChunkCount The total number of chunks to complete upload of the file
	 */
	public void directUploadStep2(final String contentType, final int currentChunkNumber, final int currentAttempt, final long totalChunkCount, final double fileSize, final List<String> requestList){
		//get the presigned upload url
		//and upload the file
		try {
			//create a request for each chunk, and try to upload each one
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
								if (xhr.getReadyState() == 4) { //XMLHttpRequest.DONE=4, posts suggest this value is not resolved in some browsers
									if (xhr.getStatus() == 200) { //OK
										chunkUploadSuccess(requestJson, contentType, currentChunkNumber, totalChunkCount, fileSize, requestList);
									}
									else {
										chunkUploadFailure(contentType, currentChunkNumber, currentAttempt, totalChunkCount, fileSize, requestList);
									}
								}
							}
						});
					}
					ByteRange range = getByteRange(currentChunkNumber, fileSize);
					synapseJsniUtils.uploadFileChunk(contentType, UploaderViewImpl.FILE_FIELD_ID, range.getStart(), range.getEnd(), urlString, xhr, new ProgressCallback() {
						@Override
						public void updateProgress(double value) {
							//Note:  0 <= value <= 1
							//And we need to add this to the chunks that have already been uploaded.  And divide by the total chunk count
							double currentProgress = (((double)(currentChunkNumber-1)) + value)/((double)totalChunkCount) * UPLOADING_TOTAL_PERCENT;
							String progressText = percentFormat.format(currentProgress*100.0) + "%";
							view.updateProgress(currentProgress, progressText);
						}
					});
				}
				@Override
				public void onFailure(Throwable t) {
					uploadError(t.getMessage());		
				}
			});
		} catch (RestServiceException e) {
			uploadError(e.getMessage());
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
	public void chunkUploadSuccess(String requestJson, String contentType, int currentChunkNumber, long totalChunkCount, double fileSize, List<String> requestList){
		//are there more chunks to upload?
		requestList.add(requestJson);
		if (currentChunkNumber >= totalChunkCount)
			directUploadStep3(false, requestList, 1);
		else
			directUploadStep2(contentType, currentChunkNumber+1, 1, totalChunkCount, fileSize, requestList);
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
	public void chunkUploadFailure(String contentType, int currentChunkNumber, int currentAttempt, long totalChunkCount, double fileSize, List<String> requestList) {
		if (currentAttempt >= MAX_RETRY)
			uploadError("Exceeded the maximum number of attempts to upload a single file chunk.");
		else //retry
			directUploadStep2(contentType, currentChunkNumber, currentAttempt+1, totalChunkCount, fileSize, requestList);
	}
	
	public class ByteRange {
		private double start, end;
		public ByteRange(double start, double end) {
			this.start = start;
			this.end = end;
		}
		public double getEnd() {
			return end;
		}
		public double getStart() {
			return start;
		}
	}
	
	public ByteRange getByteRange(int currentChunkNumber, double fileSize) {
		double startByte = (currentChunkNumber-1) * BYTES_PER_CHUNK;
		double endByte = currentChunkNumber * BYTES_PER_CHUNK - 1;
		if (endByte >= fileSize)
			endByte = fileSize-1;
		return new ByteRange(startByte, endByte);
	}
	
	public void directUploadStep3(final boolean isNewlyRestricted, final List<String> requestList, final int currentAttempt){
		//complete the file upload, and refresh
		try {
			final String entityId = entity==null ? null : entity.getId();
			
			//start the daemon to complete the file upload, and continue to check back until it's complete
			synapseClient.combineChunkedFileUpload(requestList, new AsyncCallback<String>() {
				@Override
				public void onSuccess(String result) {
					try {
						UploadDaemonStatus status = nodeModelCreator.createJSONEntity(result, UploadDaemonStatus.class);
						//if it's already done, then finish.  Otherwise keep checking back until it's complete.
						processDaemonStatus(status, entityId, parentEntityId, isUploadRestricted, isNewlyRestricted, requestList, currentAttempt);
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
	
	public void processDaemonStatus(UploadDaemonStatus status, final String entityId, final String parentEntityId, final boolean isUploadRestricted, final boolean isNewlyRestricted, List<String> requestList, int currentAttempt){
		State state = status.getState();
		if (State.COMPLETED == state) {
			view.updateProgress(.99d, "99%");
			if (entityId != null || parentEntityId != null)
				setFileEntityFileHandle(status.getFileHandleId(), entityId, parentEntityId, isUploadRestricted, isNewlyRestricted);
			if (fileHandleIdCallback != null) {
				fileHandleIdCallback.invoke(status.getFileHandleId());
				uploadSuccess(false);
			}
		}
		else if (State.PROCESSING == state){
			//still processing.  update the progress bar and check again later
			double currentProgress = ((status.getPercentComplete()*.01d) * COMBINING_TOTAL_PERCENT) + UPLOADING_TOTAL_PERCENT;
			String progressText = percentFormat.format(currentProgress*100.0) + "%";
			view.updateProgress(currentProgress, progressText);
			checkStatusAgainLater(status.getDaemonId(), entityId, parentEntityId, isUploadRestricted, isNewlyRestricted, requestList, currentAttempt);
		}
		else if (State.FAILED == state) {
			combineChunksUploadFailure(isNewlyRestricted, requestList, currentAttempt, status.getErrorMessage());
		}
	}
	
	public void combineChunksUploadFailure(boolean isNewlyRestricted, List<String> requestList, int currentAttempt, String errorMessage) {
		if (currentAttempt >= MAX_RETRY)
			uploadError("Exceeded the maximum number of attempts to combine all of the parts. " + errorMessage);
		else //retry
			directUploadStep3(isNewlyRestricted, requestList, currentAttempt+1);
	}
	
	public void checkStatusAgainLater(final String daemonId, final String entityId, final String parentEntityId, final boolean isUploadRestricted, final boolean isNewlyRestricted, final List<String> requestList, final int currentAttempt) {
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
								processDaemonStatus(status, entityId, parentEntityId, isUploadRestricted, isNewlyRestricted, requestList, currentAttempt);
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
	
	public void setFileEntityFileHandle(String fileHandleId, final String entityId, String parentEntityId, boolean isUploadRestricted, final boolean isNewlyRestricted) {
		try {
			synapseClient.setFileEntityFileHandle(fileHandleId, entityId, parentEntityId, isUploadRestricted, new AsyncCallback<String>() {
				@Override
				public void onSuccess(String entityId) {
					//to new file handle id, or create new file entity with this file handle id
					view.hideLoading();
					refreshAfterSuccessfulUpload(entityId, isNewlyRestricted);
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
	
	
	@Override
	public void setExternalFilePath(String path, String name, final boolean isNewlyRestricted) {
		if (entity==null || entity instanceof FileEntity) {
			//new data, use the appropriate synapse call
			//if we haven't created the entity yet, do that first
			if (entity == null) {
				createNewExternalFileEntity(path, name, isNewlyRestricted);
			}
			else {
				updateExternalFileEntity(entity.getId(), path, name, isNewlyRestricted);
			}
		}
		else {
			//old data
			String entityId = entity.getId();
			synapseClient.updateExternalLocationable(entityId, path, name, new AsyncCallback<EntityWrapper>() {
				
				public void onSuccess(EntityWrapper result) {
					externalLinkUpdated(result, isNewlyRestricted, entity.getClass());
				};
				@Override
				public void onFailure(Throwable caught) {
					view.showErrorMessage(DisplayConstants.TEXT_LINK_FAILED);
				}
			} );
		}
	}
	
	public void externalLinkUpdated(EntityWrapper result, boolean isNewlyRestricted, Class<? extends Entity> entityClass) {
		try {
			entity = nodeModelCreator.createJSONEntity(result.getEntityJson(), entityClass);
			if (isNewlyRestricted) {
				synapseClient.createLockAccessRequirement(entity.getId(), new AsyncCallback<EntityWrapper>(){
					@Override
					public void onSuccess(EntityWrapper result) {
						view.showInfo(DisplayConstants.TEXT_LINK_FILE, DisplayConstants.TEXT_LINK_SUCCESS);
						entityUpdated();
					}
					@Override
					public void onFailure(Throwable caught) {
						view.showErrorMessage(DisplayConstants.TEXT_LINK_FAILED);
					}
				});
			} else {
				view.showInfo(DisplayConstants.TEXT_LINK_FILE, DisplayConstants.TEXT_LINK_SUCCESS);
				entityUpdated();						
			}
		} catch (JSONObjectAdapterException e) {
			view.showErrorMessage(DisplayConstants.TEXT_LINK_FAILED);
		}
	}
	
	public void updateExternalFileEntity(String entityId, String path, String name, final boolean isNewlyRestricted) {
		try {
			synapseClient.updateExternalFile(entityId, path, name, new AsyncCallback<EntityWrapper>() {
				@Override
				public void onSuccess(EntityWrapper result) {
					externalLinkUpdated(result, isNewlyRestricted, FileEntity.class);
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
	public void createNewExternalFileEntity(final String path, final String name, final boolean isNewlyRestricted) {
		try {
			synapseClient.createExternalFile(parentEntityId, path, name, new AsyncCallback<EntityWrapper>() {
				@Override
				public void onSuccess(EntityWrapper result) {
					externalLinkUpdated(result, isNewlyRestricted, FileEntity.class);
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
	public void handleSubmitResult(String resultHtml, final boolean isNewlyRestricted) {
		if(resultHtml == null) resultHtml = "";
		// response from server
		//try to parse
		UploadResult uploadResult = null;
		String detailedErrorMessage = null;
		try{
			uploadResult = AddAttachmentDialog.getUploadResult(resultHtml);
			if (uploadResult.getUploadStatus() == UploadStatus.SUCCESS) {
				//upload result has the entity id that was created by the FileHandleServlet
				String entityId = uploadResult.getMessage();
				//get the entity, and report success
				refreshAfterSuccessfulUpload(entityId, isNewlyRestricted);
			}else {
				uploadError("Upload result status indicated upload was unsuccessful.");
			}
		} catch (Throwable th) {detailedErrorMessage = th.getMessage();};//wasn't an UplaodResult
		
		if (uploadResult == null) {
			if(!resultHtml.contains(DisplayConstants.UPLOAD_SUCCESS)) {
				uploadError(detailedErrorMessage);
			} else {
				uploadSuccess(isNewlyRestricted);
			}
		}
	}
	
	public void showCancelButton(boolean showCancel) {
		view.setShowCancelButton(showCancel);
	}
		
	@Override
	public boolean isRestricted() {
		return GovernanceServiceHelper.entityRestrictionLevel(accessRequirements)!=RESTRICTION_LEVEL.OPEN;
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
	private void refreshAfterSuccessfulUpload(String entityId, final boolean isNewlyRestricted) {
		synapseClient.getEntity(entityId, new AsyncCallback<EntityWrapper>() {
			@Override
			public void onSuccess(EntityWrapper result) {
				try {
					entity = nodeModelCreator.createEntity(result);
					uploadSuccess(isNewlyRestricted);
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
	}
	
	private void uploadSuccess(boolean isNewlyRestricted) {
		view.showInfo(DisplayConstants.TEXT_UPLOAD_FILE_OR_LINK, DisplayConstants.TEXT_UPLOAD_SUCCESS);
		view.clear();
		handlerManager.fireEvent(new EntityUpdatedEvent());
	}
}
