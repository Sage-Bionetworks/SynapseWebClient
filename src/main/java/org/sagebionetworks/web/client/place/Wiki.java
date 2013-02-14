package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class Wiki extends Place{
	public static final String DELIMITER = "/"; 
	
	private String token;
	private String ownerId, ownerType, wikiId;
	
	public Wiki(String token) {
		this.token = token;
		if(token.contains(DELIMITER)) {
			String[] parts = token.split(DELIMITER);
			if(parts.length == 3) {				
				ownerId = parts[0];
				ownerType = parts[1];
				wikiId = parts[2];
			} 		
		} 
	}

	public Wiki(String ownerId, String ownerType, String wikiId) {		
		this.token = ownerId + DELIMITER + ownerType + DELIMITER + wikiId;
		this.ownerId = ownerId;
		this.ownerType = ownerType;
		this.wikiId = wikiId;
	}

	public String toToken() {
		return token;
	}
	
	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	public String getOwnerType() {
		return ownerType;
	}

	public void setOwnerType(String ownerType) {
		this.ownerType = ownerType;
	}

	public String getWikiId() {
		return wikiId;
	}

	public void setWikiId(String wikiId) {
		this.wikiId = wikiId;
	}

	public static class Tokenizer implements PlaceTokenizer<Wiki> {
        @Override
        public String getToken(Wiki place) {
            return place.toToken();
        }

        @Override
        public Wiki getPlace(String token) {
            return new Wiki(token);
        }
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((token == null) ? 0 : token.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Wiki other = (Wiki) obj;
		if (token == null) {
			if (other.token != null)
				return false;
		} else if (!token.equals(other.token))
			return false;
		return true;
	}

	
}
