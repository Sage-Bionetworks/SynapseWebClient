package org.sagebionetworks.web.client;

import java.util.Date;

import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.web.client.callback.MD5Callback;
import org.sagebionetworks.web.client.widget.provenance.nchart.LayoutResult;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartCharacters;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartLayersArray;

import com.google.gwt.core.client.Callback;
import com.google.gwt.dom.client.Element;
import com.google.gwt.xhr.client.XMLHttpRequest;

public interface SynapseJSNIUtils {

	public void recordPageVisit(String token);

	public String getCurrentHistoryToken();

	public void bindBootstrapTooltip(String id);

	public void hideBootstrapTooltip(String id);
	
	public void bindBootstrapPopover(String id);
	
	public void highlightCodeBlocks();
	
	public void tablesorter(String id);
	
	public String convertDateToSmallString(Date toFormat);
	
	public String getBaseFileHandleUrl();
	
	public String getBaseProfileAttachmentUrl();
	
	public String getFileHandleAssociationUrl(String objectId, FileHandleAssociateType objectType, String fileHandleId);

	public int randomNextInt();
	
	public String getLocationPath();
	
	public String getLocationQueryString();
	
	public LayoutResult nChartlayout(NChartLayersArray layers, NChartCharacters characters);
	
	public void setPageTitle(String newTitle);
	
	public void setPageDescription(String newDescription);

	public void uploadFileChunk(String contentType, int index, String fileFieldId, Long startByte, Long endByte, String url, XMLHttpRequest xhr, ProgressCallback callback);
	
	public String getContentType(String fileFieldId, int index);
	public boolean isFileAPISupported();
	public boolean isElementExists(String elementId);
	public String getFileUrl(String fileFieldId);
	public void getFileMd5(String fileFieldId, int index, MD5Callback callback);
	public void getFilePartMd5(String fileFieldId, Long start, Long end, int fileIndex, MD5Callback md5Callback);
	public double getFileSize(String fileFieldId, int index);
	String[] getMultipleUploadFileNames(String fileFieldId);
	public void consoleLog(String message);
	public void consoleError(String message);
	public void uploadUrlToGenomeSpace(String url);	
	public void uploadUrlToGenomeSpace(String url, String filename);
	
	public void processWithMathJax(Element element);	

	public void loadCss(String url, Callback<Void, Exception> callback);
	
	/**
	 * Replace the current history state with a new token using history.replaceState().
	 * @see https://developer.mozilla.org/en-US/docs/Web/Guide/API/DOM/Manipulating_the_browser_history
	 * @param token
	 */
	public void replaceHistoryState(String token);

	/**
	 * push a new state to the current history state with a token using history.pushState().
	 * @param token
	 */
	public void pushHistoryState(String token);

	/**
	 * initialize the behavior for on pop state
	 */
	public void initOnPopStateHandler();
	
	public void showTwitterFeed(String dataWidgetId, String elementId, String linkColor, String borderColor, int height);
	
	public String getCurrentURL();
	public String getCurrentHostName();
}
