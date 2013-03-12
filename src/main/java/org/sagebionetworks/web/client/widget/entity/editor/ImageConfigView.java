package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.web.client.widget.WidgetEditorView;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.IsWidget;

public interface ImageConfigView extends IsWidget, WidgetEditorView {

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
	public String getAltText();
	public void setImageUrl(String url);
	public String getUploadedFileHandleName();
	public void setExternalVisible(boolean visible);
	
	public void configure(WikiPageKey wikiKey);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
	}
}
