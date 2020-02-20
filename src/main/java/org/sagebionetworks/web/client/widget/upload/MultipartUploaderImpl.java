package org.sagebionetworks.web.client.widget.upload;

import java.util.Collections;
import java.util.Date;
import org.sagebionetworks.repo.model.file.AddPartResponse;
import org.sagebionetworks.repo.model.file.AddPartState;
import org.sagebionetworks.repo.model.file.BatchPresignedUploadUrlRequest;
import org.sagebionetworks.repo.model.file.BatchPresignedUploadUrlResponse;
import org.sagebionetworks.repo.model.file.MultipartUploadRequest;
import org.sagebionetworks.repo.model.file.MultipartUploadStatus;
import org.sagebionetworks.repo.model.file.PartPresignedUrl;
import org.sagebionetworks.repo.model.file.PartUtils;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.ProgressCallback;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.utils.Callback;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.logical.shared.HasAttachHandlers;
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
	public static final String BINARY_CONTENT_TYPE = "application/octet-stream";
	public static final String EMPTY_FILE_ERROR_MESSAGE = "The selected file is empty: ";
	// if any parts fail to upload, then it will restart the upload from the beginning up to 10 times,
	// with a 3 second delay between attempts.
	public static final int RETRY_DELAY = 3000;

	private GWTWrapper gwt;
	private SynapseJavascriptClient jsClient;
	private SynapseJSNIUtils synapseJsniUtils;
	private NumberFormat percentFormat;
	private CookieProvider cookies;

	// This class will create a multipart upload request (containing information specific to the file
	// that the user wants to upload).
	private MultipartUploadRequest request;
	// Get the upload status of all parts from the backend. Will be refreshed from the server on each
	// attempt.
	private MultipartUploadStatus currentStatus;
	// For convenient reference, remember how many parts this file upload has.
	private int totalPartCount;
	// Report success/failure/progress to the given ProgressingFileUploadHandler.
	private ProgressingFileUploadHandler handler;
	// Will retry the entire file upload if any part fails to upload. Use this variable to flag the
	// necessity to retry after going through all parts.
	private boolean retryRequired;
	// Keep track of the part number (1-based index) that we are currently trying to upload.
	private int currentPartNumber;
	private int completedPartCount;
	private long startTime, nextProgressPoint;
	private String uploadSpeed;
	// in alpha mode, upload log is sent to the js console
	private boolean isDebugLevelLogging = false;
	JavaScriptObject blob;
	HasAttachHandlers view;
	boolean isCanceled;

	@Inject
	public MultipartUploaderImpl(GWTWrapper gwt, SynapseJSNIUtils synapseJsniUtils, SynapseJavascriptClient jsClient, CookieProvider cookies) {
		super();
		this.gwt = gwt;
		this.synapseJsniUtils = synapseJsniUtils;
		this.jsClient = jsClient;
		this.percentFormat = gwt.getNumberFormat("##");;
		this.cookies = cookies;
	}

	@Override
	public void uploadFile(final String fileName, final String contentType, final JavaScriptObject blob, ProgressingFileUploadHandler handler, final Long storageLocationId, HasAttachHandlers view) {
		// initialize attempt count.
		this.request = null;
		this.totalPartCount = 0;
		this.handler = handler;
		this.blob = blob;
		this.view = view;
		isCanceled = false;
		isDebugLevelLogging = DisplayUtils.isInTestWebsite(cookies);

		// SWC-3779: check for empty file
		long fileSize = (long) synapseJsniUtils.getFileSize(blob);
		if (fileSize <= 0) {
			handler.uploadFailed(EMPTY_FILE_ERROR_MESSAGE + fileName);
			return;
		}

		log(gwt.getUserAgent() + "\n" + gwt.getAppVersion() + "\nDirectly uploading " + fileName + "\n");
		long partSizeBytes = PartUtils.choosePartSize(fileSize);
		// create request
		request = new MultipartUploadRequest();
		request.setContentType(contentType);
		request.setFileName(fileName);
		request.setFileSizeBytes(fileSize);
		request.setPartSizeBytes(partSizeBytes);
		request.setStorageLocationId(storageLocationId);
		startMultipartUpload();
	}

	/**
	 * Start uploading the file
	 */
	public void startMultipartUpload() {
		if (isStillUploading()) {
			retryRequired = false;

			synapseJsniUtils.getFileMd5(blob, md5 -> {
				if (md5 == null) {
					handler.uploadFailed(DisplayConstants.MD5_CALCULATION_ERROR);
					return;
				}
				if (request.getContentMD5Hex() != null && !md5.equals(request.getContentMD5Hex())) {
					uploadFailedDueToFileModification(request.getContentMD5Hex(), md5);
				} else {
					startMultipartUpload(md5);
				}
			});
		}
	}

	private void startMultipartUpload(String md5) {
		request.setContentMD5Hex(md5);
		String fileStats = "fileName=" + request.getFileName() + " MD5=" + request.getContentMD5Hex() + " contentType=" + request.getContentType() + " fileSize=" + request.getFileSizeBytes() + " partSizeBytes=" + request.getPartSizeBytes() + "\n";
		log(fileStats);

		// update the status and process
		jsClient.startMultipartUpload(request, false, new AsyncCallback<MultipartUploadStatus>() {
			@Override
			public void onFailure(Throwable t) {
				logError(t.getMessage());
				handler.uploadFailed(t.getMessage());
			}

			@Override
			public void onSuccess(MultipartUploadStatus status) {
				currentStatus = status;
				currentPartNumber = 0;
				startTime = new Date().getTime();
				nextProgressPoint = 2000;
				uploadSpeed = "";
				totalPartCount = currentStatus.getPartsState().length();
				completedPartCount = getCompletedPartCount(currentStatus.getPartsState());
				attemptToUploadNextPart();
			}
		});
	}

	public int getCompletedPartCount(String partState) {
		int successCount = 0;
		for (int i = 0; i < partState.length(); i++) {
			if (partState.charAt(i) == '1') {
				successCount++;
			}
		}

		return successCount;
	}

	/**
	 * Increment the current part that we are processing. Look at the MultipartUploadStatus part state
	 * to determine if we should try to upload the part, or skip it.
	 */
	public void attemptToUploadNextPart() {
		// for each chunk that still needs to be uploaded, get the presigned url and upload to it
		currentPartNumber++;

		if (currentStatus.getPartsState().charAt(currentPartNumber - 1) == '0') {
			attemptUploadCurrentPart();
		} else {
			// this part has already been uploaded, skip it
			log("attemptChunkUpload: skipping part number = " + currentPartNumber + "\n");
			partSuccess();
		}
	}

	/**
	 * Get a presigned URL for the current part number, upload the part to it, and add the part to the
	 * upload.
	 */
	public void attemptUploadCurrentPart() {
		log("attemptChunkUpload: attempting to upload part number = " + currentPartNumber + "\n");
		BatchPresignedUploadUrlRequest batchPresignedUploadUrlRequest = new BatchPresignedUploadUrlRequest();
		batchPresignedUploadUrlRequest.setContentType(BINARY_CONTENT_TYPE);
		batchPresignedUploadUrlRequest.setPartNumbers(Collections.singletonList(new Long(currentPartNumber)));
		batchPresignedUploadUrlRequest.setUploadId(currentStatus.getUploadId());
		jsClient.getMultipartPresignedUrlBatch(batchPresignedUploadUrlRequest, new AsyncCallback<BatchPresignedUploadUrlResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				partFailure(caught.getMessage());
			}

			@Override
			public void onSuccess(BatchPresignedUploadUrlResponse batchPresignedUploadUrlResponse) {
				if (isStillUploading()) {
					PartPresignedUrl url = batchPresignedUploadUrlResponse.getPartPresignedUrls().get(0);
					String urlString = url.getUploadPresignedUrl();
					final XMLHttpRequest xhr = gwt.createXMLHttpRequest();
					if (xhr != null) {
						xhr.setOnReadyStateChange(new ReadyStateChangeHandler() {
							@Override
							public void onReadyStateChange(XMLHttpRequest xhr) {
								log("XMLHttpRequest.setOnReadyStateChange: readyState=" + xhr.getReadyState() + " status=" + xhr.getStatus() + "\n");
								if (xhr.getReadyState() == 4) { // XMLHttpRequest.DONE=4, posts suggest this value is not resolved in some browsers
									xhr.clearOnReadyStateChange();
									if (xhr.getStatus() == 200) { // OK
										log("XMLHttpRequest.setOnReadyStateChange: OK\n");
										// add part number to the upload (and potentially complete)
										addCurrentPartToMultipartUpload();
									} else {
										log("XMLHttpRequest.setOnReadyStateChange: Failure\n" + xhr.getStatusText());
										partFailure(xhr.getStatus() + ": " + xhr.getStatusText());
									}
								}
							}
						});
					}
					ByteRange range = new ByteRange(currentPartNumber, request.getFileSizeBytes(), request.getPartSizeBytes());
					log("attemptChunkUpload: uploading file chunk. ByteRange=" + range.getStart() + "-" + range.getEnd() + " \n");
					ProgressCallback progressCallback = new ProgressCallback() {
						@Override
						public void updateProgress(double loaded, double total) {
							// 0 < currentPartProgress < 1. We need to add this to the chunks that have already been uploaded.
							// And divide by the total chunk count.
							if (!isStillUploading()) {
								if (xhr != null) {
									xhr.abort();
								}
							} else {
								double currentPartProgress = loaded / total;
								double currentProgress = (((double) (completedPartCount)) + currentPartProgress) / ((double) totalPartCount);
								String progressText = percentFormat.format(currentProgress * 100.0) + "%";
								// update uploadSpeed every couple of seconds
								long msElapsed = (new Date().getTime() - startTime);
								if (msElapsed > 0 && (msElapsed > nextProgressPoint)) {
									double totalBytesTransfered = (request.getPartSizeBytes() * completedPartCount) + loaded;
									uploadSpeed = "(" + DisplayUtils.getFriendlySize(totalBytesTransfered / (msElapsed / 1000), true) + "/s)";
									nextProgressPoint += 2000;
								}
								handler.updateProgress(currentProgress, progressText, uploadSpeed);
							}
						}
					};
					synapseJsniUtils.uploadFileChunk(BINARY_CONTENT_TYPE, blob, range.getStart(), range.getEnd(), urlString, xhr, progressCallback);
				}
			}
		});
	}

	/**
	 * @return True if user is still looking at the upload UI.
	 */
	public boolean isStillUploading() {
		return view.isAttached() && !isCanceled;
	}

	/**
	 * Called if the current part was successfully uploaded. Will continue on to process the next file
	 * part (if there is one).
	 */
	public void partSuccess() {
		checkAllPartsProcessed();
	}

	/**
	 * Called if the current part failed to upload. Will continue on to process the next file part (if
	 * there is one).
	 */
	public void partFailure(String message) {
		logError("Upload error on part " + currentPartNumber + ": \n" + message);
		retryRequired = true;
		checkAllPartsProcessed();
	}

	/**
	 * If all parts have been processed, it will either restart (if there were any problems during
	 * upload that were detected), or attempt to complete the upload. If parts are left, then it will
	 * continue on to process the next file part.
	 */
	public void checkAllPartsProcessed() {
		if (currentPartNumber >= totalPartCount) {
			if (retryRequired) {
				// wait a couple of seconds and start over :(
				gwt.scheduleExecution(new Callback() {
					@Override
					public void invoke() {
						startMultipartUpload();
					}
				}, RETRY_DELAY);
			} else {
				// complete upload and return file handle
				completeMultipartUpload();
			}
		} else {
			attemptToUploadNextPart();
		}
	}

	public void completeMultipartUpload() {
		// before finishing, verify that the file checksum has not changed during the upload.
		synapseJsniUtils.getFileMd5(blob, md5 -> {
			if (!request.getContentMD5Hex().equals(md5)) {
				uploadFailedDueToFileModification(request.getContentMD5Hex(), md5);
			} else {
				completeMultipartUploadAfterMd5Verification();
			}
		});
	}

	private void uploadFailedDueToFileModification(String startMd5, String newMd5) {
		handler.uploadFailed("Unable to upload the file \"" + request.getFileName() + "\" because it's been modified during the upload.  The starting md5 of the file (" + startMd5 + ") differs from the current md5 (" + newMd5 + ").");
	}

	public void completeMultipartUploadAfterMd5Verification() {
		// combine
		jsClient.completeMultipartUpload(currentStatus.getUploadId(), new AsyncCallback<MultipartUploadStatus>() {
			@Override
			public void onFailure(Throwable caught) {
				// failed to complete multipart upload. log it and start over.
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

	public void addCurrentPartToMultipartUpload() {
		// calculate the md5 of this file part
		if (isStillUploading()) {
			synapseJsniUtils.getFilePartMd5(blob, currentPartNumber - 1, request.getPartSizeBytes(), partMd5 -> {
				log("partNumber=" + currentPartNumber + " partNumberMd5=" + partMd5);
				jsClient.addPartToMultipartUpload(currentStatus.getUploadId(), currentPartNumber, partMd5, new AsyncCallback<AddPartResponse>() {
					@Override
					public void onFailure(Throwable caught) {
						partFailure(caught.getMessage());
					}

					public void onSuccess(AddPartResponse addPartResponse) {
						if (addPartResponse.getAddPartState().equals(AddPartState.ADD_SUCCESS)) {
							completedPartCount++;
							partSuccess();
						} else {
							partFailure(addPartResponse.getErrorMessage());
						}
					};
				});
			});
		}
	}

	public void log(String message) {
		if (isDebugLevelLogging) {
			synapseJsniUtils.consoleLog(message);
		}
	}

	public void logError(String message) {
		// to the console
		synapseJsniUtils.consoleError(message);
	}

	@Override
	public void cancelUpload() {
		isCanceled = true;
	}
}
