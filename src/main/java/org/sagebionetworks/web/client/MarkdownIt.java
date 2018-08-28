package org.sagebionetworks.web.client;

import org.sagebionetworks.web.client.utils.CallbackP;


public interface MarkdownIt {
	public void markdown2Html(String md, String uniqueSuffix, CallbackP<String> callbackHtml);
}
