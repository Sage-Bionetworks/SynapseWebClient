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
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.MultipartFileUploadClientAsync;
import org.sagebionetworks.web.client.ProgressCallback;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.callback.MD5Callback;
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

	public static final String PLEASE_SELECT_A_FILE = "Please select a file.";
	//we are dedicating 90% of the progress bar to uploading the chunks, reserving 10% for the final combining (last) step
	public static final long OLD_BROWSER_MAX_SIZE = (long)ClientProperties.MB * 5; //5MB
	
	public static final int RETRY_DELAY = 2000;
	public static final int MAX_RETRIES = 10;
	
	private GWTWrapper gwt;
	private MultipartFileUploadClientAsync multipartFileUploadClient;
	private SynapseJSNIUtils synapseJsniUtils;
	private NumberFormat percentFormat;
	private ClientLogger logger;

	//string builder to capture upload information.  sends to output if any errors occur during direct upload.
	private StringBuilder uploadLog;
	private String fileInputId;
	int fileIndex;
	private MultipartUploadRequest request;
	private String uploadId;
	private int totalPartCount;
	private ProgressingFileUploadHandler handler;
	private int completedChunkCount;
	private boolean retryRequired;
	private int attempt;
	@Inject
	public MultipartUploaderImpl(GWTWrapper gwt,
			SynapseClientAsync synapseClient,
			SynapseJSNIUtils synapseJsniUtils,
			ClientLogger logger, 
			MultipartFileUploadClientAsync multipartFileUploadClient) {
		super();
		this.gwt = gwt;
		this.synapseJsniUtils = synapseJsniUtils;
		this.multipartFileUploadClient = multipartFileUploadClient;
		this.percentFormat = gwt.getNumberFormat("##");;
		this.logger = logger;
	}
	
	/**
	 * 
	 */
	@Override
	public void uploadFile(final String fileName, final String fileInputId, final int fileIndex, ProgressingFileUploadHandler handler, final Long storageLocationId) {
		//initialize attempt count. 
		attempt=0;
		this.request = null;
		this.uploadId = null;
		this.totalPartCount = 0;
		this.fileInputId = fileInputId;
		this.fileIndex = fileIndex;
		this.handler = handler;
		uploadLog = new StringBuilder();
		uploadLog.append(gwt.getUserAgent() + "\n" + gwt.getAppVersion() + "\nDirectly uploading " + fileName + " - calculating MD5\n");
		synapseJsniUtils.getFileMd5(fileInputId, fileIndex, new MD5Callback() {
			@Override
			public void setMD5(String md5) {
				String contentType = fixDefaultContentType(synapseJsniUtils.getContentType(fileInputId, fileIndex), fileName);
				long fileSize = (long)synapseJsniUtils.getFileSize(fileInputId, fileIndex);
				long partSizeBytes = PartUtils.choosePartSize(fileSize);
				long numberOfParts = PartUtils.calculateNumberOfParts(
						fileSize, partSizeBytes);
				String fileStats = "fileName="+fileName+"MD5=" + md5+" contentType="+contentType+" fileSize="+fileSize + " partSizeBytes=" + partSizeBytes + " numberOfParts=" + numberOfParts+"\n"; 
				uploadLog.append(fileStats);
				synapseJsniUtils.consoleLog(fileStats);
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
	 * Step two of an upload is to create a ChunkedFileToken.
	 */
	public void startMultipartUpload(){
		attempt++;
		if (synapseJsniUtils.isElementExists(fileInputId) && attempt <= MAX_RETRIES) {
			retryRequired = false;
			multipartFileUploadClient.startMultipartUpload(request, false, new AsyncCallback<MultipartUploadStatus>() {
				@Override
				public void onFailure(Throwable t) {
					logError(t.getMessage());
					handler.uploadFailed(t.getMessage());
				}
				
				@Override
				public void onSuccess(MultipartUploadStatus status) {
					attemptChunkUpload(request, status);
				}
			});
		} else {
			handler.uploadFailed("Unable to upload the file at this time. Please try again later.");
		}
	}
	
	/**
	 * Step three of an upload is to upload each chunk.
	 */
	public void attemptChunkUpload(final MultipartUploadRequest request, MultipartUploadStatus currentStatus){
		//for each chunk that still needs to be uploaded, get the presigned url and upload to it
		completedChunkCount = 0;
		uploadId = currentStatus.getUploadId();
		String partState = currentStatus.getPartsState();
		totalPartCount = partState.length();
		uploadLog.append("attemptChunkUpload: attempt number "+attempt+" to upload file.\n");
		for (int i = 0; i < partState.length(); i++) {
			//part is 1 based
			final int currentPartNumber = i + 1;
			if (partState.charAt(i) == '0') {
				//needs to be uploaded
				uploadLog.append("attemptChunkUpload: attempting to upload part number = "+currentPartNumber+"\n");
				BatchPresignedUploadUrlRequest batchPresignedUploadUrlRequest = new BatchPresignedUploadUrlRequest();
				batchPresignedUploadUrlRequest.setPartNumbers(Collections.singletonList(new Long(currentPartNumber)));
				batchPresignedUploadUrlRequest.setUploadId(currentStatus.getUploadId());
				multipartFileUploadClient.getMultipartPresignedUrlBatch(batchPresignedUploadUrlRequest, new AsyncCallback<BatchPresignedUploadUrlResponse>() {
					@Override
					public void onFailure(Throwable caught) {
						partFailure(currentPartNumber, caught.getMessage());
					}
					public void onSuccess(BatchPresignedUploadUrlResponse batchPresignedUploadUrlResponse) {
						PartPresignedUrl url = batchPresignedUploadUrlResponse.getPartPresignedUrls().get(0);
						String urlString = url.getUploadPresignedUrl();
						XMLHttpRequest xhr = gwt.createXMLHttpRequest();
						if (xhr != null) {
							xhr.setOnReadyStateChange(new ReadyStateChangeHandler() {
								@Override
								public void onReadyStateChange(XMLHttpRequest xhr) {
									uploadLog.append("XMLHttpRequest.setOnReadyStateChange: readyState="+xhr.getReadyState() + " status=" + xhr.getStatus()+"\n");
									if (xhr.getReadyState() == 4) { //XMLHttpRequest.DONE=4, posts suggest this value is not resolved in some browsers
										if (xhr.getStatus() == 200) { //OK
											uploadLog.append("XMLHttpRequest.setOnReadyStateChange: OK\n");
											//add part number to the upload (and potentially complete)
											addPartToUpload(uploadId, currentPartNumber);
										} else {
											uploadLog.append("XMLHttpRequest.setOnReadyStateChange: Failure\n" + xhr.getStatusText());
											partFailure(currentPartNumber, uploadLog.toString());
											uploadLog = new StringBuilder();
										}
									}
								}
							});
						}
						ByteRange range = getByteRange(currentPartNumber, request.getFileSizeBytes(), request.getPartSizeBytes());
						uploadLog.append("attemptChunkUpload: uploading file chunk. ByteRange="+range.getStart()+"-"+range.getEnd()+" \n");
						synapseJsniUtils.uploadFileChunk(request.getContentType(), fileIndex, fileInputId, range.getStart(), range.getEnd(), urlString, xhr, new ProgressCallback() {
							@Override
							public void updateProgress(double value) {
								//Note:  0 <= value <= 1
								//And we need to add this to the chunks that have already been uploaded.  And divide by the total chunk count
								double currentProgress = (((double)(completedChunkCount)) + value)/((double)totalPartCount);
								String progressText = percentFormat.format(currentProgress*100.0) + "%";
								handler.updateProgress(currentProgress, progressText);
							}
						});
					};
				});		
			} else {
				//this part has already been uploaded, skip it
				uploadLog.append("attemptChunkUpload: skipping part number = "+currentPartNumber+"\n");
				partSuccess();
			}
		}
	}
	
	/**
	 * Step three of an upload is to upload each chunk.
	 */
	public void attemptChunkUpload(final MultipartUploadRequest request, MultipartUploadStatus currentStatus, final int partNumber){
		
	}
	
	
	public void partSuccess() {
		completedChunkCount++;
		checkAllPartsProcessed();
	}
	
	public void partFailure(int partNumber, String message) {
		logError("Upload error on part " + partNumber + ": \n" + message);
		completedChunkCount++;
		retryRequired = true;
		checkAllPartsProcessed();
	}
	
	public void checkAllPartsProcessed() {
		if (completedChunkCount >= totalPartCount) {
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
		}
	}
	
	public void completeMultipartUpload() {
		synapseJsniUtils.consoleLog(uploadLog.toString());
		//combine
		multipartFileUploadClient.completeMultipartUpload(uploadId, new AsyncCallback<MultipartUploadStatus>() {
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
	
	public void addPartToUpload(final String uploadId, final int partNumber) {
		//calculate the md5 of this file part
		ByteRange range = getByteRange(partNumber, request.getFileSizeBytes(), request.getPartSizeBytes());
		synapseJsniUtils.getFilePartMd5(fileInputId, range.getStart(), range.getEnd(), fileIndex, new MD5Callback() {
			@Override
			public void setMD5(String partMd5) {
				synapseJsniUtils.consoleLog("partNumber="+partNumber + " partNumberMd5="+partMd5);
				multipartFileUploadClient.addPartToMultipartUpload(uploadId, partNumber, partMd5, new AsyncCallback<AddPartResponse>() {
					@Override
					public void onFailure(Throwable caught) {
						partFailure(partNumber, caught.getMessage());
					}
					
					public void onSuccess(AddPartResponse addPartResponse) {
						if (addPartResponse.getAddPartState().equals(AddPartState.ADD_SUCCESS)) {
							partSuccess();	
						} else {
							partFailure(partNumber, addPartResponse.getErrorMessage());
						}
					};
				});
			}});
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
	
	public ByteRange getByteRange(int partNumber, Long fileSize, long partSize) {
		long startByte = (partNumber-1) * partSize;
		long endByte = partNumber * partSize - 1;
		if (endByte >= fileSize)
			endByte = fileSize-1;
		return new ByteRange(startByte, endByte);
	}	
	
	private void logError(String message) {
		uploadLog.append(message+"\n");
		//and to the console
		synapseJsniUtils.consoleError(message);
		logger.error(message);
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
