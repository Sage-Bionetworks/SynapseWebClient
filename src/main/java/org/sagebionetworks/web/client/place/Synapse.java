package org.sagebionetworks.web.client.place;

import java.util.LinkedList;

import org.sagebionetworks.web.client.StringUtils;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class Synapse extends Place {
	public static final String DOT_REGEX = "\\.";
	public static final String DELIMITER = "/";
	public static final String SYNAPSE_ENTITY_PREFIX = "#!Synapse:";
	public static final String VERSION = "version";
	
	private String synapsePlaceToken;
	private String entityId, areaToken;
	private Long versionNumber;
	private Synapse.EntityArea area;
	
	public Synapse(String token) {
		this.synapsePlaceToken = token;
		area = null;
		areaToken = null;
		String[] tokensArray = token.split(DELIMITER);
		LinkedList<String> tokens = new LinkedList<String>();
		for (int i = 0; i < tokensArray.length; i++) {
			tokens.add(tokensArray[i]);
		}
		
		//first token should be the entity id
		entityId = tokens.poll();
		
		//look for dot version syntax in this token
		String[] entityIdTokens = entityId.split(DOT_REGEX);
		if (entityIdTokens.length > 1) {
			entityId = entityIdTokens[0];
			versionNumber = Long.parseLong(entityIdTokens[1]);
		}
		
		//set the next token
		String nextToken = tokens.poll();
		
		if (nextToken != null && VERSION.equals(nextToken.toLowerCase())) {
			nextToken = null;
			if (!tokens.isEmpty()) {
				versionNumber = Long.parseLong(tokens.removeFirst());
				nextToken = tokens.poll();
			}
		}
		
		if (nextToken != null) {
			area = EntityArea.valueOf(nextToken.toUpperCase());
		}
			
		//remaining tokens are recognized is the area token
		if (tokens.size() > 0) {
			areaToken = "";
		}
		while (tokens.size() > 0) {
			areaToken += tokens.poll();
			if (tokens.size() > 0) {
				areaToken += "/";	
			}
		}
	}
	
	public static String getDelimiter(Synapse.EntityArea tab) {
		return "/"+tab.toString().toLowerCase()+"/";
	}

	public Synapse(String entityId, Long versionNumber, Synapse.EntityArea area, String areaToken) {				
		this.entityId = entityId;
		this.versionNumber = versionNumber;
		this.area = area;
		this.areaToken = areaToken;
		calculateToken(entityId, versionNumber, area, areaToken);
	}

	private void calculateToken(String entityId, Long versionNumber,
			Synapse.EntityArea area, String areaToken) {
		this.synapsePlaceToken = entityId;
		if(versionNumber != null)
			this.synapsePlaceToken += "." + versionNumber;
		if(area != null) {
			this.synapsePlaceToken += getDelimiter(area);
			if (areaToken != null) {
				this.synapsePlaceToken += areaToken;
			}
		}
	}

	public String toToken() {
		return synapsePlaceToken;
	}
	
	public String getEntityId() {
		return entityId;
	}

	public Long getVersionNumber() {
		return versionNumber;
	}
	
	public Synapse.EntityArea getArea() {
		return area;
	}
	
	public String getAreaToken() {
		return areaToken;
	}
	
	public void setArea(Synapse.EntityArea area) {
		this.area = area;
		calculateToken(entityId, versionNumber, area, areaToken);
	}

	public void setAreaToken(String areaToken) {
		this.areaToken = areaToken;
		calculateToken(entityId, versionNumber, area, areaToken);
	}

	@Prefix("!Synapse")
	public static class Tokenizer implements PlaceTokenizer<Synapse> {
        @Override
        public String getToken(Synapse place) {
            return place.toToken();
        }

        @Override
        public Synapse getPlace(String token) {
            return new Synapse(token);
        }
    }

	public static enum EntityArea { WIKI, FILES, TABLES, CHALLENGE, DISCUSSION, DOCKER }
	public static enum ProfileArea { PROFILE, PROJECTS, CHALLENGES, TEAMS, DOWNLOADS, SETTINGS }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((synapsePlaceToken == null) ? 0 : synapsePlaceToken.hashCode());
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
		Synapse other = (Synapse) obj;
		if (synapsePlaceToken == null) {
			if (other.synapsePlaceToken != null)
				return false;
		} else if (!synapsePlaceToken.equals(other.synapsePlaceToken))
			return false;
		return true;
	}
	
	/**
	 * Given a string where the version number is delimited with a dot (.) convert to a valid token.
	 * @param dotNotation
	 * @return
	 */
	public static String getHrefForDotVersion(String dotNotation){
		dotNotation = StringUtils.emptyAsNull(dotNotation);
		if(dotNotation == null){
			return null;
		}
		return SYNAPSE_ENTITY_PREFIX+dotNotation.toLowerCase();
	}
}
