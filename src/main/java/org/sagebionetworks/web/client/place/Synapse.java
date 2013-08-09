package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class Synapse extends Place{
	public static final String VERSION_DELIMITER = "/version/";
	
	public static final String ADMIN_DELIMITER = getDelimiter(Synapse.EntityTab.ADMIN);
	public static final String WIKI_DELIMITER = getDelimiter(Synapse.EntityTab.WIKI);
	public static final String FILES_DELIMITER = getDelimiter(Synapse.EntityTab.FILES);
	
	private String token;
	private String entityId, areaToken;
	private Long versionNumber;
	private Synapse.EntityTab area;
	
	public Synapse(String token) {
		this.token = token;
		area = null;
		areaToken = null;
		
		//the first token is the entityId
		int firstSlash = token.indexOf("/");
		if (firstSlash > -1) {
			entityId = token.substring(0, firstSlash);
			
			//there's more
			String toProcess = token.substring(firstSlash);
			//is there a version?
			if (toProcess.contains(VERSION_DELIMITER)) {
				String[] parts = token.split(VERSION_DELIMITER);
				if(parts.length == 2) {
					entityId = parts[0];
					int slashIndex = parts[1].indexOf("/");
					if (slashIndex > -1) {
						//there's more information after the version
						versionNumber = Long.parseLong(parts[1].substring(0, slashIndex));
						toProcess = parts[1].substring(slashIndex);
					} else {
						versionNumber = Long.parseLong(parts[1]);
						toProcess = "";
					}
				} 
			}
			
			if(toProcess.contains(WIKI_DELIMITER)) {
				String[] parts = toProcess.split(WIKI_DELIMITER);
				area = Synapse.EntityTab.WIKI;
				if (parts.length == 2) {
					areaToken = parts[1];
				}
				return;
			} else if(toProcess.contains(ADMIN_DELIMITER)) {
				area = Synapse.EntityTab.ADMIN;
				return;
			} else if(toProcess.contains(FILES_DELIMITER)) {
				area = Synapse.EntityTab.FILES;
				return;
			}
		} else {
			//no slash
			entityId = token;
		}
	}
	
	public static String getDelimiter(Synapse.EntityTab tab) {
		return "/"+tab+"/";
	}

	public Synapse(String entityId, Long versionNumber, Synapse.EntityTab area, String areaToken) {		
		this.token = entityId;
		if(versionNumber != null) 
			this.token += VERSION_DELIMITER + versionNumber;
		if(area != null) {
			this.token += "/" + area + "/";
			if (areaToken != null) {
				this.token += areaToken;
			}
		}
		
		this.entityId = entityId;
		this.versionNumber = versionNumber;
		this.area = area;
		this.areaToken = areaToken;
	}

	public String toToken() {
		return token;
	}
	
	public String getEntityId() {
		return entityId;
	}

	public Long getVersionNumber() {
		return versionNumber;
	}
	
	public Synapse.EntityTab getEntityArea() {
		return area;
	}
	
	public String getAreaToken() {
		return areaToken;
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

	public static enum EntityTab { WIKI, FILES, ADMIN }

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
		Synapse other = (Synapse) obj;
		if (token == null) {
			if (other.token != null)
				return false;
		} else if (!token.equals(other.token))
			return false;
		return true;
	}

	
}
