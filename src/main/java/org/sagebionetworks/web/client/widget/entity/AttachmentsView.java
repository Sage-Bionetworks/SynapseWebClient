package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.web.client.events.AttachmentSelectedHandler;
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
		void configure(String baseUrl, Entity entity, boolean widgetAttachmentsOnly);
		/**
		 * Delete the list of attachments from the entity
		 * @param attachmentName
		 */
		void deleteAttachment(String tokenId);
		
		/**
		 * Called by the view when an attachment is selected
		 * @param tokenId
		 */
		void attachmentClicked(String attachmentName, String tokenId, String previewTokenId);
		
		/**
		 * add a handler to be informed when an attachment is selected
		 * @param handler
		 */
		void addAttachmentSelectedHandler(AttachmentSelectedHandler handler);
		
		/**
		 * recommended to clear existing handlers before adding your own attachment selected handler
		 */
		void clearHandlers();
		
		void configureWidgetAttachment(String attachmentName);
	}

	/**
	 * Configures attachments view for this entity
	 * @param entity
	 */
	public void configure(String baseUrl, String entityId, List<AttachmentData> attachments);

	public void attachmentDeleted(String tokenId, String deletedName);
}
