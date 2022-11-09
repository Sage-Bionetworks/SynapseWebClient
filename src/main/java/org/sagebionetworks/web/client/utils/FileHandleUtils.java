package org.sagebionetworks.web.client.utils;

import org.sagebionetworks.repo.model.file.CloudProviderFileHandleInterface;
import org.sagebionetworks.repo.model.file.FileHandle;

public class FileHandleUtils {

  public static boolean isPreviewFileHandle(FileHandle fh) {
    return (
      fh instanceof CloudProviderFileHandleInterface &&
      ((CloudProviderFileHandleInterface) fh).getIsPreview()
    );
  }
}
