package org.sagebionetworks.web.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.xhr.client.XMLHttpRequest;
import elemental2.dom.Blob;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.web.client.callback.MD5Callback;
import org.sagebionetworks.web.client.widget.provenance.nchart.LayoutResult;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartCharacters;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartLayersArray;

public interface SynapseJSNIUtils {
  public String getCurrentHistoryToken();

  public void highlightCodeBlocks();

  public void loadTableSorters();

  public String getBaseFileHandleUrl();

  public String getFileHandleAssociationUrl(
    String objectId,
    FileHandleAssociateType objectType,
    String fileHandleId
  );

  public String getRawFileHandleUrl(String fileHandleId);

  public int randomNextInt();

  public String getLocationPath();

  public String getLocationQueryString();

  public LayoutResult nChartlayout(
    NChartLayersArray layers,
    NChartCharacters characters
  );

  public void setPageTitle(String newTitle);

  public void setPageDescription(String newDescription);

  public void uploadFileChunk(
    String contentType,
    JavaScriptObject blob,
    Long startByte,
    Long endByte,
    String url,
    XMLHttpRequest xhr,
    ProgressCallback callback
  );

  public boolean isFileAPISupported();

  public boolean isElementExists(String elementId);

  public String getFileUrl(String fileFieldId);

  public void getFileMd5(Blob blob, MD5Callback callback);

  public void getFilePartMd5(
    JavaScriptObject blob,
    int currentChunk,
    Long chunkSize,
    MD5Callback md5Callback
  );

  public void consoleLog(String message);

  public void consoleError(String message);

  public void consoleError(Throwable t);

  public void processMath(Element element);

  public void loadCss(String url);

  public String[] getSrcPersistentLocalStorageKeys();

  /**
   * initialize the behavior for on pop state
   */
  public void initOnPopStateHandler();

  public String getCurrentURL();

  public String getCurrentHostName();

  String getProtocol(String url);

  String getHost(String url);

  String getHostname(String url);

  String getPort(String url);

  String getPathname(String url);

  void copyToClipboard();

  String sanitizeHtml(String html);

  boolean elementSupportsAttribute(Element el, String attribute);

  String getCdnEndpoint();

  String getAccessTokenCookieUrl();

  void scrollIntoView(Element el);

  void showJiraIssueCollector(
    String issueSummary,
    String issueDescription,
    String jiraIssueCollectorURL,
    String principalId,
    String userDisplayName,
    String userEmailAddress,
    String synapseDataObjectId,
    String componentID,
    String accessRequirementId,
    String issuePriority
  );

  String getVersionsServletUrl();

  String getAppConfigServletUrl();

  long getLastModified(JavaScriptObject blob);

  void setIsInnerProgrammaticHistoryChange();

  String setHash(String hash);

  String getHash();
}
