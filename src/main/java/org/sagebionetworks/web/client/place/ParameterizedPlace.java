package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;

public class ParameterizedPlace extends Place {
	protected String token;
	ParameterizedToken parameterizedToken;
	public ParameterizedPlace(String token) {
		parameterizedToken = new ParameterizedToken(token);
		updateToken();
	}
	
	public String getParam(String key) {
		return parameterizedToken.get(key);
	}
	
	public void removeParam(String key) {
		parameterizedToken.remove(key);
		updateToken();
	}
	
	public void putParam(String key, String value) {
		parameterizedToken.put(key, value);
		updateToken();
	}
	
	private void updateToken() {
		token = parameterizedToken.toString();
	}
	
	
	public String toToken() {
		return token;
	}

	public ParameterizedToken getParameterizedToken(){
		return parameterizedToken;
	}
}
