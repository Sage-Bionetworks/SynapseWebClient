package org.sagebionetworks.web.client.widget.upload;

import java.util.Collections;

import org.sagebionetworks.repo.model.file.AddPartResponse;
import org.sagebionetworks.repo.model.file.AddPartState;
import org.sagebionetworks.repo.model.file.BatchPresignedUploadUrlRequest;
import org.sagebionetworks.repo.model.file.BatchPresignedUploadUrlResponse;
import org.sagebionetworks.repo.model.file.MultipartUploadRequest;
import org.sagebionetworks.repo.model.file.MultipartUploadStatus;
import org.sagebionetworks.repo.model.file.PartPresignedUrl;
import org.sagebionetworks.repo.model.util.ContentTypeUtils;
import org.sagebionetworks.web.client.ClientLogger;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.MultipartFileUploadClientAsync;
import org.sagebionetworks.web.client.ProgressCallback;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.callback.MD5Callback;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.xhr.client.ReadyStateChangeHandler;
import com.google.gwt.xhr.client.XMLHttpRequest;
import com.google.inject.Inject;

/**
 * This was extracted from the uploader.
 * 
 * @author Jay
 *
 */
public class MultipartUploaderImpl implements MultipartUploader {

	public static final String EXCEEDED_THE_MAXIMUM_UPLOAD_A_FILE = "Exceeded the maximum number of attempts to upload a file. Please try again later.";
	public static final String PLEASE_SELECT_A_FILE = "Please select a file.";
	//we are dedicating 90% of the progress bar to uploading the chunks, reserving 10% for the final combining (last) step
	public static final long OLD_BROWSER_MAX_SIZE = (long)ClientProperties.MB * 5; //5MB
	
	public static final int MAX_RETRY = 10;
	public static final int RETRY_DELAY = 3000;
	
	private GWTWrapper gwt;
	private MultipartFileUploadClientAsync multipartFileUploadClient;
	private SynapseJSNIUtils synapseJsniUtils;
	private NumberFormat percentFormat;
	private ClientLogger logger;
	private CookieProvider cookies;
	
	//string builder to capture upload information.  sends to output if any errors occur during direct upload.
	private StringBuilder uploadLog;
	private String fileInputId;
	int fileIndex;
	//This class will create a multipart upload request (containing information specific to the file that the user wants to upload).
	private MultipartUploadRequest request;
	//Get the upload status of all parts from the backend.  Will be refreshed from the server on each attempt.
	private MultipartUploadStatus currentStatus;
	//For convenient reference, remember how many parts this file upload has.
	private int totalPartCount;
	//Report success/failure/progress to the given ProgressingFileUploadHandler.
	private ProgressingFileUploadHandler handler;
	//Will retry the entire file upload if any part fails to upload.  Use this variable to flag the necessity to retry after going through all parts.
	private boolean retryRequired;
	//Keep track of how many times we try to upload the file (processing all parts).
	private int attempt;
	//Keep track of the part number (1-based index) that we are currently trying to upload.
	private int currentPartNumber;
	
	private boolean isDebugLevelLogging = false;
	
	@Inject
	public MultipartUploaderImpl(GWTWrapper gwt,
			SynapseJSNIUtils synapseJsniUtils,
			ClientLogger logger, 
			MultipartFileUploadClientAsync multipartFileUploadClient,
			CookieProvider cookies) {
		super();
		this.gwt = gwt;
		this.synapseJsniUtils = synapseJsniUtils;
		this.multipartFileUploadClient = multipartFileUploadClient;
		this.percentFormat = gwt.getNumberFormat("##");;
		this.logger = logger;
		this.cookies = cookies;
	}
	
	@Override
	public void uploadSelectedFile(String fileInputId,ProgressingFileUploadHandler handler, Long storageLocationId) {
		// First get the name of the file
		String[] names = synapseJsniUtils.getMultipleUploadFileNames(fileInputId);
		if(names == null || names.length < 1){
			handler.uploadFailed(PLEASE_SELECT_A_FILE);
			return;
		}
		int index = 0;
		String fileName = names[0];
		uploadFile(fileName, fileInputId, index, handler, storageLocationId);
	}
	
