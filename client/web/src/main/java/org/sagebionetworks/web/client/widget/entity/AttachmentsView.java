package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.web.client.widget.SynapseWidgetView;

import com.google.gwt.user.client.ui.IsWidget;

public interface AttachmentsView extends IsWidget, SynapseWidgetView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
		
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		
		/**
		 * Delete the list of attachments from the entity
		 * @param attachmentName
		 */
		void deleteAttachment(String tokenId);
		
	}

	/**
	 * Configures attachments view for this entity
	 * @param entity
	 */
	public void configure(String baseUrl, String entityId, List<AttachmentData> attachments);

	public void attachmentDeleted(String tokenId, String deletedName);
}
