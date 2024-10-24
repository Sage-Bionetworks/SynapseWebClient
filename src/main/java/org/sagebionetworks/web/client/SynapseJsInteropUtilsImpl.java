package org.sagebionetworks.web.client;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import elemental2.dom.Blob;
import elemental2.dom.DomGlobal;
import elemental2.dom.File;
import elemental2.dom.FileList;
import elemental2.dom.HTMLInputElement;
import jsinterop.base.Js;
import org.sagebionetworks.web.client.callback.MD5Callback;

public class SynapseJsInteropUtilsImpl implements SynapseJsInteropUtils {

  @Override
  public FileList getFileList(String fileFieldId) {
    elemental2.dom.Element fileToUploadElement =
      DomGlobal.document.getElementById(fileFieldId);
    if (
      fileToUploadElement instanceof HTMLInputElement &&
      ((HTMLInputElement) fileToUploadElement).files != null
    ) return ((HTMLInputElement) fileToUploadElement).files;

    return null;
  }

  private static String getFilesSelected(FileList fileList) {
    StringBuilder out = new StringBuilder();
    for (double i = 0; i < fileList.length; i++) {
      File file = fileList.item(i);
      out.append(file.name).append(';');
    }
    return out.toString();
  }

  @Override
  public String[] getMultipleUploadFileNames(FileList fileList) {
    String unSplitNames = getFilesSelected(fileList);
    if (unSplitNames.isEmpty()) return null;
    return unSplitNames.split(";");
  }

  @Override
  public String getWebkitRelativePath(FileList fileList, double index) {
    return (String) Js
      .asPropertyMap(fileList.item(index))
      .get("webkitRelativePath");
  }

  @Override
  public Element getElementById(String elementId) {
    return Document.get().getElementById(elementId);
  }
}
