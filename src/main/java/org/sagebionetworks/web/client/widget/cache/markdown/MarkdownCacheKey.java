package org.sagebionetworks.web.client.widget.cache.markdown;

import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

public class MarkdownCacheKey {
	String md, hostPrefix;
	boolean isInTestWebsite;
	
	public MarkdownCacheKey() {
	}
	
	public void init(String md, String hostPrefix,
			boolean isInTestWebsite) {
		this.md = md;
		this.hostPrefix = hostPrefix;
		this.isInTestWebsite = isInTestWebsite;
	}
	
	public String toJSON() {
		JSONObject json = new JSONObject();
		json.put("md", new JSONString(URL.encode(md)));
		json.put("hostPrefix", new JSONString(URL.encode(hostPrefix)));
		json.put("isInTestWebsite", new JSONString(Boolean.toString(isInTestWebsite)));
		return json.toString();
	}
}
