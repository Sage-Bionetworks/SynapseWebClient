package org.sagebionetworks.web.client.widget.upload;

import com.google.gwt.core.client.JavaScriptObject;
import org.sagebionetworks.web.client.jsinterop.Promise;
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
    JavaScriptObject file, // blob
    int storageLocationId,
    String contentType,
    Progress progressCallback,
    IsCancelled isCancelled
  );
}
