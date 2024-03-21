package org.sagebionetworks.web.client.widget.upload;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.logical.shared.HasAttachHandlers;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.jsinterop.Promise;
import org.sagebionetworks.web.client.jsinterop.SRC.SynapseClient.FileUploadComplete;
import org.sagebionetworks.web.client.security.AuthenticationController;

/**
 * Perform a multi-part parallel upload utilizing code from the Synapse React Client
 *
 * @author Jay
 *
 */
public class MultipartUploaderImplV2 implements MultipartUploader {

  public static final String PLEASE_SELECT_A_FILE = "Please select a file.";
  public static final String BINARY_CONTENT_TYPE = "application/octet-stream";
  public static final String EMPTY_FILE_ERROR_MESSAGE =
    "The selected file is empty: ";
  // if any parts fail to upload, then it will restart the upload from the beginning up to 10 times,
  // with a 3 second delay between attempts.
  public static final int RETRY_DELAY = 3000;

  private SynapseJSNIUtils synapseJsniUtils;
  private NumberFormat percentFormat;

  JavaScriptObject blob;
  HasAttachHandlers view;
  boolean isCanceled;
  DateTimeUtils dateTimeUtils;
  AuthenticationController auth;
  SRCUploadFileWrapper srcUploadFileWrapper;

  @Inject
  public MultipartUploaderImplV2(
    AuthenticationController auth,
    GWTWrapper gwt,
    SynapseJSNIUtils synapseJsniUtils,
    DateTimeUtils dateTimeUtils,
    SRCUploadFileWrapper srcUploadFileWrapper
  ) {
    super();
    this.auth = auth;
    this.synapseJsniUtils = synapseJsniUtils;
    this.percentFormat = gwt.getNumberFormat("##");
    this.dateTimeUtils = dateTimeUtils;
    this.srcUploadFileWrapper = srcUploadFileWrapper;
  }

  @Override
  public void uploadFile(
    final String fileName,
    final String contentType,
    final JavaScriptObject blob,
    ProgressingFileUploadHandler handler,
    final Long storageLocationId,
    HasAttachHandlers view
  ) {
    this.blob = blob;
    this.view = view;
    isCanceled = false;

    long fileSize = (long) synapseJsniUtils.getFileSize(blob);
    if (fileSize <= 0) {
      handler.uploadSuccess(null);
      return;
    }

    Promise<FileUploadComplete> p = srcUploadFileWrapper.uploadFile(
      auth.getCurrentUserAccessToken(),
      fileName,
      blob,
      storageLocationId.intValue(),
      contentType,
      progress -> {
        double currentProgress = progress.value / progress.total;
        String progressText =
          percentFormat.format(currentProgress * 100.0) + "%";
        handler.updateProgress(currentProgress, progressText, "");
      },
      () -> {
        return isCanceled || !view.isAttached();
      }
    );
    p.then(fileUploadResolve -> {
      handler.uploadSuccess(fileUploadResolve.fileHandleId);
    });
    p.catch_(error -> {
      handler.uploadFailed(error.toString());
    });
  }

  @Override
  public void cancelUpload() {
    isCanceled = true;
  }
}
