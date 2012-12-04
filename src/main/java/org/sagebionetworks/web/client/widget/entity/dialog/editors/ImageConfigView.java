package org.sagebionetworks.web.client.widget.entity.dialog.editors;

import org.sagebionetworks.web.client.widget.WidgetDescriptorView;

import com.google.gwt.user.client.ui.IsWidget;

public interface ImageConfigView extends IsWidget, WidgetDescriptorView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	/**
	 * True if user just wants to insert a reference to an image from the web
	 * @return
	 */
	public boolean isExternal();
	
	public String getImageUrl();
	
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {

	}
}
