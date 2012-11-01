package org.sagebionetworks.web.client.events;

import com.google.gwt.event.shared.GwtEvent;

public class AttachmentSelectedEvent extends GwtEvent<AttachmentSelectedHandler> {

	private static final Type TYPE = new Type<AttachmentSelectedHandler>();
	private String name, tokenId, previewTokenId;
	public AttachmentSelectedEvent(String attachmentName, String tokenId, String previewTokenId) {
		this.name = attachmentName;
		this.tokenId = tokenId;
		this.previewTokenId = previewTokenId;
	}
	
	public static Type getType() {
		return TYPE;
	}
	
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<AttachmentSelectedHandler> getAssociatedType() {
		return TYPE;
	}
	
	public String getTokenId() {
		return tokenId;
	}
	
	public String getName() {
		return name;
	}
	
	public String getPreviewTokenId() {
		return previewTokenId;
	}

	@Override
	protected void dispatch(AttachmentSelectedHandler handler) {
		handler.onAttachmentSelected(this);
	}

}
