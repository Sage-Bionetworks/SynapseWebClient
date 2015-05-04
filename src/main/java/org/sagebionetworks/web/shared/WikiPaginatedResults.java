package org.sagebionetworks.web.shared;

import java.io.Serializable;

import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.wiki.WikiHeader;

@SuppressWarnings("serial")
public class WikiPaginatedResults implements Serializable {
	private PaginatedResults<WikiHeader> pageHeaders;
	private String ownerId;
	private ObjectType ownerType;
	
	/**
	 * Default constructor is required
	 */
	public WikiPaginatedResults() {
	}

	public WikiPaginatedResults(PaginatedResults<WikiHeader> pageHeaders,
			String ownerId, ObjectType ownerType) {
		super();
		this.pageHeaders = pageHeaders;
		this.ownerId = ownerId;
		this.ownerType = ownerType;
	}

	public PaginatedResults<WikiHeader> getPageHeaders() {
		return pageHeaders;
	}

	public void setPageHeaders(PaginatedResults<WikiHeader> pageHeaders) {
		this.pageHeaders = pageHeaders;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	public ObjectType getOwnerType() {
		return ownerType;
	}

	public void setOwnerType(ObjectType ownerType) {
		this.ownerType = ownerType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ownerId == null) ? 0 : ownerId.hashCode());
		result = prime * result
				+ ((ownerType == null) ? 0 : ownerType.hashCode());
		result = prime * result
				+ ((pageHeaders == null) ? 0 : pageHeaders.hashCode());
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
		WikiPaginatedResults other = (WikiPaginatedResults) obj;
		if (ownerId == null) {
			if (other.ownerId != null)
				return false;
		} else if (!ownerId.equals(other.ownerId))
			return false;
		if (ownerType != other.ownerType)
			return false;
		if (pageHeaders == null) {
			if (other.pageHeaders != null)
				return false;
		} else if (!pageHeaders.equals(other.pageHeaders))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "WikiPaginatedResults [pageHeaders=" + pageHeaders
				+ ", ownerId=" + ownerId + ", ownerType=" + ownerType + "]";
	}
}
