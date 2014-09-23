package org.sagebionetworks.web.client.widget.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.entity.query.Condition;
import org.sagebionetworks.repo.model.entity.query.EntityFieldName;
import org.sagebionetworks.repo.model.entity.query.EntityQuery;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResult;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResults;
import org.sagebionetworks.repo.model.entity.query.EntityQueryUtils;
import org.sagebionetworks.repo.model.entity.query.EntityType;
import org.sagebionetworks.repo.model.entity.query.Operator;
import org.sagebionetworks.repo.model.entity.query.Sort;
import org.sagebionetworks.repo.model.entity.query.SortDirection;
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
import org.sagebionetworks.web.client.widget.pagination.BasicPaginationWidget;
import org.sagebionetworks.web.client.widget.pagination.PageChangeListener;
import org.sagebionetworks.web.client.widget.pagination.PaginationWidget;
import org.sagebionetworks.web.client.widget.table.modal.CreateTableModalView;
import org.sagebionetworks.web.shared.QueryConstants.WhereOperator;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WhereCondition;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.steadystate.css.parser.selectors.PseudoClassConditionImpl;

public class TableListWidget implements TableListWidgetView.Presenter, WidgetRendererPresenter, CreateTableModalView.Presenter, PageChangeListener {
	
	private static final long PAGE_SIZE = 10L;
	private static final long OFFSET_ZERO = 0L;
	protected static final String TABLE_NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTERS = "Table name must include at least one characters.";
	private TableListWidgetView view;
	private SynapseClientAsync synapseClient;
	private SearchServiceAsync searchService;
	private AuthenticationController authenticationController;
	private GlobalApplicationState globalApplicationState;
	private AdapterFactory adapterFactory;
	private CreateTableModalView createTableModalView;
	PaginationWidget paginationWidget;
	
	private String projectOwnerId;
	private boolean canEdit;
	private boolean showAddTable;
	private List<EntityQueryResult> configuredTables;
	private EntityQuery query;
	
	@Inject
	public TableListWidget(TableListWidgetView view,
			SynapseClientAsync synapseClient, SearchServiceAsync searchService,
			AuthenticationController authenticationController,
			AdapterFactory adapterFactory,
			GlobalApplicationState globalApplicationState,
			CreateTableModalView createTableModalView,
			PaginationWidget paginationWidget) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.searchService = searchService;
		this.authenticationController = authenticationController;
		this.adapterFactory = adapterFactory;
		this.globalApplicationState = globalApplicationState;
		this.createTableModalView = createTableModalView;
		this.view.setPresenter(this);
		this.view.addCreateTableModal(createTableModalView);
		this.createTableModalView.setPresenter(this);
		this.paginationWidget = paginationWidget;
		this.view.addPaginationWidget(paginationWidget);
	}	
	
	public void configure(String projectOwnerId, final boolean canEdit, final boolean showAddTable) {
		this.configuredTables = null;
		this.projectOwnerId = projectOwnerId;
		this.canEdit = canEdit;
		this.showAddTable = showAddTable;
		
		this.query = new EntityQuery();
		this.query.setFilterByType(EntityType.table);
		Sort sort = new Sort();
		sort.setColumnName(EntityFieldName.createdOn.name());
		sort.setDirection(SortDirection.DESC);
		this.query.setSort(sort);
		Condition condition = EntityQueryUtils.buildCondition(EntityFieldName.parentId, Operator.EQUALS, projectOwnerId);
		this.query.setConditions(Arrays.asList(condition));
		this.query.setLimit(PAGE_SIZE);
		queryForOnePage(OFFSET_ZERO);
	}
	
	private void queryForOnePage(final Long offset){
		this.query.setOffset(offset);
		this.view.setLoading(true);
		synapseClient.executeEntityQuery(this.query, new AsyncCallback<EntityQueryResults>() {
			
			@Override
			public void onSuccess(EntityQueryResults results) {
				paginationWidget.configure(query.getLimit(), query.getOffset(), results.getTotalEntityCount(), TableListWidget.this);
				view.showPaginationVisible(results.getTotalEntityCount() > query.getLimit());
				setResults(results);
			}
			
			@Override
			public void onFailure(Throwable arg0) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	private void setResults(EntityQueryResults results) {
		this.configuredTables = results.getEntities();

		view.configure(results.getEntities());
		//Must have edit and showAddTables for the buttons to be visible.
		boolean buttonsVisible = canEdit && showAddTable;
		view.setAddTableVisible(buttonsVisible);
		view.setUploadTableVisible(buttonsVisible);
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

	
	public void createTableEntity(final String name) {
		if(projectOwnerId == null) {
			view.showErrorMessage("Can not create Table outside of project context.");
			return;
		}
		TableEntity newTable = new TableEntity();
		String json;
		try {
			this.createTableModalView.setLoading(true);
			newTable.setName(name);
			newTable.setParentId(projectOwnerId);
			newTable.setEntityType(TableEntity.class.getName());
			json = newTable.writeToJSONObject(adapterFactory.createNew()).toJSONString();
			synapseClient.createOrUpdateEntity(json, null, true, new AsyncCallback<String>() {
				@Override
				public void onSuccess(String result) {
					createTableModalView.hide();
					// add shell header to view instead of query
					EntityHeader header = new EntityHeader();
					header.setId(result);
					header.setName(name);
					view.addTable(header);
					view.showInfo(DisplayConstants.TABLE_CREATED, "");
					queryForOnePage(OFFSET_ZERO);
				}
				@Override
				public void onFailure(Throwable caught) {
					if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view)){
						createTableModalView.showError(caught.getMessage());
						createTableModalView.setLoading(false);
					}

				}
			});
		} catch (JSONObjectAdapterException e) {
			createTableModalView.showError(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);
			createTableModalView.setLoading(false);
		}
	}

	@Override
	public void onAddTable() {
		// Clear and show the create table dialog
		this.createTableModalView.setLoading(false);
		this.createTableModalView.clear();
		this.createTableModalView.show();
	}

	@Override
	public void onUploadTable() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCreateTable() {
		String tableName = createTableModalView.getTableName();
		if(tableName == null || "".equals(tableName)){
			createTableModalView.showError(TABLE_NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTERS);
		}else{
			// Create the table
			createTableEntity(tableName);
		}
	}

	@Override
	public void onPageChange(Long newOffset) {
		// TODO Auto-generated method stub
		
	}
	
}
