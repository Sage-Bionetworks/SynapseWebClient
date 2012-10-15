package org.sagebionetworks.web.client.widget.entity.dialog;

import java.util.Iterator;
import java.util.List;

import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class VisualAttachmentsList implements VisualAttachmentsListView.Presenter,
		SynapseWidgetPresenter {

	private VisualAttachmentsListView view;
	private List<AttachmentData> attachments;
	
	@Inject
	public VisualAttachmentsList(VisualAttachmentsListView view, SynapseClientAsync synapseClient){
		this.view = view;
	}

	public void configure(String baseUrl, String entityId, List<AttachmentData> attachments) {
		view.configure(baseUrl, entityId, attachments);
		this.attachments = attachments;
	}

	@Override
	public AttachmentData getSelectedAttachment() {
		String tokenId = view.getSelectedAttachmentTokenId();
		for (Iterator iterator = attachments.iterator(); iterator.hasNext();) {
			AttachmentData data = (AttachmentData) iterator.next();
			if (data.getTokenId().equals(tokenId))
				return data;
		}
		return null;
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
