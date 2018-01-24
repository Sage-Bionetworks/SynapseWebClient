package org.sagebionetworks.web.client;

import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.web.client.callback.MD5Callback;
import org.sagebionetworks.web.client.widget.provenance.nchart.LayoutResult;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartCharacters;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartLayersArray;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.xhr.client.XMLHttpRequest;

public interface SynapseJSNIUtils {

	public void recordPageVisit(String token);

	public String getCurrentHistoryToken();
	
	public void highlightCodeBlocks();
	
	public void loadTableSorters();
	
	public String getBaseFileHandleUrl();
	
	public String getBaseProfileAttachmentUrl();
	
	public String getFileHandleAssociationUrl(String objectId, FileHandleAssociateType objectType, String fileHandleId);

	public int randomNextInt();
	
	public String getLocationPath();
	
	public String getLocationQueryString();
	
	public LayoutResult nChartlayout(NChartLayersArray layers, NChartCharacters characters);
	
	public void setPageTitle(String newTitle);
	
	public void setPageDescription(String newDescription);

	public JavaScriptObject getFileList(String fileFieldId);
	public JavaScriptObject getFileBlob(int index, JavaScriptObject fileList);
	public void uploadFileChunk(String contentType, JavaScriptObject blob, Long startByte, Long endByte, String url, XMLHttpRequest xhr, ProgressCallback callback);
	
	public String getContentType(JavaScriptObject fileList, int index);
	public boolean isFileAPISupported();
	public boolean isElementExists(String elementId);
	public String getFileUrl(String fileFieldId);
	public void getFileMd5(JavaScriptObject blob, MD5Callback callback);
	public void getFilePartMd5(JavaScriptObject blob, int currentChunk, Long chunkSize, MD5Callback md5Callback);
	public double getFileSize(JavaScriptObject blob);
	String[] getMultipleUploadFileNames(JavaScriptObject fileList);
	public void consoleLog(String message);
	public void consoleError(String message);
	
	public void processWithMathJax(Element element);	

	public void loadCss(String url);

	/**
	 * initialize the behavior for on pop state
	 */
	public void initOnPopStateHandler();
	
	public void showTwitterFeed(String dataWidgetId, String elementId, String linkColor, String borderColor, int tweetCount);
	
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

	Element getElementById(String elementId);
	String getCdnEndpoint();
}
