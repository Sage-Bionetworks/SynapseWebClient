package org.sagebionetworks.web.client.widget.entity.renderer;

import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.IsWidget;

public interface ButtonLinkWidgetView extends IsWidget {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	public void configure(WikiPageKey wikiKey, String buttonText, String url, boolean isHighlight, boolean openInNewWindow);
	public void setWidth(String width);
	public void setSize(ButtonSize size);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
	}
	
}
