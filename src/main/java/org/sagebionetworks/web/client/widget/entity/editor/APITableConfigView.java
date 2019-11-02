package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.List;
import org.sagebionetworks.web.client.widget.WidgetEditorView;
import com.google.gwt.user.client.ui.IsWidget;

public interface APITableConfigView extends IsWidget, WidgetEditorView {
	public String getApiUrl();

	public Boolean isPaging();

	public Boolean isQueryTableResults();

	public Boolean isShowIfLoggedInOnly();

	public String getPageSize();

	public String getJsonResultsKeyName();

	public String getCssStyle();

	public List<APITableColumnConfig> getConfigs();

	public void configure(APITableConfig tableConfig);
}
