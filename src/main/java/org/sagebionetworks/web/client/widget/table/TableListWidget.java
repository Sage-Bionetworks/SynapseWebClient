package org.sagebionetworks.web.client.widget.table;

import java.util.ArrayList;
import java.util.List;
import org.sagebionetworks.repo.model.EntityChildrenRequest;
import org.sagebionetworks.repo.model.EntityChildrenResponse;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.entity.Direction;
import org.sagebionetworks.repo.model.entity.SortBy;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import com.google.gwt.http.client.Request;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * This widget lists the tables of a given project.
 * 
 * @author John
 *
 */
public class TableListWidget implements TableListWidgetView.Presenter, IsWidget {
	private TableListWidgetView view;
	private SynapseJavascriptClient jsClient;
	private EntityChildrenRequest query;
	private EntityBundle parentBundle;
	private CallbackP<EntityHeader> onTableClickCallback;
	private LoadMoreWidgetContainer loadMoreWidget;
	private SynapseAlert synAlert;
	public static final SortBy DEFAULT_SORT_BY = SortBy.CREATED_ON;
	public static final Direction DEFAULT_DIRECTION = Direction.DESC;
	boolean isInitializing = false;
	Request currentRequest = null;
	private List<String> idList = null;

	@Inject
	public TableListWidget(TableListWidgetView view, SynapseJavascriptClient jsClient, LoadMoreWidgetContainer loadMoreWidget, SynapseAlert synAlert) {
		this.view = view;
		this.jsClient = jsClient;
		this.loadMoreWidget = loadMoreWidget;
		this.synAlert = synAlert;
		this.view.setPresenter(this);
		this.view.setLoadMoreWidget(loadMoreWidget);
		view.setSynAlert(synAlert);
		loadMoreWidget.configure(new Callback() {
			@Override
			public void invoke() {
				loadMore();
			}
		});
	}

	/**
	 * Configure this widget before use.
	 * 
	 * @param projectOwnerId
	 * @param canEdit
	 * @param showAddTable
	 */
	public void configure(EntityBundle parentBundle) {
		if (currentRequest != null) {
			currentRequest.cancel();
		}
		isInitializing = true;
		this.parentBundle = parentBundle;
		loadData();
	}

	public void resetSynIdList() {
		idList = null;
	}

	public void addSynIdToList(String id) {
		if (idList == null) {
			idList = new ArrayList<>();
		}
		idList.add(id);
	}

	public void toggleSort(SortBy sortColumn) {
		Direction newDirection = Direction.ASC.equals(query.getSortDirection()) ? Direction.DESC : Direction.ASC;
		onSort(sortColumn, newDirection);
	}

	public void onSort(SortBy sortColumn, Direction sortDirection) {
		resetSynIdList();
		query.setSortBy(sortColumn);
		query.setSortDirection(sortDirection);
		query.setNextPageToken(null);
		view.clearTableWidgets();
		loadMore();
	}

	public void loadData() {
		resetSynIdList();
		query = createQuery(parentBundle.getEntity().getId());
		view.clearTableWidgets();
		view.hideLoading();
		query.setNextPageToken(null);
		loadMore();
	}

	/**
	 * Create a new query.
	 * 
	 * @param parentId
	 * @return
	 */
	public EntityChildrenRequest createQuery(String parentId) {
		EntityChildrenRequest newQuery = new EntityChildrenRequest();
		newQuery.setSortBy(DEFAULT_SORT_BY);
		newQuery.setSortDirection(DEFAULT_DIRECTION);
		newQuery.setParentId(parentId);
		List<EntityType> types = new ArrayList<EntityType>();
		types.add(EntityType.table);
		types.add(EntityType.entityview);
		newQuery.setIncludeTypes(types);
		return newQuery;
	}

	/**
	 * Run a query and populate the page with the results.
	 * 
	 * @param offset The offset used by the query.
	 */
	private void loadMore() {
		synAlert.clear();
		if (isInitializing) {
			view.clearSortUI();
		} else {
			view.setSortUI(query.getSortBy(), query.getSortDirection());
		}
		currentRequest = jsClient.getEntityChildren(query, new AsyncCallback<EntityChildrenResponse>() {
			public void onSuccess(EntityChildrenResponse result) {
				query.setNextPageToken(result.getNextPageToken());
				loadMoreWidget.setIsMore(result.getNextPageToken() != null);
				setResults(result.getPage());
				isInitializing = false;
			};

			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	}

	private void setResults(List<EntityHeader> results) {
		view.hideLoading();
		for (EntityHeader header : results) {
			addSynIdToList(header.getId());
			view.addTableListItem(header);
		}
		for (EntityHeader header : results) {
			jsClient.populateEntityBundleCache(header.getId());
		}
	}

	@Override
	public Widget asWidget() {
		view.setPresenter(this);
		return view.asWidget();
	}

	/**
	 * Invokes callback when a table entity is clicked in the table list.
	 * 
	 * @param callback
	 */
	public void setTableClickedCallback(CallbackP<EntityHeader> callback) {
		this.onTableClickCallback = callback;
	}

	@Override
	public void onTableClicked(EntityHeader entityHeader) {
		if (onTableClickCallback != null) {
			view.showLoading();
			view.clearTableWidgets();
			onTableClickCallback.invoke(entityHeader);
		}
	}

	@Override
	public void copyIDsToClipboard() {
		StringBuilder clipboardValue = new StringBuilder();
		for (String synId : idList) {
			clipboardValue.append(synId);
			clipboardValue.append("\n");
		}
		view.copyToClipboard(clipboardValue.toString());
	}

}
