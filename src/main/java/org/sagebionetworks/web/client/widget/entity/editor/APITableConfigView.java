package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.List;

import org.sagebionetworks.web.client.widget.WidgetEditorView;

import com.google.gwt.user.client.ui.IsWidget;

public interface APITableConfigView extends IsWidget, WidgetEditorView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	public String getApiUrl();
	
	public String getTableWidth();
	public Boolean isPaging();
	public String getPageSize();
	public Boolean isShowRowNumbers();
	public String getRowNumberColumnName();
	public String getJsonResultsKeyName();
	public String getCssStyle();
	public List<APITableColumnConfig> getConfigs();
	public void configure(APITableConfig tableConfig);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {

	}

	
}
