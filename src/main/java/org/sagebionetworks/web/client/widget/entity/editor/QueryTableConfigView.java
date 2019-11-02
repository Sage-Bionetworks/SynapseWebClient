package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.List;
import org.sagebionetworks.web.client.widget.WidgetEditorView;
import com.google.gwt.user.client.ui.IsWidget;

public interface QueryTableConfigView extends IsWidget, WidgetEditorView {

	/**
	 * Set the presenter.
	 * 
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	public String getQueryString();

	public Boolean isPaging();

	public List<APITableColumnConfig> getConfigs();

	public void configure(APITableConfig tableConfig);

	public void setQueryPlaceholder(String placeHolder);

	public void setConfigs(List<APITableColumnConfig> newColumnConfigs);

	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void autoAddColumns();
	}
}
