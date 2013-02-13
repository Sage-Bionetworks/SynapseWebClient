package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.IsWidget;

public interface AttachmentPreviewWidgetView extends IsWidget {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	public void configure(WikiPageKey wikiKey, String fileName);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
	}
}
