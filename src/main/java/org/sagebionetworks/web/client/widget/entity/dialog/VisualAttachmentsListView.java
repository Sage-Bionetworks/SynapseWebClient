package org.sagebionetworks.web.client.widget.entity.dialog;

import java.util.List;

import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.web.client.widget.SynapseWidgetView;

import com.google.gwt.user.client.ui.IsWidget;

public interface VisualAttachmentsListView extends IsWidget, SynapseWidgetView {

	public String getSelectedAttachmentTokenId();
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		public AttachmentData getSelectedAttachment();
	}

	/**
	 * Configures attachments view for this entity
	 * @param entity
	 */
	public void configure(String baseUrl, String entityId, List<AttachmentData> attachments);
}
