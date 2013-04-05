package org.sagebionetworks.web.client;

import java.util.Date;

import org.sagebionetworks.web.client.widget.provenance.nchart.LayoutResult;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartCharacters;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartLayersArray;

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

	public boolean isDirectUploadSupported();
	
	public void uploadFile(String fileFieldId, String url, XMLHttpRequest xhr);
	public String getContentType(String fileFieldId);
}
