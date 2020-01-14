package org.sagebionetworks.web.client.widget.table.v2.results;

import java.util.Map;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.PortalGinInjector;
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

	TableEntityWidget tableEntityWidget = null;
	SynapseJSNIUtils synapseJsniUtils;
	TableQueryResultWikiWidgetView view;
	SynapseJavascriptClient jsClient;
	SynapseAlert synAlert;
	ActionMenuWidget actionMenu;
	EntityActionController entityActionController;
	Query query;
	boolean isQueryVisible;
	PortalGinInjector ginInjector;
	GWTWrapper gwt;
	public static boolean isLoading = false;

	@Inject
	public TableQueryResultWikiWidget(TableQueryResultWikiWidgetView view, ActionMenuWidget actionMenu, EntityActionController entityActionController, SynapseJSNIUtils synapseJsniUtils, SynapseJavascriptClient jsClient, SynapseAlert synAlert, GWTWrapper gwt, PortalGinInjector ginInjector) {
		this.view = view;
		this.actionMenu = actionMenu;
		this.entityActionController = entityActionController;
		this.jsClient = jsClient;
		this.synAlert = synAlert;
		this.synapseJsniUtils = synapseJsniUtils;
		view.setSynAlert(synAlert.asWidget());
		this.ginInjector = ginInjector;
		this.gwt = gwt;
		actionMenu.addControllerWidget(entityActionController.asWidget());
	}

	private TableEntityWidget getTableEntityWidget() {
		if (tableEntityWidget == null) {
			tableEntityWidget = ginInjector.createNewTableEntityWidget();
			view.setTableQueryResultWidget(tableEntityWidget.asWidget());
		}
		return tableEntityWidget;
	}

	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> descriptor, Callback widgetRefreshRequired, Long wikiVersionInView) {
		if (isLoading) {
			gwt.scheduleExecution(() -> {
				configure(wikiKey, descriptor, widgetRefreshRequired, wikiVersionInView);
			}, 1000);
			return;
		}
		isLoading = true;

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
		Long tableVersionNumber = QueryBundleUtils.getTableVersion(query.getSql());
		configureTableQueryResultWidget(tableId, tableVersionNumber);
	}

	public void configureTableQueryResultWidget(String tableId, Long tableVersionNumber) {
		synAlert.clear();

		AsyncCallback<EntityBundle> callback = new AsyncCallback<EntityBundle>() {
			@Override
			public void onSuccess(EntityBundle bundle) {
				boolean isCurrentVersion = true;
				entityActionController.configure(actionMenu, bundle, isCurrentVersion, bundle.getRootWikiId(), EntityArea.TABLES);
				boolean canEdit = false;
				getTableEntityWidget().configure(bundle, tableVersionNumber, canEdit, TableQueryResultWikiWidget.this, actionMenu);
				hideEditActions();
				isLoading = false;
			}

			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
				isLoading = false;
			}
		};

		jsClient.getEntityBundleFromCache(tableId, callback);
	}

	public void hideEditActions() {
		this.actionMenu.setActionVisible(Action.UPLOAD_TABLE_DATA, false);
		this.actionMenu.setActionVisible(Action.EDIT_TABLE_DATA, false);
		this.actionMenu.setActionVisible(Action.SHOW_TABLE_SCHEMA, false);
		this.actionMenu.setActionVisible(Action.SHOW_VERSION_HISTORY, false);
		if (!isQueryVisible) {
			getTableEntityWidget().hideFiltering();
		}
	}

	@Override
	public Query getQueryString() {
		return query;
	}

	@Override
	public void onQueryChange(Query newQuery) {}

	@Override
	public void onPersistSuccess(EntityUpdatedEvent event) {}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

}
