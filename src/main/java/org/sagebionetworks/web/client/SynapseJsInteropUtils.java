package org.sagebionetworks.web.client;

import com.google.gwt.dom.client.Element;
import elemental2.dom.FileList;

public interface SynapseJsInteropUtils {
  FileList getFileList(String fileFieldId);

  String[] getMultipleUploadFileNames(FileList fileList);

  String getWebkitRelativePath(FileList fileList, double index);

  Element getElementById(String elementId);
}
