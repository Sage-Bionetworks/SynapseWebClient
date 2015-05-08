package org.sagebionetworks.web.client.widget.table.v2.results;

import java.util.Map;

import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.controller.ServiceErrorHandler;
import org.sagebionetworks.web.client.widget.table.v2.TableEntityWidget;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TableQueryResultWikiWidget implements WidgetRendererPresenter{

	TableQueryResultWidget tableQueryResultWidget;
	SynapseJSNIUtils synapseJsniUtils;
	TableQueryResultWikiWidgetView view;
	
	@Inject
	public TableQueryResultWikiWidget(TableQueryResultWikiWidgetView view, 
			TableQueryResultWidget tableQueryResultWidget, 
			SynapseJSNIUtils synapseJsniUtils,
			ServiceErrorHandler serviceErrorHandler) {
		this.view = view;
		this.tableQueryResultWidget = tableQueryResultWidget;
		view.setTableQueryResultWidget(tableQueryResultWidget.asWidget());
		this.synapseJsniUtils = synapseJsniUtils;
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> descriptor, Callback widgetRefreshRequired, Long wikiVersionInView) {
		Long limit = TableEntityWidget.DEFAULT_LIMIT;
		try {
			if (descriptor.containsKey(WidgetConstants.TABLE_LIMIT_KEY))
				limit = Long.parseLong(descriptor.get(WidgetConstants.TABLE_LIMIT_KEY));
		} catch (Exception e) {
			synapseJsniUtils.consoleError("Could not set query limit: " + e.getMessage());
		}
		
		Long offset = TableEntityWidget.DEFAULT_OFFSET;
		try {
			if (descriptor.containsKey(WidgetConstants.TABLE_OFFSET_KEY))
				offset = Long.parseLong(descriptor.get(WidgetConstants.TABLE_OFFSET_KEY));
		} catch (Exception e) {
			synapseJsniUtils.consoleError("Could not set query offset: " + e.getMessage());
		}
		
		Query query = new Query();
		query.setLimit(limit);
		query.setOffset(offset);
		String sql = descriptor.get(WidgetConstants.TABLE_QUERY_KEY);
		query.setSql(sql);
		tableQueryResultWidget.configure(query, false, null);
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

}
