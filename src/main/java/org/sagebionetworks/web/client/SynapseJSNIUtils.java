package org.sagebionetworks.web.client;

import java.util.Date;

public interface SynapseJSNIUtils {

	public void recordPageVisit(String token);

	public String getCurrentHistoryToken();

	public void bindBootstrapTooltip(String id);

	public void hideBootstrapTooltip(String id);

	public void bindBootstrapPopover(String id);
	
	public void highlightCodeBlocks();
	
	public String convertDateToSmallString(Date toFormat);
	
	public String getBaseProfileAttachmentUrl();
}
