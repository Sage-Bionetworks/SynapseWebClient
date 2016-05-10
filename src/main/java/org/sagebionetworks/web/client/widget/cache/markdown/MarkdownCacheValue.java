package org.sagebionetworks.web.client.widget.cache.markdown;

import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;

public class MarkdownCacheValue {
	String uniqueSuffix, html;
	
	public MarkdownCacheValue() {
	}
	
	/**
	 * Initialize from raw values.
	 * @param uniqueSuffix
	 * @param html
	 */
	public void init(String uniqueSuffix, String html) {
		this.uniqueSuffix = uniqueSuffix;
		this.html = html;
	}

	/**
	 * Initialize from json (with encoded values) that has been saved into the cache.
	 * @param cacheValue
	 */
	public void init(String cacheValue) {
		if (cacheValue != null) {
			JSONObject jsonObject = (JSONObject)JSONParser.parseStrict(cacheValue);
			JSONString uniqueSuffix = (JSONString)jsonObject.get("uniqueSuffix");
			JSONString html = (JSONString)jsonObject.get("html");
			init(URL.decode(uniqueSuffix.stringValue()), URL.decode(html.stringValue()));
		}
	}

	public String toJSON() {
		JSONObject json = new JSONObject();
		json.put("uniqueSuffix", new JSONString(URL.encode(uniqueSuffix)));
		json.put("html", new JSONString(URL.encode(html)));
		return json.toString();
	}
	public String getHtml() {
		return html;
	}
	public String getUniqueSuffix() {
		return uniqueSuffix;
	}
}
