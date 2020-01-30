package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

/**
 * This place supports either a token with the full wiki key definition. For example:
 * !StandaloneWiki:syn1768504/ENTITY/56098
 * 
 * Or one defined via alias in the portal.properties. For example: !StandaloneWiki:Collaborate
 * 
 * @author jayhodgson
 *
 */
public class StandaloneWiki extends Place {
	public static final String DELIMITER = "/";

	private String token;
	private String ownerId, ownerType, wikiId;

	public StandaloneWiki(String token) {
		this.token = token;
		if (token.contains(DELIMITER)) {
			String[] parts = token.split(DELIMITER);
			if (parts.length >= 2) {
				ownerId = parts[0];
				ownerType = parts[1];
				if (parts.length == 3)
					wikiId = parts[2];
			}
		}
	}

	public StandaloneWiki(String ownerId, String ownerType, String wikiId) {
		String wikiIdToken = wikiId != null ? DELIMITER + wikiId : "";
		this.token = ownerId + DELIMITER + ownerType + wikiIdToken;

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

	@Prefix("!StandaloneWiki")
	public static class Tokenizer implements PlaceTokenizer<StandaloneWiki> {
		@Override
		public String getToken(StandaloneWiki place) {
			return place.toToken();
		}

		@Override
		public StandaloneWiki getPlace(String token) {
			return new StandaloneWiki(token);
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
		StandaloneWiki other = (StandaloneWiki) obj;
		if (token == null) {
			if (other.token != null)
				return false;
		} else if (!token.equals(other.token))
			return false;
		return true;
	}


}
