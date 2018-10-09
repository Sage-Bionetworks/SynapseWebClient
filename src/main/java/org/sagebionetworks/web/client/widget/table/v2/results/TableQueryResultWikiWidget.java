package org.sagebionetworks.web.client.widget.table.v2.results;

import static org.sagebionetworks.repo.model.EntityBundle.BENEFACTOR_ACL;
import static org.sagebionetworks.repo.model.EntityBundle.ENTITY;
import static org.sagebionetworks.repo.model.EntityBundle.PERMISSIONS;
import static org.sagebionetworks.repo.model.EntityBundle.TABLE_DATA;

import java.util.Map;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.controller.EntityActionController;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.menu.v2.Action;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.table.QueryChangeHandler;
import org.sagebionetworks.web.client.widget.table.v2.TableEntityWidget;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TableQueryResultWikiWidget implements WidgetRendererPresenter, QueryChangeHandler {

	TableEntityWidget tableEntityWidget;
	SynapseJSNIUtils synapseJsniUtils;
	TableQueryResultWikiWidgetView view;
	SynapseJavascriptClient jsClient;
	SynapseAlert synAlert;
	ActionMenuWidget actionMenu;
	EntityActionController entityActionController;
	Query query;
	boolean isQueryVisible;
	
	@Inject
	public TableQueryResultWikiWidget(TableQueryResultWikiWidgetView view, 
			TableEntityWidget tableEntityWidget, 
			ActionMenuWidget actionMenu,
			EntityActionController entityActionController,
			SynapseJSNIUtils synapseJsniUtils,
			SynapseJavascriptClient jsClient,
			SynapseAlert synAlert) {
		this.view = view;
		this.tableEntityWidget = tableEntityWidget;
		this.actionMenu = actionMenu;
		this.entityActionController = entityActionController;
		this.jsClient = jsClient;
		this.synAlert = synAlert;
		view.setTableQueryResultWidget(tableEntityWidget.asWidget());
		this.synapseJsniUtils = synapseJsniUtils;
		view.setSynAlert(synAlert.asWidget());
		actionMenu.addControllerWidget(entityActionController.asWidget());
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
		
		isQueryVisible = true;
		if (descriptor.containsKey(WidgetConstants.QUERY_VISIBLE)) {
			isQueryVisible = Boolean.parseBoolean(descriptor.get(WidgetConstants.QUERY_VISIBLE));
		}
		
		hideEditActions();
		query = new Query();
		query.setLimit(limit);
		query.setOffset(offset);
		query.setIsConsistent(false);
		String sql = descriptor.get(WidgetConstants.TABLE_QUERY_KEY);
		query.setSql(sql);
		String tableId = QueryBundleUtils.getTableIdFromSql(query.getSql());
		configureTableQueryResultWidget(tableId);
	}
	
	public void configureTableQueryResultWidget(String tableId) {
		synAlert.clear();
		
		int mask = ENTITY | PERMISSIONS | TABLE_DATA | BENEFACTOR_ACL;
		AsyncCallback<EntityBundle> callback = new AsyncCallback<EntityBundle>() {
			@Override
			public void onSuccess(EntityBundle bundle) {
				boolean isCurrentVersion = true;
				entityActionController.configure(actionMenu, bundle, isCurrentVersion, bundle.getRootWikiId(), EntityArea.TABLES);
				boolean canEdit = false;
				tableEntityWidget.configure(bundle, canEdit, TableQueryResultWikiWidget.this, actionMenu);
				hideEditActions();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}			
		};
		
		jsClient.getEntityBundle(tableId, mask, callback);
	}
	
	public void hideEditActions() {
		this.actionMenu.setActionVisible(Action.UPLOAD_TABLE_DATA, false);
		this.actionMenu.setActionVisible(Action.EDIT_TABLE_DATA, false);
		this.actionMenu.setActionVisible(Action.SHOW_TABLE_SCHEMA, false);
		this.actionMenu.setActionVisible(Action.SHOW_FILE_HISTORY, false);
		if (!isQueryVisible) {
			tableEntityWidget.hideFiltering();
		}
	}
	
	@Override
	public Query getQueryString() {
		return query;
	}
	@Override
	public void onQueryChange(Query newQuery) {
	}
	@Override
	public void onPersistSuccess(EntityUpdatedEvent event) {
	}
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

}
