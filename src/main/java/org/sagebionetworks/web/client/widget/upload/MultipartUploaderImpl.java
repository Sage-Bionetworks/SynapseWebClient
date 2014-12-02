package org.sagebionetworks.web.client.widget.upload;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.file.ChunkRequest;
import org.sagebionetworks.repo.model.file.ChunkedFileToken;
import org.sagebionetworks.repo.model.file.State;
import org.sagebionetworks.repo.model.file.UploadDaemonStatus;
import org.sagebionetworks.repo.model.util.ContentTypeUtils;
import org.sagebionetworks.web.client.ClientLogger;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.ProgressCallback;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.callback.MD5Callback;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.exceptions.CombineFileChunksException;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Timer;
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

	public static final String EXCEEDED_THE_MAXIMUM_UPLOAD_A_SINGLE_FILE_CHUNK = "Exceeded the maximum number of attempts to upload a single file chunk. ";
	public static final String EXCEEDED_THE_MAXIMUM_COMBINE_ALL_OF_THE_PARTS = "Exceeded the maximum number of attempts to combine all of the parts. ";
	public static final String PLEASE_SELECT_A_FILE = "Please select a file.";
	//we are dedicating 90% of the progress bar to uploading the chunks, reserving 10% for the final combining (last) step
	public static final double UPLOADING_TOTAL_PERCENT = .9d;
	public static final double COMBINING_TOTAL_PERCENT = .1d;
	public static final long OLD_BROWSER_MAX_SIZE = (long)ClientProperties.MB * 5; //5MB
	public static final long BYTES_PER_CHUNK = (long)ClientProperties.MB * 5; //5MB
	public static final int MAX_RETRY = 5;
	public static final int RETRY_DELAY = 1000;
	
	private GWTWrapper gwt;
	private SynapseClientAsync synapseClient;
	private SynapseJSNIUtils synapseJsniUtils;
	private NumberFormat percentFormat;
	private ClientLogger logger;

	//string builder to capture upload information.  sends to output if any errors occur during direct upload.
	private StringBuilder uploadLog;
	private ChunkedFileToken token;
	private String fileName;
	private String fileInputId;
	int fileIndex;
	private String contentType;
	private String md5;
	private ProgressingFileUploadHandler handler;
	
	@Inject
	public MultipartUploaderImpl(GWTWrapper gwt,
			SynapseClientAsync synapseClient,
			SynapseJSNIUtils synapseJsniUtils,
			ClientLogger logger) {
		super();
		this.gwt = gwt;
		this.synapseClient = synapseClient;
		this.synapseJsniUtils = synapseJsniUtils;
		this.percentFormat = gwt.getNumberFormat("##");;
		this.logger = logger;
	}
	
	/**
	 * 
	 */
	@Override
	public void uploadFile(final String fileName, final String fileInputId, final int fileIndex, ProgressingFileUploadHandler handler) {
		this.token = null;
		this.fileName = fileName;
		this.fileInputId = fileInputId;
		this.fileIndex = fileIndex;
		this.handler = handler;
		uploadLog = new StringBuilder();
		uploadLog.append(gwt.getUserAgent() + "\n" + gwt.getAppVersion() + "\nDirectly uploading " + fileName + " - calculating MD5\n");
		
		//get the chunked file request (includes token)
		//get the content type
		contentType = fixDefaultContentType(synapseJsniUtils.getContentType(fileInputId, fileIndex), fileName);
		synapseJsniUtils.getFileMd5(fileInputId, fileIndex, new MD5Callback() {
			
			@Override
			public void setMD5(String hexValue) {
				uploadLog.append("MD5=" + hexValue+"\ncontentType="+contentType+"\n");
				md5 = hexValue;
				directUploadStep2();
			}
		});
	}
	
	/**
	 * Step two of an upload is to create a ChunkedFileToken.
	 */
	private void directUploadStep2(){
		try {
			synapseClient.getChunkedFileToken(fileName, contentType, md5, new AsyncCallback<ChunkedFileToken>() {
				@Override
				public void onSuccess(ChunkedFileToken result) {
					token = result;
					long fileSize = (long)synapseJsniUtils.getFileSize(fileInputId, fileIndex);
					long totalChunkCount = getChunkCount(fileSize);
					uploadLog.append("fileSize="+fileSize + " totalChunkCount=" + totalChunkCount+"\n");
					attemptChunkUpload(1, 1, totalChunkCount, fileSize, new ArrayList<ChunkRequest>());
				}
				@Override
				public void onFailure(Throwable t) {
					uploadError(t.getMessage(), t);
				}
			});
		} catch (RestServiceException e) {
			uploadError(e.getMessage(), e);
		}
	}
	
	/**
	 * Calculate a chunk size for a given file size.
	 * 
	 * @param fileSize
	 * @return
	 */
	public long getChunkCount(long fileSize) {
		return (long)Math.ceil((double)fileSize / (double)BYTES_PER_CHUNK);
	}
	
	/**
	 * Step three of an upload is to upload each chunk.
	 * 
	 * @param currentChunkNumber
	 * @param currentAttempt
	 * @param totalChunkCount
	 * @param fileSize
	 * @param requestList
	 */
	public void attemptChunkUpload(final int currentChunkNumber, final int currentAttempt, final long totalChunkCount, final long fileSize, final List<ChunkRequest> requestList){
		//get the presigned upload url
		//and upload the file
		try {
			//create a request for each chunk, and try to upload each one
			uploadLog.append("directUploadStep4: currentChunkNumber="+currentChunkNumber + " currentAttempt=" + currentAttempt+"\n");
			final ChunkRequest request = new ChunkRequest();
			request.setChunkedFileToken(token);
			request.setChunkNumber((long) currentChunkNumber);
			synapseClient.getChunkedPresignedUrl(request, new AsyncCallback<String>() {
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
										chunkUploadSuccess(request, currentChunkNumber, totalChunkCount, fileSize, requestList);
									} else {
										uploadLog.append("XMLHttpRequest.setOnReadyStateChange: Failure\n");
										chunkUploadFailure(currentChunkNumber, currentAttempt, totalChunkCount, fileSize, requestList, xhr.getStatusText());
									}
								}
							}
						});
					}
					ByteRange range = getByteRange(currentChunkNumber, fileSize);
					uploadLog.append("directUploadStep2: uploading file chunk.  ByteRange="+range.getStart()+"-"+range.getEnd()+" \n");
					synapseJsniUtils.uploadFileChunk(contentType, fileIndex, fileInputId, range.getStart(), range.getEnd(), urlString, xhr, new ProgressCallback() {
						@Override
						public void updateProgress(double value) {
							//Note:  0 <= value <= 1
							//And we need to add this to the chunks that have already been uploaded.  And divide by the total chunk count
							double currentProgress = ((((double)(currentChunkNumber-1)) + value)/((double)totalChunkCount) * UPLOADING_TOTAL_PERCENT);
							String progressText = percentFormat.format(currentProgress*100.0) + "%";
							handler.updateProgress(currentProgress, progressText);
						}
					});
				}
				@Override
				public void onFailure(Throwable t) {
					chunkUploadFailure(currentChunkNumber, currentAttempt, totalChunkCount, fileSize, requestList, t.getMessage());
				}
			});
		} catch (RestServiceException e) {
			chunkUploadFailure(currentChunkNumber, currentAttempt, totalChunkCount, fileSize, requestList, e.getMessage());
		}
	}
	
	/**
	 * The fourth step of an upload involves starting the job that will combine all of the chunks. 
	 * @param requestList
	 * @param currentAttempt
	 */
	public void attemptCombineChunks(final List<ChunkRequest> requestList, final int currentAttempt){
		uploadLog.append("directUploadStep5: currentAttempt=" + currentAttempt+"\n");
		//complete the file upload, and refresh
		try {
			//start the daemon to complete the file upload, and continue to check back until it's complete
			synapseClient.combineChunkedFileUpload(requestList, new AsyncCallback<UploadDaemonStatus>() {
				@Override
				public void onSuccess(UploadDaemonStatus result) {
					//if it's already done, then finish.  Otherwise keep checking back until it's complete.
					processDaemonStatus(result,  requestList, currentAttempt);
				}
				@Override
				public void onFailure(Throwable caught) {
					combineChunksUploadFailure(requestList, currentAttempt, caught.getMessage());
				}
			});
		} catch (RestServiceException e) {
			uploadError(e.getMessage(), e);
		}
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
	public void chunkUploadFailure(final int currentChunkNumber, final int currentAttempt, final long totalChunkCount, final long fileSize, final List<ChunkRequest> requestList, String detailedMessage) {
		if (currentAttempt >= MAX_RETRY)
			uploadError(EXCEEDED_THE_MAXIMUM_UPLOAD_A_SINGLE_FILE_CHUNK + detailedMessage, new CombineFileChunksException(EXCEEDED_THE_MAXIMUM_UPLOAD_A_SINGLE_FILE_CHUNK));
		else { //retry
			//sleep for a second on the client, then try again.
			gwt.scheduleExecution(new Callback() {
				@Override
				public void invoke() {
					attemptChunkUpload(currentChunkNumber, currentAttempt+1, totalChunkCount, fileSize, requestList);
				}
			}, RETRY_DELAY);
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
	public void chunkUploadSuccess(ChunkRequest chunkedRequest, int currentChunkNumber, long totalChunkCount, long fileSize, List<ChunkRequest> requestList){
		//are there more chunks to upload?
		requestList.add(chunkedRequest);
		if (currentChunkNumber >= totalChunkCount)
			attemptCombineChunks(requestList, 1);
		else
			attemptChunkUpload(currentChunkNumber+1, 1, totalChunkCount, fileSize, requestList);
	}
	
	private void combineChunksUploadFailure(List<ChunkRequest> requestList, int currentAttempt, String errorMessage) {
		if (currentAttempt >= MAX_RETRY)
			uploadError(EXCEEDED_THE_MAXIMUM_COMBINE_ALL_OF_THE_PARTS + errorMessage, new Exception(EXCEEDED_THE_MAXIMUM_COMBINE_ALL_OF_THE_PARTS));
		else //retry
			attemptCombineChunks(requestList, currentAttempt+1);
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
	
	
	private void processDaemonStatus(UploadDaemonStatus status, List<ChunkRequest> requestList, int currentAttempt){
		State state = status.getState();
		if (State.COMPLETED == state) {
			handler.updateProgress(.99d, "99%");
			handler.uploadSuccess(status.getFileHandleId());
		}
		else if (State.PROCESSING == state){
			//still processing.  update the progress bar and check again later
			double currentProgress = (((status.getPercentComplete()*.01d) * COMBINING_TOTAL_PERCENT) + UPLOADING_TOTAL_PERCENT);
			String progressText = percentFormat.format(currentProgress*100.0) + "%";
			handler.updateProgress(currentProgress, progressText);
			checkStatusAgainLater(status.getDaemonId(), requestList, currentAttempt);
		}
		else if (State.FAILED == state) {
			combineChunksUploadFailure(requestList, currentAttempt, status.getErrorMessage());
		}
	}
	
	
	public void checkStatusAgainLater(final String daemonId, final List<ChunkRequest> requestList, final int currentAttempt) {
		//in one second, do a web service call to check the status again
		Timer t = new Timer() {
		      public void run() {
		    	  try {
					synapseClient.getUploadDaemonStatus(daemonId, new AsyncCallback<UploadDaemonStatus>() {
						@Override
						public void onSuccess(UploadDaemonStatus result) {
							// if it's already done, then finish. Otherwise keep checking back until it's complete.
							processDaemonStatus(result, requestList, currentAttempt);
						}
						@Override
						public void onFailure(Throwable caught) {
							uploadError(caught.getMessage(), caught);
						}
					});
				} catch (RestServiceException e) {
					uploadError(e.getMessage(), e);
				}       
		      }
		    };
	    t.schedule(1000);
	}
	
	private void uploadError(String message, Throwable t) {
		uploadLog.append(message+"\n");
		handler.uploadFailed(message);
		//send full log to server logs
		logger.errorToRepositoryServices(uploadLog.toString(), t);
		//and to the console
		synapseJsniUtils.consoleError(uploadLog.toString());
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


	@Override
	public void uploadSelectedFile(String fileInputId,ProgressingFileUploadHandler handler) {
		// First get the name of the file
		String[] names = synapseJsniUtils.getMultipleUploadFileNames(fileInputId);
		if(names == null || names.length < 1){
			handler.uploadFailed(PLEASE_SELECT_A_FILE);
			return;
		}
		int index = 0;
		String fileName = names[0];
		uploadFile(fileName, fileInputId, index, handler);
	}

	@Override
	public FileMetadata[] getSelectedFileMetadata(String inputId) {
		FileMetadata[] results = null;
		String[] fileNames = synapseJsniUtils.getMultipleUploadFileNames(inputId);
		if(fileNames != null){
			results = new FileMetadata[fileNames.length];
			for(int i=0; i<fileNames.length; i++){
				String name = fileNames[i];
				String contentType = fixDefaultContentType(synapseJsniUtils.getContentType(inputId, i), name);
				results[i] = new FileMetadata(name, contentType);
			}
		}
		return results;
	}

	
}
