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
	
	public void loadTableSorters();
	
	public String convertDateToSmallString(Date toFormat);
	/**
	 * Return a friendly relative date string.  Like "4 hours ago"
	 * @param toFormat
	 * @return
	 */
	public String getRelativeTime(Date toFormat);
	/**
	 * Return a friendly calendar date string.  Like "Yesterday at 3:32 PM"
	 * @param toFormat
	 * @return
	 */
	public String getCalendarTime(Date toFormat);
	/**
	 * Return a friendly calendar date string.  Like "January 20, 2016 3:47 PM"
	 * @param toFormat
	 * @return
	 */
	public String getLongFriendlyDate(Date toFormat);
	
	
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
	public void getFilePartMd5(String fileFieldId, int currentChunk, Long chunkSize, int fileIndex, MD5Callback md5Callback);
	public double getFileSize(String fileFieldId, int index);
	String[] getMultipleUploadFileNames(String fileFieldId);
	public void consoleLog(String message);
	public void consoleError(String message);
	public void uploadUrlToGenomeSpace(String url);	
	public void uploadUrlToGenomeSpace(String url, String filename);
	
	public void processWithMathJax(Element element);	

	public void loadCss(String url, Callback<Void, Exception> callback);

	/**
	 * initialize the behavior for on pop state
	 */
	public void initOnPopStateHandler();
	
	public void showTwitterFeed(String dataWidgetId, String elementId, String linkColor, String borderColor, int tweetCount);
	
	public String getCurrentURL();
	public String getCurrentHostName();

	boolean copyToClipboard(String text);
}
