package org.sagebionetworks.web.client.events;

public interface UploadSuccessHandler {
  /**
   * Called when one or more files have been successfully uploaded.
   * @param benefactorId the benefactor ID of all uploaded files. May be null if request to get benefactor fails
   */
  void onSuccessfulUpload(String benefactorId);
}
