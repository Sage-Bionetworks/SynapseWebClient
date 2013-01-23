package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.repo.model.attachment.AttachmentData;

import com.google.gwt.user.client.ui.IsWidget;

public interface ImageWidgetView extends IsWidget {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	public void configure(String entityId, AttachmentData uploadedAttachmentData, String explicitWidth);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
	}
}
