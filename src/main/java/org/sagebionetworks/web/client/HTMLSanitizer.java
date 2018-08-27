package org.sagebionetworks.web.client;

import org.sagebionetworks.web.client.utils.CallbackP;

public interface HTMLSanitizer {
	void sanitizeHtml(String html, CallbackP<String> sanitizedHtmlCallback);
}
