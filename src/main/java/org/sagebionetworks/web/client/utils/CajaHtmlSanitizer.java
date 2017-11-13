package org.sagebionetworks.web.client.utils;

public class CajaHtmlSanitizer {
	public String sanitize(String html) {
		return _sanitize(html);
	}

	private static native String _sanitize(String html) /*-{
		// client-side html sanitize from google caja project (for preview).  
		// urlTransformer: Takes a URL and either modifies it or strips it by returning null
		// classIdTransformer: Takes in an element ID or class name and either modifies it or strips it by returning null
		return $wnd.html_sanitize(html, function() {}, function() {});
	}-*/;

}
