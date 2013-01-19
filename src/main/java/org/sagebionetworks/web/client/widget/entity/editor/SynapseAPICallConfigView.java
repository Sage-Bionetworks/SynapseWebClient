package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.web.client.widget.WidgetEditorView;

import com.google.gwt.user.client.ui.IsWidget;

public interface SynapseAPICallConfigView extends IsWidget, WidgetEditorView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	public void setApiUrl(String url);
	public String getApiUrl();
	
	public String getColumnsToDisplay();
	public String getFriendlyColumnNames();
	public String getRendererNames();
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {

	}
}
