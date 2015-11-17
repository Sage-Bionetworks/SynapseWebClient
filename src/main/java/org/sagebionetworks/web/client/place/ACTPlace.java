package org.sagebionetworks.web.client.place;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class ACTPlace extends Place{
	
	private String token;
	private Map<String, String> params;
	public ACTPlace(String token) {
		this.token = token;
		params = new HashMap<String, String>();
		String[] paramList = token.split("&");
		for (String keyValue : paramList) {
			String[] keyValueArray = keyValue.split("=");
			if (keyValueArray.length==2) {
				params.put(keyValueArray[0], keyValueArray[1]);	
			}
		}
	}
	
	public ACTPlace(Map<String, String> params) {
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
		token = sb.toString();
	}
	
	public String toToken() {
		return token;
	}
	
	@Prefix("!ACT")
	public static class Tokenizer implements PlaceTokenizer<ACTPlace> {
        @Override
        public String getToken(ACTPlace place) {
            return place.toToken();
        }

        @Override
        public ACTPlace getPlace(String token) {
            return new ACTPlace(token);
        }
    }
}
