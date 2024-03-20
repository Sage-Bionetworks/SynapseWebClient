package org.sagebionetworks.web.client.widget.upload;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.logical.shared.HasAttachHandlers;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.jsinterop.Promise;
import org.sagebionetworks.web.client.jsinterop.SRC.SynapseClient.FileUploadComplete;
import org.sagebionetworks.web.client.security.AuthenticationController;

/**
 * This was extracted from the uploader.
 *
 * @author Jay
 *
 */
public class MultipartUploaderImpl implements MultipartUploader {

  public static final String PLEASE_SELECT_A_FILE = "Please select a file.";
  public static final String BINARY_CONTENT_TYPE = "application/octet-stream";
  public static final String EMPTY_FILE_ERROR_MESSAGE =
    "The selected file is empty: ";
  // if any parts fail to upload, then it will restart the upload from the beginning up to 10 times,
  // with a 3 second delay between attempts.
  public static final int RETRY_DELAY = 3000;

  private GWTWrapper gwt;
  private SynapseJSNIUtils synapseJsniUtils;
  private NumberFormat percentFormat;
  private CookieProvider cookies;

  // in alpha mode, upload log is sent to the js console
  private boolean isDebugLevelLogging = false;
  JavaScriptObject blob;
  HasAttachHandlers view;
  boolean isCanceled;
  DateTimeUtils dateTimeUtils;
  AuthenticationController auth;
  SRCUploadFileWrapper srcUploadFileWrapper;

  @Inject
  public MultipartUploaderImpl(
    AuthenticationController auth,
    GWTWrapper gwt,
    SynapseJSNIUtils synapseJsniUtils,
    CookieProvider cookies,
    DateTimeUtils dateTimeUtils,
    SRCUploadFileWrapper srcUploadFileWrapper
  ) {
    super();
    this.auth = auth;
    this.gwt = gwt;
    this.synapseJsniUtils = synapseJsniUtils;
    this.percentFormat = gwt.getNumberFormat("##");
    this.cookies = cookies;
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
    isDebugLevelLogging = DisplayUtils.isInTestWebsite(cookies);

    long fileSize = (long) synapseJsniUtils.getFileSize(blob);
    if (fileSize <= 0) {
      handler.uploadSuccess(null);
      return;
    }

    log(
      gwt.getUserAgent() +
      "\n" +
      gwt.getAppVersion() +
      "\nDirectly uploading " +
      fileName +
      "\n"
    );
    Promise p = srcUploadFileWrapper.uploadFile(
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
      FileUploadComplete fileUploadComplete =
        (FileUploadComplete) fileUploadResolve;
      handler.uploadSuccess(fileUploadComplete.fileHandleId);
    });
    p.catch_(error -> {
      handler.uploadFailed(error.toString());
    });
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
