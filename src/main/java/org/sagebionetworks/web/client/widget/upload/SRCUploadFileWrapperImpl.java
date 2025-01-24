package org.sagebionetworks.web.client.widget.upload;

import elemental2.dom.Blob;
import elemental2.promise.Promise;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.jsinterop.SRC.SynapseClient.IsCancelled;
import org.sagebionetworks.web.client.jsinterop.SRC.SynapseClient.Progress;

public class SRCUploadFileWrapperImpl implements SRCUploadFileWrapper {

  @Override
  public Promise uploadFile(
    String accessToken,
    String filename,
    Blob file,
    int storageLocationId,
    String contentType,
    Progress progressCallback,
    IsCancelled isCancelled
  ) {
    return SRC.SynapseClient.uploadFile(
      accessToken,
      filename,
      file,
      storageLocationId,
      contentType,
      progressCallback,
      isCancelled
    );
  }
}
