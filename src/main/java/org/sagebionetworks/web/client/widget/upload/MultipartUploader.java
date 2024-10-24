package org.sagebionetworks.web.client.widget.upload;

import com.google.gwt.event.logical.shared.HasAttachHandlers;
import elemental2.dom.Blob;

/**
 * Abstraction for a muti-part file uploader.
 *
 * @author Jay
 *
 */
public interface MultipartUploader {
  /**
   * Upload a single file using multi-part upload.
   *
   * @param fileName
   * @param inputFile
   * @param handler
   */
  void uploadFile(
    String fileName,
    String contentType,
    Blob blob,
    ProgressingFileUploadHandler handler,
    Long storageLocationId,
    HasAttachHandlers view
  );

  void cancelUpload();
}
