package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.web.client.widget.WidgetEditorView;

import com.google.gwt.user.client.ui.IsWidget;
public interface ExternalImageConfigView extends IsWidget, WidgetEditorView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	public String getImageUrl();
	public String getAltText();
	public void setImageUrl(String url);
	/**
	 * Presenter interface
	 */
	public interface Presenter {
	}
}
