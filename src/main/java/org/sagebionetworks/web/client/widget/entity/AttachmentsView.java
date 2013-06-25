package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.web.client.events.AttachmentSelectedHandler;
import org.sagebionetworks.web.client.events.WidgetDescriptorUpdatedHandler;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;

public interface AttachmentsView extends IsWidget, SynapseView {

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
		 * add a handler to be informed when an attachment has been edited
		 * @param handler
		 */
		void addAttachmentUpdatedHandler(WidgetDescriptorUpdatedHandler handler); 
		/**
		 * recommended to clear existing handlers before adding your own attachment selected handler
		 */
		void clearHandlers();
		
		void setAttachmentColumnWidth(int width);
	}

	/**
	 * Configures attachments view for this entity
	 * @param entity
	 */
	public void configure(String baseUrl, String entityId, List<AttachmentData> attachments, boolean showEditButton);
	public void setAttachmentColumnWidth(int width);
	public void attachmentDeleted(String tokenId, String deletedName);
}