	@Override
	public void uploadFile(final String fileName, final String fileInputId, final int fileIndex, ProgressingFileUploadHandler handler, final Long storageLocationId) {
		//initialize attempt count. 
		attempt=0;
		this.request = null;
		this.totalPartCount = 0;
		this.fileInputId = fileInputId;
		this.fileIndex = fileIndex;
		this.handler = handler;
		isDebugLevelLogging = DisplayUtils.isInTestWebsite(cookies);
		uploadLog = new StringBuilder();
		log(gwt.getUserAgent() + "\n" + gwt.getAppVersion() + "\nDirectly uploading " + fileName + " - calculating MD5\n");
		synapseJsniUtils.getFileMd5(fileInputId, fileIndex, new MD5Callback() {
			@Override
			public void setMD5(String md5) {
				String contentType = fixDefaultContentType(synapseJsniUtils.getContentType(fileInputId, fileIndex), fileName);
				long fileSize = (long)synapseJsniUtils.getFileSize(fileInputId, fileIndex);
				long partSizeBytes = PartUtils.choosePartSize(fileSize);
				long numberOfParts = PartUtils.calculateNumberOfParts(
						fileSize, partSizeBytes);
				String fileStats = "fileName="+fileName+"MD5=" + md5+" contentType="+contentType+" fileSize="+fileSize + " partSizeBytes=" + partSizeBytes + " numberOfParts=" + numberOfParts+"\n"; 
				log(fileStats);
				//create request
				request = new MultipartUploadRequest();
				request.setContentMD5Hex(md5);
				request.setContentType(contentType);
				request.setFileName(fileName);
				request.setFileSizeBytes(fileSize);
				request.setPartSizeBytes(partSizeBytes);
				request.setStorageLocationId(storageLocationId);
				startMultipartUpload();
			}
		});
	}
	
	/**
	 * Start uploading the file
	 */
	public void startMultipartUpload() {
		attempt++;
		if (attempt <= MAX_RETRY) {
			if (isStillUploading()) {
				retryRequired = false;
				//update the status and process
				multipartFileUploadClient.startMultipartUpload(request, false, new AsyncCallback<MultipartUploadStatus>() {
					@Override
					public void onFailure(Throwable t) {
						logError(t.getMessage());
						handler.uploadFailed(t.getMessage());
					}
					
					@Override
					public void onSuccess(MultipartUploadStatus status) {
						currentStatus = status;
						currentPartNumber = 0;
						totalPartCount = currentStatus.getPartsState().length();
						log("attemptChunkUpload: attempt number "+attempt+" to upload file.\n");
						attemptToUploadNextPart();
					}
				});
			}
		} else {
			handler.uploadFailed(EXCEEDED_THE_MAXIMUM_UPLOAD_A_FILE);
		}
	}
	
	/**
	 * Increment the current part that we are processing.  Look at the MultipartUploadStatus part state to determine if we should try to upload the part, or skip it.
	 */
	public void attemptToUploadNextPart(){
		//for each chunk that still needs to be uploaded, get the presigned url and upload to it
		currentPartNumber++;
		
		if (currentStatus.getPartsState().charAt(currentPartNumber-1) == '0') {
			attemptUploadCurrentPart();
		} else {
			//this part has already been uploaded, skip it
			String skipMessage = "attemptChunkUpload: skipping part number = "+currentPartNumber+"\n";
			log(skipMessage);
			partSuccess();
		}
	}
	
