package org.sagebionetworks.web.client.widget.entity;

import java.util.Date;
import com.google.gwt.safehtml.shared.SafeHtml;

public class EntityGroupRecordDisplay {
	private String entityId;
	private SafeHtml name;
	private String nameLinkUrl;
	private String downloadUrl;
	private SafeHtml description;
	private SafeHtml version;
	private Date modifienOn;
	private String createdByPrincipalId;
	private SafeHtml note;

	public EntityGroupRecordDisplay() {

	}

	public EntityGroupRecordDisplay(String entityId, SafeHtml name, String nameLinkUrl, String downloadUrl, SafeHtml description, SafeHtml version, Date modifienOn, String createdByPrincipalId, SafeHtml note) {
		super();
		this.entityId = entityId;
		this.name = name;
		this.nameLinkUrl = nameLinkUrl;
		this.downloadUrl = downloadUrl;
		this.description = description;
		this.version = version;
		this.modifienOn = modifienOn;
		this.createdByPrincipalId = createdByPrincipalId;
		this.note = note;
	}

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public SafeHtml getName() {
		return name;
	}

	public void setName(SafeHtml name) {
		this.name = name;
	}

	public String getNameLinkUrl() {
		return nameLinkUrl;
	}

	public void setNameLinkUrl(String nameLinkUrl) {
		this.nameLinkUrl = nameLinkUrl;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	public SafeHtml getDescription() {
		return description;
	}

	public void setDescription(SafeHtml description) {
		this.description = description;
	}

	public SafeHtml getVersion() {
		return version;
	}

	public void setVersion(SafeHtml version) {
		this.version = version;
	}

	public Date getModifienOn() {
		return modifienOn;
	}

	public void setModifienOn(Date modifienOn) {
		this.modifienOn = modifienOn;
	}

	public String getCreatedByPrincipalId() {
		return createdByPrincipalId;
	}

	public void setCreatedByPrincipalId(String createdByPrincipalId) {
		this.createdByPrincipalId = createdByPrincipalId;
	}

	public SafeHtml getNote() {
		return note;
	}

	public void setNote(SafeHtml note) {
		this.note = note;
	}

}
