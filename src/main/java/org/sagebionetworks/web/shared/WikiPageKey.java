package org.sagebionetworks.web.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class WikiPageKey implements IsSerializable {

	private String ownerObjectId,ownerObjectType,wikiPageId;

	/**
	 * This should only be used for RPC
	 */
	public WikiPageKey(){
		
	}
	public WikiPageKey(String ownerObjectId, String ownerObjectType, String wikiPageId) {
		super();
		if(ownerObjectId == null) throw new IllegalArgumentException("owner object id cannot be null");
		if(ownerObjectType == null) throw new IllegalArgumentException("ownerObjectType cannot be null");
		//if(wikiPageId == null) throw new IllegalArgumentException("wikiPageId cannot be null");
		//if wiki page id is null, then it's a request for the root wiki associated with the owner object
		this.ownerObjectId = ownerObjectId;
		this.ownerObjectType = ownerObjectType;
		this.wikiPageId = wikiPageId;
	}
	public String getOwnerObjectId() {
		return ownerObjectId;
	}
	public void setOwnerObjectId(String ownerObjectId) {
		this.ownerObjectId = ownerObjectId;
	}
	public String getOwnerObjectType() {
		return ownerObjectType;
	}
	public void setOwnerObjectType(String ownerObjectType) {
		this.ownerObjectType = ownerObjectType;
	}
	public String getWikiPageId() {
		return wikiPageId;
	}
	public void setWikiPageId(String wikiPageId) {
		this.wikiPageId = wikiPageId;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((ownerObjectId == null) ? 0 : ownerObjectId.hashCode());
		result = prime * result
				+ ((ownerObjectType == null) ? 0 : ownerObjectType.hashCode());
		result = prime * result
				+ ((wikiPageId == null) ? 0 : wikiPageId.hashCode());
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
		WikiPageKey other = (WikiPageKey) obj;
		if (ownerObjectId == null) {
			if (other.ownerObjectId != null)
				return false;
		} else if (!ownerObjectId.equals(other.ownerObjectId))
			return false;
		if (ownerObjectType == null) {
			if (other.ownerObjectType != null)
				return false;
		} else if (!ownerObjectType.equals(other.ownerObjectType))
			return false;
		if (wikiPageId == null) {
			if (other.wikiPageId != null)
				return false;
		} else if (!wikiPageId.equals(other.wikiPageId))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "WikiPageKeyWrapper [ownerObjectId=" + ownerObjectId
				+ ", ownerObjectType=" + ownerObjectType + ", wikiPageId="
				+ wikiPageId + "]";
	}
}
