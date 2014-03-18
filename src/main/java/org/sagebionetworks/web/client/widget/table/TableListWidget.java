package org.sagebionetworks.web.client.widget.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SearchServiceAsync;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.QueryConstants.WhereOperator;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WhereCondition;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TableListWidget implements TableListWidgetView.Presenter, WidgetRendererPresenter {
	
	private TableListWidgetView view;
	private SynapseClientAsync synapseClient;
	private SearchServiceAsync searchService;
	private AuthenticationController authenticationController;
	private GlobalApplicationState globalApplicationState;
	private AdapterFactory adapterFactory;
	
	private String projectOwnerId;
	private boolean canEdit;
	private boolean showAddTable;
	private List<EntityHeader> configuredTables;
	
	@Inject
	public TableListWidget(TableListWidgetView view,
			SynapseClientAsync synapseClient, SearchServiceAsync searchService,
			AuthenticationController authenticationController,
			AdapterFactory adapterFactory,
			GlobalApplicationState globalApplicationState) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.searchService = searchService;
		this.authenticationController = authenticationController;
		this.adapterFactory = adapterFactory;
		this.globalApplicationState = globalApplicationState;
		view.setPresenter(this);
	}	
	
	public void configure(String projectOwnerId, final boolean canEdit, final boolean showAddTable) {
		this.configuredTables = null;
		this.projectOwnerId = projectOwnerId;
		this.canEdit = canEdit;
		this.showAddTable = showAddTable;
		
		List<WhereCondition> where = new ArrayList<WhereCondition>();
		where.add(new WhereCondition(WebConstants.ENTITY_PARENT_ID_KEY, WhereOperator.EQUALS, projectOwnerId));
		where.add(new WhereCondition(WebConstants.CONCRETE_TYPE_KEY, WhereOperator.EQUALS, TableEntity.class.getName()));
		searchService.searchEntities("entity", where, 1, 1000, null, false, new AsyncCallback<List<String>>() {
			@Override
			public void onSuccess(List<String> result) {
				configuredTables = new ArrayList<EntityHeader>();
				for(String entityHeaderJson : result) {
					try {
						configuredTables.add(new EntityHeader(adapterFactory.createNew(entityHeaderJson)));
					} catch (JSONObjectAdapterException e) {
						onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
					}
				}				
				view.configure(configuredTables, canEdit, showAddTable);
			}
			@Override
			public void onFailure(Throwable caught) {
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.isLoggedIn(), view))
					view.showErrorMessage(DisplayConstants.ERROR_GENERIC);
			}	
		});
		

	}
	
	public void configure(List<EntityHeader> tables, boolean canEdit, boolean showAddTable) {
		this.projectOwnerId = null;
		this.configuredTables = tables;
		this.canEdit = canEdit;
		this.showAddTable = showAddTable;

		view.configure(tables, canEdit, showAddTable);
	}
    
	@Override
	public Widget asWidget() {
		view.setPresenter(this);
		return view.asWidget();		
	}

	@Override
	public void configure(
			WikiPageKey wikiKey,
			Map<String, String> widgetDescriptor,
			org.sagebionetworks.web.client.utils.Callback widgetRefreshRequired,
			Long wikiVersionInView) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createTableEntity(final String name) {
		if(projectOwnerId == null) {
			view.showErrorMessage("Can not create Table outside of project context.");
			return;
		}
		TableEntity newTable = new TableEntity();
		String json;
		try {
			newTable.setName(name);
			newTable.setParentId(projectOwnerId);
			newTable.setColumnIds(Arrays.asList(new String[]{"175"}));
			newTable.setEntityType(TableEntity.class.getName());
			json = newTable.writeToJSONObject(adapterFactory.createNew()).toJSONString();
			synapseClient.createOrUpdateEntity(json, null, true, new AsyncCallback<String>() {
				@Override
				public void onSuccess(String result) {
					// add shell header to view instead of query
					EntityHeader header = new EntityHeader();
					header.setId(result);
					header.setName(name);
					view.addTable(header);
					view.showInfo(DisplayConstants.TABLE_CREATED, "");
				}
				@Override
				public void onFailure(Throwable caught) {
					if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.isLoggedIn(), view))
						view.showErrorMessage(DisplayConstants.TABLE_CREATION_FAILED);
				}
			});
		} catch (JSONObjectAdapterException e) {
			view.showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);
		}
	}

	@Override
	public void getTableDetails(EntityHeader table, final AsyncCallback<TableEntity> callback) {
		synapseClient.getEntity(table.getId(), new AsyncCallback<EntityWrapper>() {
			@Override
			public void onSuccess(EntityWrapper result) {
				try {
					TableEntity tableEntity = new TableEntity(adapterFactory.createNew(result.getEntityJson()));
					callback.onSuccess(tableEntity);
				} catch (JSONObjectAdapterException e) {
					onFailure(e);
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	
}
