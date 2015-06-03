package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.web.client.widget.WidgetEditorView;

import com.google.gwt.user.client.ui.IsWidget;

public interface RestServiceButtonConfigView extends IsWidget, WidgetEditorView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	
	String getURI();
	String getMethod();
	String getRequestJson();
	String getButtonText();
	String getButtonType();
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {

	}
}
