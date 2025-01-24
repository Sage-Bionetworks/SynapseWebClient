package org.sagebionetworks.web.client.widget.upload;

import com.google.gwt.event.logical.shared.HasAttachHandlers;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.inject.Inject;
import elemental2.dom.Blob;
import elemental2.promise.Promise;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.jsinterop.SRC.SynapseClient.FileUploadComplete;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.shared.WebConstants;

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

  Blob blob;
  HasAttachHandlers view;
  boolean isCanceled;
  DateTimeUtils dateTimeUtils;
  AuthenticationController auth;
  SRCUploadFileWrapper srcUploadFileWrapper;
  private SynapseProperties synapseProperties;

  @Inject
  public MultipartUploaderImplV2(
    AuthenticationController auth,
    GWTWrapper gwt,
    SynapseJSNIUtils synapseJsniUtils,
    DateTimeUtils dateTimeUtils,
    SRCUploadFileWrapper srcUploadFileWrapper,
    SynapseProperties synapseProperties
  ) {
    super();
    this.auth = auth;
    this.synapseJsniUtils = synapseJsniUtils;
    this.percentFormat = gwt.getNumberFormat("##");
    this.dateTimeUtils = dateTimeUtils;
    this.srcUploadFileWrapper = srcUploadFileWrapper;
    this.synapseProperties = synapseProperties;
  }

  @Override
  public void uploadFile(
    final String fileName,
    final String contentType,
    final Blob blob,
    ProgressingFileUploadHandler handler,
    final Long storageLocationId,
    HasAttachHandlers view
  ) {
    Long defaultStorageId = Long.parseLong(
      synapseProperties.getSynapseProperty(
        WebConstants.DEFAULT_STORAGE_ID_PROPERTY_KEY
      )
    );

    int storageLocationIntValue = storageLocationId == null
      ? defaultStorageId.intValue()
      : storageLocationId.intValue();
    this.blob = blob;
    this.view = view;
    isCanceled = false;

    long fileSize = blob.size;
    if (fileSize <= 0) {
      handler.uploadSuccess(null);
      return;
    }

    Promise<FileUploadComplete> p = srcUploadFileWrapper.uploadFile(
      auth.getCurrentUserAccessToken(),
      fileName,
      blob,
      storageLocationIntValue,
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
      return null;
    });
    p.catch_(error -> {
      handler.uploadFailed(error.toString());
      return null;
    });
  }

  @Override
  public void cancelUpload() {
    isCanceled = true;
  }
}
