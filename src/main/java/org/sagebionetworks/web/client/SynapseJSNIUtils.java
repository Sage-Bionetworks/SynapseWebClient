package org.sagebionetworks.web.client;

import java.util.Date;

import org.sagebionetworks.web.client.callback.MD5Callback;
import org.sagebionetworks.web.client.widget.entity.download.Uploader;
import org.sagebionetworks.web.client.widget.provenance.nchart.LayoutResult;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartCharacters;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartLayersArray;

import com.google.gwt.core.client.Callback;
import com.google.gwt.user.client.Element;
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

	public int randomNextInt();
	
	public String getLocationPath();
	
	public String getLocationQueryString();
	
	public LayoutResult nChartlayout(NChartLayersArray layers, NChartCharacters characters);
	
	public void setPageTitle(String newTitle);
	
	public void setPageDescription(String newDescription);

	public void uploadFileChunk(String contentType, int index, String fileFieldId, Long startByte, Long endByte, String url, XMLHttpRequest xhr, ProgressCallback callback);
	
	public String getContentType(String fileFieldId, int index);
	
	public String getFileUrl(String fileFieldId);
	public void getFileMd5(String fileFieldId, int index, MD5Callback callback);
	public double getFileSize(String fileFieldId, int index);
	String[] getMultipleUploadFileNames(String fileFieldId);
	public void consoleLog(String message);
	public void consoleError(String message);
	public void uploadUrlToGenomeSpace(String url);	
	public void uploadUrlToGenomeSpace(String url, String filename);
	
	public void processWithMathJax(Element element);	

	public void loadCss(String url, Callback<Void, Exception> callback);
}
