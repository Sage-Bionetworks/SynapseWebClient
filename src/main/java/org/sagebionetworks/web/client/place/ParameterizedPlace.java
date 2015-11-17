package org.sagebionetworks.web.client.place;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.gwt.place.shared.Place;

public class ParameterizedPlace extends Place {
	private Map<String, String> params;
	protected String token;
	public ParameterizedPlace(String token) {
		params = new HashMap<String, String>();
		String[] paramList = token.split("&");
		for (String keyValue : paramList) {
			String[] keyValueArray = keyValue.split("=");
			if (keyValueArray.length==2) {
				params.put(keyValueArray[0], keyValueArray[1]);	
			}
		}
		updateToken();
	}
	
	public ParameterizedPlace(Map<String, String> params) {
		this.params = params;
		updateToken();
	}
	
	public String getParam(String key) {
		return params.get(key);
	}
	
	public void removeParam(String key) {
		params.remove(key);
		updateToken();
	}
	
	public void putParam(String key, String value) {
		params.put(key, value);
		updateToken();
	}
	
	private void updateToken() {
		StringBuilder sb = new StringBuilder();
		for (Iterator<String> iterator = params.keySet().iterator(); iterator.hasNext();) {
			String key = iterator.next();
			sb.append(key);
			sb.append("=");
			sb.append(params.get(key));
			if (iterator.hasNext()) {
				sb.append("&");	
			}
		}
		if (sb.length() == 0) {
			sb.append("0");
		}
		token = sb.toString();
	}
	
	public String toToken() {
		return token;
	}
}
