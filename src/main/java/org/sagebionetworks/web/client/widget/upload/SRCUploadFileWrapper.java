package org.sagebionetworks.web.client.widget.upload;

import elemental2.dom.Blob;
import elemental2.promise.Promise;
import org.sagebionetworks.web.client.jsinterop.SRC.SynapseClient.FileUploadComplete;
import org.sagebionetworks.web.client.jsinterop.SRC.SynapseClient.IsCancelled;
import org.sagebionetworks.web.client.jsinterop.SRC.SynapseClient.Progress;

/**
 * Abstraction for the SRC implementation of performing a multi-part file upload to Synapse.
 *
 * @author Jay
 *
 */
public interface SRCUploadFileWrapper {
  public Promise<FileUploadComplete> uploadFile(
    String accessToken,
    String filename,
    Blob file,
    int storageLocationId,
    String contentType,
    Progress progressCallback,
    IsCancelled isCancelled
  );
}