	/**
	 * Get a presigned URL for the current part number, upload the part to it, and add the part to the upload. 
	 */
	public void attemptUploadCurrentPart() {
		log("attemptChunkUpload: attempting to upload part number = "+currentPartNumber+"\n");
		BatchPresignedUploadUrlRequest batchPresignedUploadUrlRequest = new BatchPresignedUploadUrlRequest();
		batchPresignedUploadUrlRequest.setPartNumbers(Collections.singletonList(new Long(currentPartNumber)));
		batchPresignedUploadUrlRequest.setUploadId(currentStatus.getUploadId());
		multipartFileUploadClient.getMultipartPresignedUrlBatch(batchPresignedUploadUrlRequest, new AsyncCallback<BatchPresignedUploadUrlResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				partFailure(currentPartNumber, caught.getMessage());
			}
			@Override
			public void onSuccess(BatchPresignedUploadUrlResponse batchPresignedUploadUrlResponse) {
				if (isStillUploading()) {
					PartPresignedUrl url = batchPresignedUploadUrlResponse.getPartPresignedUrls().get(0);
					String urlString = url.getUploadPresignedUrl();
					XMLHttpRequest xhr = gwt.createXMLHttpRequest();
					if (xhr != null) {
						xhr.setOnReadyStateChange(new ReadyStateChangeHandler() {
							@Override
							public void onReadyStateChange(XMLHttpRequest xhr) {
								log("XMLHttpRequest.setOnReadyStateChange: readyState="+xhr.getReadyState() + " status=" + xhr.getStatus()+"\n");
								if (xhr.getReadyState() == 4) { //XMLHttpRequest.DONE=4, posts suggest this value is not resolved in some browsers
									if (xhr.getStatus() == 200) { //OK
										log("XMLHttpRequest.setOnReadyStateChange: OK\n");
										//add part number to the upload (and potentially complete)
										addPartToUpload();
									} else {
										log("XMLHttpRequest.setOnReadyStateChange: Failure\n" + xhr.getStatusText());
										logFullUpload();
										partFailure(currentPartNumber, uploadLog.toString());
										uploadLog = new StringBuilder();
									}
								}
							}
						});
					}
					ByteRange range = new ByteRange(currentPartNumber, request.getFileSizeBytes(), request.getPartSizeBytes());
					log("attemptChunkUpload: uploading file chunk. ByteRange="+range.getStart()+"-"+range.getEnd()+" \n");
					ProgressCallback progressCallback = new ProgressCallback() {
						@Override
						public void updateProgress(double value) {
							//Note:  0 <= value <= 1
							//And we need to add this to the chunks that have already been uploaded.  And divide by the total chunk count
							double currentProgress = (((double)(currentPartNumber-1)) + value)/((double)totalPartCount);
							String progressText = percentFormat.format(currentProgress*100.0) + "%";
							handler.updateProgress(currentProgress, progressText);
						}
					};
					synapseJsniUtils.uploadFileChunk(request.getContentType(), fileIndex, fileInputId, range.getStart(), range.getEnd(), urlString, xhr, progressCallback);
				}
			}
		});
	}
	
	/**
	 * @return True if user is still looking at the upload UI.
	 */
	public boolean isStillUploading() {
		return synapseJsniUtils.isElementExists(fileInputId);
	}
	
	/**
	 * Called if the current part was successfully uploaded.  Will continue on to process the next file part (if there is one).
	 */
	public void partSuccess() {
		checkAllPartsProcessed();
	}
	
	/**
	 * Called if the current part failed to upload.  Will continue on to process the next file part (if there is one).
	 */
	public void partFailure(int partNumber, String message) {
		logError("Upload error on part " + partNumber + ": \n" + message);
		retryRequired = true;
		checkAllPartsProcessed();
	}
	
	/**
	 * If all parts have been processed, it will either restart (if there were any problems during upload that were detected), or attempt to complete the upload.
	 * If parts are left, then it will continue on to process the next file part.
	 */
	public void checkAllPartsProcessed() {
		if (currentPartNumber >= totalPartCount) {
			if (retryRequired) {
				//wait a couple of seconds and start over :(
				gwt.scheduleExecution(new Callback() {
					@Override
					public void invoke() {
						startMultipartUpload();		
					}
				}, RETRY_DELAY);
			} else {
				//complete upload and return file handle
				completeMultipartUpload();
			}
		} else {
			attemptToUploadNextPart();
		}
	}
	
	public void completeMultipartUpload() {
		logFullUpload();
		//combine
		multipartFileUploadClient.completeMultipartUpload(currentStatus.getUploadId(), new AsyncCallback<MultipartUploadStatus>() {
			@Override
			public void onFailure(Throwable caught) {
				//failed to complete multipart upload.  log it and start over.
				logError(caught.getMessage());
				retryRequired = true;
				checkAllPartsProcessed();
			}
			
			@Override
			public void onSuccess(MultipartUploadStatus status) {
				handler.uploadSuccess(status.getResultFileHandleId());
			}
		});
	}
	
	public void addPartToUpload() {
		//calculate the md5 of this file part
		if (isStillUploading()) {
			synapseJsniUtils.getFilePartMd5(fileInputId, currentPartNumber-1, request.getPartSizeBytes(), fileIndex, new MD5Callback() {
				@Override
				public void setMD5(String partMd5) {
					log("partNumber="+currentPartNumber + " partNumberMd5="+partMd5);
					multipartFileUploadClient.addPartToMultipartUpload(currentStatus.getUploadId(), currentPartNumber, partMd5, new AsyncCallback<AddPartResponse>() {
						@Override
						public void onFailure(Throwable caught) {
							partFailure(currentPartNumber, caught.getMessage());
						}
						
						public void onSuccess(AddPartResponse addPartResponse) {
							if (addPartResponse.getAddPartState().equals(AddPartState.ADD_SUCCESS)) {
								partSuccess();	
							} else {
								partFailure(currentPartNumber, addPartResponse.getErrorMessage());
							}
						};
					});
				}});
		}
	}
	
	public void log(String message) {
		if (isDebugLevelLogging) {
			synapseJsniUtils.consoleLog(message);
		}
		uploadLog.append(message);
	}

	public void logError(String message) {
		uploadLog.append(message+"\n");
		//and to the console
		synapseJsniUtils.consoleError(message);
		logger.error(message);
	}
	

	/**
	 * Logs entire upload (and resets the upload log). 
	 */
	private void logFullUpload() {
		log(uploadLog.toString());
		uploadLog = new StringBuilder();
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
			else if (lowercaseFilename.endsWith(".csv")) {
				contentType = WebConstants.TEXT_COMMA_SEPARATED_VALUES;
			}
			else if (lowercaseFilename.endsWith(".txt")) {
				contentType = ContentTypeUtils.PLAIN_TEXT;
			}
		}
		return contentType;
	}

	
}
