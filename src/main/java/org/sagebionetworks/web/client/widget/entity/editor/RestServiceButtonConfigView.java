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
	void setUri(String uri);
	String getMethod();
	String getRequestJson();
	void setRequestJson(String requestJson);
	String getButtonText();
	void setButtonText(String buttonText);
	String getButtonType();
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {

	}
}
