package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.web.client.widget.WidgetEditorView;

import com.google.gwt.user.client.ui.IsWidget;

public interface APITableConfigView extends IsWidget, WidgetEditorView {

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
	
	public String getTableWidth();
	public Boolean isPaging();
	public String getPageSize();
	public Boolean isShowRowNumbers();
	public String getRowNumberColumnName();
	public String getJsonResultsKeyName();
	public String getCssStyle();
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {

	}
}
