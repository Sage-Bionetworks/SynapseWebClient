package org.sagebionetworks.web.server.servlet;

import java.io.Serializable;

import org.sagebionetworks.repo.model.dao.WikiPageKey;

public class MarkdownCacheRequest implements Serializable {
	private static final long serialVersionUID = -4204768923738949620L;
	private WikiPageKey wikiPageKey;
	private String etag;
	private Long version;
	public MarkdownCacheRequest(WikiPageKey wikiPageKey, String etag, Long version) {
		super();
		this.wikiPageKey = wikiPageKey;
		this.etag = etag;
		this.version = version;
		
	}
	public WikiPageKey getWikiPageKey() {
		return wikiPageKey;
	}
	public void setWikiPageKey(WikiPageKey wikiPageKey) {
		this.wikiPageKey = wikiPageKey;
	}
	
	
	public String getEtag() {
		return etag;
	}
	public void setEtag(String etag) {
		this.etag = etag;
	}
	public Long getVersion() {
		return version;
	}
	public void setVersion(Long version) {
		this.version = version;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((etag == null) ? 0 : etag.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		result = prime * result
				+ ((wikiPageKey == null) ? 0 : wikiPageKey.hashCode());
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
		MarkdownCacheRequest other = (MarkdownCacheRequest) obj;
		if (etag == null) {
			if (other.etag != null)
				return false;
		} else if (!etag.equals(other.etag))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		if (wikiPageKey == null) {
			if (other.wikiPageKey != null)
				return false;
		} else if (!wikiPageKey.equals(other.wikiPageKey))
			return false;
		return true;
	}
	
	
	
	

}
