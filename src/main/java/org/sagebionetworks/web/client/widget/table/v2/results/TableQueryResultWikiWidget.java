package org.sagebionetworks.web.client.widget.table.v2.results;

import static org.sagebionetworks.repo.model.EntityBundle.ANNOTATIONS;
import static org.sagebionetworks.repo.model.EntityBundle.BENEFACTOR_ACL;
import static org.sagebionetworks.repo.model.EntityBundle.DOI;
import static org.sagebionetworks.repo.model.EntityBundle.ENTITY;
import static org.sagebionetworks.repo.model.EntityBundle.ENTITY_PATH;
import static org.sagebionetworks.repo.model.EntityBundle.HAS_CHILDREN;
import static org.sagebionetworks.repo.model.EntityBundle.PERMISSIONS;
import static org.sagebionetworks.repo.model.EntityBundle.TABLE_DATA;

import java.util.Map;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
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
	SynapseClientAsync synapseClient;
	SynapseAlert synAlert;
	ActionMenuWidget actionMenu;
	EntityActionController entityActionController;
	Query query;
	
	@Inject
	public TableQueryResultWikiWidget(TableQueryResultWikiWidgetView view, 
			TableEntityWidget tableEntityWidget, 
			ActionMenuWidget actionMenu,
			EntityActionController entityActionController,
			SynapseJSNIUtils synapseJsniUtils,
			SynapseClientAsync synapseClient,
			SynapseAlert synAlert) {
		this.view = view;
		this.tableEntityWidget = tableEntityWidget;
		this.actionMenu = actionMenu;
		this.entityActionController = entityActionController;
		this.synapseClient = synapseClient;
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
		hideEditActions();
		query = new Query();
		query.setLimit(limit);
		query.setOffset(offset);
		String sql = descriptor.get(WidgetConstants.TABLE_QUERY_KEY);
		query.setSql(sql);
		configureTableQueryResultWidget(query);
	}
	
	public void configureTableQueryResultWidget(final Query query) {
		synAlert.clear();
		
		int mask = ENTITY | ANNOTATIONS | PERMISSIONS | ENTITY_PATH | HAS_CHILDREN | DOI | TABLE_DATA | BENEFACTOR_ACL;
		AsyncCallback<EntityBundle> callback = new AsyncCallback<EntityBundle>() {
			@Override
			public void onSuccess(EntityBundle bundle) {
				boolean isCurrentVersion = true;
				entityActionController.configure(actionMenu, bundle, isCurrentVersion, bundle.getRootWikiId(), null);
				boolean canEdit = false;
				tableEntityWidget.configure(bundle, canEdit, TableQueryResultWikiWidget.this, actionMenu);
				hideEditActions();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}			
		};
		
		String tableId = QueryBundleUtils.getTableIdFromSql(query.getSql());
		synapseClient.getEntityBundle(tableId, mask, callback);
	}
	public void hideEditActions() {
		this.actionMenu.setActionVisible(Action.UPLOAD_TABLE_DATA, false);
		this.actionMenu.setActionVisible(Action.EDIT_TABLE_DATA, false);
		this.actionMenu.setActionVisible(Action.TOGGLE_TABLE_SCHEMA, false);
		this.actionMenu.setActionVisible(Action.TOGGLE_VIEW_SCOPE, false);
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
