package org.sagebionetworks.web.server.servlet;

import java.io.Serializable;

public class MarkdownCacheRequest implements Serializable {
	private static final long serialVersionUID = -1906312264088602086L;
	private String markdown, clientHostString;
	private Boolean isPreview;
	public MarkdownCacheRequest(String markdown, String clientHostString,
			Boolean isPreview) {
		super();
		this.markdown = markdown;
		this.clientHostString = clientHostString;
		this.isPreview = isPreview;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((clientHostString == null) ? 0 : clientHostString.hashCode());
		result = prime * result
				+ ((isPreview == null) ? 0 : isPreview.hashCode());
		result = prime * result
				+ ((markdown == null) ? 0 : markdown.hashCode());
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
		if (clientHostString == null) {
			if (other.clientHostString != null)
				return false;
		} else if (!clientHostString.equals(other.clientHostString))
			return false;
		if (isPreview == null) {
			if (other.isPreview != null)
				return false;
		} else if (!isPreview.equals(other.isPreview))
			return false;
		if (markdown == null) {
			if (other.markdown != null)
				return false;
		} else if (!markdown.equals(other.markdown))
			return false;
		return true;
	}
	public String getMarkdown() {
		return markdown;
	}
	public void setMarkdown(String markdown) {
		this.markdown = markdown;
	}
	public String getClientHostString() {
		return clientHostString;
	}
	public void setClientHostString(String clientHostString) {
		this.clientHostString = clientHostString;
	}
	public Boolean getIsPreview() {
		return isPreview;
	}
	public void setIsPreview(Boolean isPreview) {
		this.isPreview = isPreview;
	}
	
	
}
