package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;

public class ParameterizedPlace extends Place {
	ParameterizedToken parameterizedToken;

	public ParameterizedPlace(String token) {
		parameterizedToken = new ParameterizedToken(token);
	}

	public String getParam(String key) {
		return parameterizedToken.get(key);
	}

	public void removeParam(String key) {
		parameterizedToken.remove(key);
	}

	public void putParam(String key, String value) {
		parameterizedToken.put(key, value);
	}

	public void setParameterizedToken(ParameterizedToken token) {
		parameterizedToken = token;
	}


	public void clearParams() {
		parameterizedToken.clear();
	}

	public String toToken() {
		return parameterizedToken.toString();
	}

	public ParameterizedToken getParameterizedToken() {
		return parameterizedToken;
	}
}
