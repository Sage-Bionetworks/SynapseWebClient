package org.sagebionetworks.web.client.place;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ParameterizedToken {
	public static final String DEFAULT_TOKEN = "default";
	private Map<String, String> params;

	public ParameterizedToken(String token) {
		params = getParamsMap(token);
	}

	public String toString() {
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
			sb.append(DEFAULT_TOKEN);
		}
		return sb.toString();
	}

	private Map<String, String> getParamsMap(String token) {
		Map<String, String> params = new HashMap<String, String>();
		if (token != null && !DEFAULT_TOKEN.equals(token)) {
			String[] paramList = token.split("&");
			for (String keyValue : paramList) {
				String[] keyValueArray = keyValue.split("=");
				if (keyValueArray.length == 2) {
					params.put(keyValueArray[0], keyValueArray[1]);
				}
			}
		}
		return params;
	}

	public String get(String key) {
		return params.get(key);
	}

	public boolean containsKey(String key) {
		return params.containsKey(key);
	}

	public void remove(String key) {
		params.remove(key);
	}

	public void put(String key, String value) {
		params.put(key, value);
	}

	public void clear() {
		params.clear();
	}

}
