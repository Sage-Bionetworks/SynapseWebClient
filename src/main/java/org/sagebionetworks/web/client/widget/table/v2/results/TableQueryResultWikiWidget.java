package org.sagebionetworks.web.client.widget.table.v2.results;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.table.v2.TableEntityWidget;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TableQueryResultWikiWidget implements WidgetRendererPresenter{

	TableQueryResultWidget tableQueryResultWidget;
	SynapseJSNIUtils synapseJsniUtils;
	TableQueryResultWikiWidgetView view;
	SynapseClientAsync synapseClient;
	SynapseAlert synAlert;
	
	@Inject
	public TableQueryResultWikiWidget(TableQueryResultWikiWidgetView view, 
			TableQueryResultWidget tableQueryResultWidget, 
			SynapseJSNIUtils synapseJsniUtils,
			SynapseClientAsync synapseClient,
			SynapseAlert synAlert) {
		this.view = view;
		this.tableQueryResultWidget = tableQueryResultWidget;
		this.synapseClient = synapseClient;
		this.synAlert = synAlert;
		view.setTableQueryResultWidget(tableQueryResultWidget.asWidget());
		this.synapseJsniUtils = synapseJsniUtils;
		view.setSynAlert(synAlert.asWidget());
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
		configureTableQueryResultWidget(query);
	}
	
	public void configureTableQueryResultWidget(final Query query) {
		synAlert.clear();
		final String tableId = QueryBundleUtils.getTableIdFromSql(query.getSql());
		synapseClient.getEntityHeaderBatch(Collections.singletonList(tableId), new AsyncCallback<ArrayList<EntityHeader>>() {
			@Override
			public void onSuccess(ArrayList<EntityHeader> result) {
				if (result.size() != 1) {
					onFailure(new NotFoundException(tableId));
				} else {
					EntityHeader header = result.get(0);
					boolean isView = EntityView.class.getName().equals(header.getType());
					tableQueryResultWidget.configure(query, false, isView, null);			
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
				
	}
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

}
