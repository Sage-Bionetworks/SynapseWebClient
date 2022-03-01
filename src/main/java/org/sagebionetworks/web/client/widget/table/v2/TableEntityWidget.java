package org.sagebionetworks.web.client.widget.table.v2;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import static org.sagebionetworks.web.client.widget.table.v2.results.QueryBundleUtils.DEFAULT_LIMIT;
import static org.sagebionetworks.web.client.widget.table.v2.results.QueryBundleUtils.DEFAULT_OFFSET;

import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.ui.constants.AlertType;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.Dataset;
import org.sagebionetworks.repo.model.table.DatasetItem;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.repo.model.table.QueryFilter;
import org.sagebionetworks.repo.model.table.SortItem;
import org.sagebionetworks.repo.model.table.SubmissionView;
import org.sagebionetworks.repo.model.table.Table;
import org.sagebionetworks.repo.model.table.TableBundle;
import org.sagebionetworks.repo.model.table.View;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.EntityTypeUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cache.SessionStorage;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.jsinterop.DatasetEditorProps;
import org.sagebionetworks.web.client.jsinterop.ToastMessageOptions;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.CopyTextModal;
import org.sagebionetworks.web.client.widget.clienthelp.FileViewClientsHelp;
import org.sagebionetworks.web.client.widget.entity.controller.PreflightController;
import org.sagebionetworks.web.client.widget.entity.file.AddToDownloadListV2;
import org.sagebionetworks.web.client.widget.entity.menu.v2.Action;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.table.QueryChangeHandler;
import org.sagebionetworks.web.client.widget.table.modal.download.DownloadTableQueryModalWidget;
import org.sagebionetworks.web.client.widget.table.modal.fileview.TableType;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadTableModalWidget;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget.WizardCallback;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryBundleUtils;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryInputListener;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryResultsListener;
import org.sagebionetworks.web.client.widget.table.v2.results.TableQueryResultWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * TableEntity widget provides viewing and editing of both a table's schema and row data. It also
 * allows a user to execute a query against the table by writing SQL.
 * 
 * @author John
 * 
 */
public class TableEntityWidget implements TableEntityWidgetView.Presenter, IsWidget, QueryResultsListener, QueryInputListener {

	public static final String IS_INVOKING_DOWNLOAD_TABLE = "isInvokingDownloadTable";
	public static final String NO_FACETS_SIMPLE_SEARCH_UNSUPPORTED = "In order to use simple search, you must first set columns to be facets in the schema editor.";
	public static final String RESET_SEARCH_QUERY_MESSAGE = "The search query will be reset. Are you sure that you would like to switch to simple search mode?";

	public static final String RESET_SEARCH_QUERY = "Reset search query?";
	public static final String getNoColumnsMessage(TableType tableType, boolean editable) {
		return "This " + tableType.getDisplayName() + " does not have any columns." + (editable ? " Edit the Schema to add columns to this " + tableType.getDisplayName() + "." : "");
	}

	public static final String noScopeMessage(TableType tableType, boolean editable) {
		if (TableType.dataset.equals(tableType)) {
			return "This " + tableType.getDisplayName() + " does not have any items." + (editable ? (" Select \"Edit " + tableType.getDisplayName() + " Items\" from the Tools Menu to add items to this " + tableType.getDisplayName() + ".") : "");
		} else {
			return "This " + tableType.getDisplayName() + " does not have a defined scope." + (editable ? " Edit the scope to populate the " + tableType.getDisplayName() + "." : "");
		}
	}

	public static final int MAX_SORT_COLUMNS = 3;
	// Look for:
	// beginning of the line, any character, whitespace, "from", whitespace, "syn<number>", optional
	// "dot notation", optional whitespace, optional order by statement, end of line.
	public static final RegExp SIMPLE_QUERY_REGEX = RegExp.compile("^.*(\\s+from\\s+syn([0-9]+[.]?[0-9]*)+)\\s*(order by .*)?$", "i");

	DownloadTableQueryModalWidget downloadTableQueryModalWidget;
	UploadTableModalWidget uploadTableModalWidget;
	TableEntityWidgetView view;
	ActionMenuWidget actionMenu;
	PreflightController preflightController;
	SessionStorage sessionStorage;
	EventBus eventBus;

	EntityBundle entityBundle;
	String tableId;
	Long tableVersionNumber = null;
	boolean isCurrentVersion = true;
	TableBundle tableBundle;
	boolean canEdit, canEditResults;
	TableType tableType;
	QueryChangeHandler queryChangeHandler;
	TableQueryResultWidget queryResultsWidget;
	QueryInputWidget queryInputWidget;
	Query currentQuery;
	CopyTextModal copyTextModal;
	SynapseClientAsync synapseClient;
	FileViewClientsHelp fileViewClientsHelp;
	public static final String SHOW = "Show ";
	public static final String HIDE = "Hide ";
	public static final String SCOPE = "Scope of ";
	public static final String SCHEMA = " Schema";
	String entityTypeDisplay;
	PortalGinInjector ginInjector;

	boolean hasQueryableData; // if `false`, then a query will never yield data.

	@Inject
	public TableEntityWidget(TableEntityWidgetView view, TableQueryResultWidget queryResultsWidget, QueryInputWidget queryInputWidget, PreflightController preflightController, SynapseClientAsync synapseClient, FileViewClientsHelp fileViewClientsHelp, PortalGinInjector ginInjector, SessionStorage sessionStorage, EventBus eventBus) {
		this.view = view;
		this.queryResultsWidget = queryResultsWidget;
		this.queryInputWidget = queryInputWidget;
		this.preflightController = preflightController;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.fileViewClientsHelp = fileViewClientsHelp;
		this.ginInjector = ginInjector;
		this.sessionStorage = sessionStorage;
		this.eventBus = eventBus;
		this.view.setPresenter(this);
		this.view.setQueryResultsWidget(this.queryResultsWidget);
		this.view.setQueryInputWidget(this.queryInputWidget);
	}

	public DownloadTableQueryModalWidget getDownloadTableQueryModalWidget() {
		if (downloadTableQueryModalWidget == null) {
			downloadTableQueryModalWidget = ginInjector.getDownloadTableQueryModalWidget();
			view.addModalWidget(downloadTableQueryModalWidget);
		}
		return downloadTableQueryModalWidget;
	}

	public UploadTableModalWidget getUploadTableModalWidget() {
		if (uploadTableModalWidget == null) {
			uploadTableModalWidget = ginInjector.getUploadTableModalWidget();
			view.addModalWidget(uploadTableModalWidget);
		}
		return uploadTableModalWidget;
	}

	public CopyTextModal getCopyTextModal() {
		if (copyTextModal == null) {
			copyTextModal = ginInjector.getCopyTextModal();
			copyTextModal.setTitle("Query:");
			view.addModalWidget(copyTextModal);
		}
		return copyTextModal;
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	/**
	 * Configure this widget with new data. Calling this method will replace all widget state to the
	 * passed parameters.
	 * 
	 * @param bundle
	 * @param canEdit
	 * @param queryString
	 * @param qch
	 */
	public void configure(EntityBundle bundle, Long versionNumber, boolean canEdit, QueryChangeHandler qch, ActionMenuWidget actionMenu) {
		this.entityBundle = bundle;
		Entity table = bundle.getEntity();
		this.tableType = TableType.getTableType(table);
		this.tableId = bundle.getEntity().getId();
		this.tableVersionNumber = versionNumber;
		this.isCurrentVersion = tableVersionNumber == null;
		this.tableBundle = bundle.getTableBundle();
		this.canEdit = canEdit;
		this.canEditResults = canEdit;
		this.queryChangeHandler = qch;
		this.view.configure(bundle, this.canEdit && isCurrentVersion);
		this.actionMenu = actionMenu;
		this.entityTypeDisplay = EntityTypeUtils.getFriendlyEntityTypeName(bundle.getEntity());
		reconfigureState();
		showEditorIfEditableAndEmpty();
	}

	private void reconfigureState() {
		initializeQuery();
		if (entityBundle.getEntity() instanceof View) {
			queryResultsWidget.getTotalVisibleResultsWidget().configure((View) entityBundle.getEntity());
		}
		if (this.hasQueryableData) {
			initSimpleAdvancedQueryState();
		}
		configureActions();
	}

	/**
	 * For certain table types, if the user has edit permissions and the table doesn't have any data,
	 *   immediately prompt them with the editor.
	 *
	 * The primary scenario for this behavior is when initially creating a table and opening this page.
	 */
	private void showEditorIfEditableAndEmpty() {
		// This currently only applies to Datasets, since other types of tables are editable via the wizard, whereas the
		// dataset items editor is built-in to this widget.
		if (canEdit && isCurrentVersion) {
			if (entityBundle.getEntity() instanceof Dataset) {
				Dataset dataset = (Dataset) entityBundle.getEntity();
				if (dataset.getItems() == null || dataset.getItems().size() == 0) {
					showDatasetItemsEditor();
				}
			}
		}
	}

	/**
	 * Setup the actions for this widget.
	 */
	private void configureActions() {
		// Listen to action events.
		view.setScopeVisible(false);
		view.setSchemaVisible(false);
		actionMenu.setActionText(Action.SHOW_TABLE_SCHEMA, SHOW + entityTypeDisplay + SCHEMA);
		actionMenu.setActionText(Action.SHOW_VIEW_SCOPE, SHOW + SCOPE + entityTypeDisplay);
		actionMenu.setActionText(Action.EDIT_DATASET_ITEMS, "Edit " + entityTypeDisplay + " Items");
		this.actionMenu.setActionListener(Action.UPLOAD_TABLE_DATA, action -> {
			onUploadTableData();
		});
		this.actionMenu.setActionListener(Action.EDIT_TABLE_DATA, action -> {
			onEditResults();
		});
		this.actionMenu.setActionListener(Action.SHOW_TABLE_SCHEMA, action -> {
			boolean isVisible = !view.isSchemaVisible();
			view.setSchemaVisible(isVisible);
			String showHide = isVisible ? HIDE : SHOW;
			actionMenu.setActionText(Action.SHOW_TABLE_SCHEMA, showHide + entityTypeDisplay + SCHEMA);
		});

		this.actionMenu.setActionListener(Action.SHOW_VIEW_SCOPE, action -> {
			boolean isVisible = !view.isScopeVisible();
			view.setScopeVisible(isVisible);
			String showHide = isVisible ? HIDE : SHOW;
			actionMenu.setActionText(Action.SHOW_VIEW_SCOPE, showHide + SCOPE + entityTypeDisplay);
		});

		this.actionMenu.setActionListener(Action.EDIT_DATASET_ITEMS, action -> {
			showDatasetItemsEditor();
		});

		// Edit data
		this.actionMenu.setActionEnabled(Action.EDIT_TABLE_DATA, hasQueryableData);
		if (hasQueryableData) {
			this.actionMenu.setEditTableDataTooltipText("Bulk edit cell values");
		} else {
			this.actionMenu.setEditTableDataTooltipText("There is no data to edit");
		}

		// Download options
		this.actionMenu.setTableDownloadOptionsVisible(true);
		this.actionMenu.setActionListener(Action.DOWNLOAD_TABLE_QUERY_RESULTS, action -> {
			onDownloadResults();
		});
		this.actionMenu.setActionVisible(Action.ADD_TABLE_RESULTS_TO_DOWNLOAD_LIST, tableType.isIncludeFiles());
		this.actionMenu.setActionVisible(Action.TABLE_DOWNLOAD_PROGRAMMATIC_OPTIONS, tableType.isIncludeFiles());
		if (this.entityBundle.getEntity() instanceof Dataset && isCurrentVersion && hasQueryableData) {
			// SWC-5878 - On the current (non-snapshot) version of a dataset, only editors should be able to download
			this.actionMenu.setTableDownloadOptionsEnabled(canEdit);
			if (!canEdit) {
				this.actionMenu.setDownloadActionsDisabledTooltipText("A draft version of a Dataset cannot be downloaded");
			}
		} else if(!entityBundle.getPermissions().getCanDownload()) {
			this.actionMenu.setTableDownloadOptionsEnabled(false);
		} else {
			this.actionMenu.setTableDownloadOptionsEnabled(hasQueryableData);
			if (!hasQueryableData) {
				this.actionMenu.setDownloadActionsDisabledTooltipText("There is no data to download");
			}
		}
		this.actionMenu.setActionListener(Action.TABLE_DOWNLOAD_PROGRAMMATIC_OPTIONS, action -> {
			onShowDownloadFilesProgrammatically();
		});
		this.actionMenu.setActionListener(Action.ADD_TABLE_RESULTS_TO_DOWNLOAD_LIST, action -> {
			onAddToDownloadList();
		});


		// Query options
		this.actionMenu.setActionListener(Action.SHOW_ADVANCED_SEARCH, action -> {
			onShowAdvancedSearch();
		});
		this.actionMenu.setActionListener(Action.SHOW_SIMPLE_SEARCH, action -> {
			onShowSimpleSearch();
		});
		this.actionMenu.setActionListener(Action.SHOW_QUERY, action -> {
			onShowQuery();
		});
	}

	/**
	 * Initilializes the Table query. This method will not issue a query if we know we will not get results (e.g. if there are no columns)
	 * @param onQuery a callback that will be invoked if we initiate a query.
	 */
	public void initializeQuery() {
		// Make a few checks to see if we know that we won't get results before submitting the query
		if (entityBundle.getEntity() instanceof View && hasUndefinedScope((View) entityBundle.getEntity())) {
			// If the table is a View with no scope or Dataset with no items, there will be no results.
			// Show a warning or prompt.
			setNoScopeState();
			this.hasQueryableData = false;
		} else if (this.tableBundle.getColumnModels().size() < 1) {
			// If there are no columns, there will be no results, so ask the user to create some columns.
			setNoColumnsState();
			this.hasQueryableData = false;
		} else {
			// There are columns, and if this is a view, the scope is defined.
			this.hasQueryableData = true;
			Query startQuery = queryChangeHandler.getQueryString();
			if (startQuery == null) {
				// use a default query
				startQuery = getDefaultQuery();
			}
			setQuery(startQuery, false);
		}
	}

	/**
	 * Check if a View has an undefined scope. If the scope is undefined, a query need not be made.
	 * @param view
	 * @return true iff we are sure that the scope is undefined.
	 */
	private static boolean hasUndefinedScope(View view) {
		if (view instanceof EntityView) {
			List<String> scopeIds = ((EntityView) view).getScopeIds();
			return scopeIds == null || scopeIds.size() == 0;
		} else if (view instanceof SubmissionView) {
			List<String> scopeIds = ((SubmissionView) view).getScopeIds();
			return scopeIds == null || scopeIds.size() == 0;
		} else if (view instanceof Dataset) {
			List<DatasetItem> datasetItems = ((Dataset) view).getItems();
			return datasetItems == null || datasetItems.size() == 0;
		} else {
			// if we aren't sure, return false
			return false;
		}
	}

	/**
	 * Set the query used by this widget.
	 * 
	 * @param sql
	 */
	private void setQuery(Query query, boolean isFromResults) {
		this.currentQuery = query;
		this.queryInputWidget.configure(query.getSql(), this);
		this.view.setQueryResultsVisible(true);
		this.view.setTableMessageVisible(false);
		if (!isFromResults) {
			this.queryResultsWidget.configure(query, this.canEditResults, tableType, this);
		}
	}

	private void initSimpleAdvancedQueryState() {
		boolean isAdvancedQuery = isAdvancedQuery();
		if (!isAdvancedQuery) {
			// show simple search if facets are available.
			boolean isFacets = isFacets();
			if (isFacets) {
				showSimpleSearchUI();
			} else {
				showAdvancedSearchUI();
			}
		} else {
			showAdvancedSearchUI();
		}
	}

	private boolean isFacets() {
		if (tableBundle == null || tableBundle.getColumnModels() == null || tableBundle.getColumnModels().isEmpty()) {
			return false;
		}
		for (ColumnModel cm : tableBundle.getColumnModels()) {
			if (cm.getFacetType() != null) {
				return true;
			}
		}
		return false;
	}

	private boolean isAdvancedQuery() {
		if (currentQuery == null || currentQuery.getSql() == null) {
			return false;
		}
		// is there anything after the synID (where, group by, having, order by, limit, ...)?
		MatchResult match = SIMPLE_QUERY_REGEX.exec(currentQuery.getSql());
		// if match is null, then this sql is more complex
		return match == null;
	}

	private void showSimpleSearchUI() {
		actionMenu.setActionVisible(Action.SHOW_ADVANCED_SEARCH, true);
		actionMenu.setActionVisible(Action.SHOW_SIMPLE_SEARCH, false);
		queryInputWidget.setShowSimpleSearchButtonVisible(false);
		queryResultsWidget.setFacetsVisible(true);
		actionMenu.setActionVisible(Action.SHOW_QUERY, false);
		queryInputWidget.setQueryInputVisible(false);
	}

	public void hideFiltering() {
		queryInputWidget.setVisible(false);
		queryResultsWidget.setFacetsVisible(false);
		actionMenu.setActionVisible(Action.SHOW_ADVANCED_SEARCH, false);
		actionMenu.setActionVisible(Action.SHOW_SIMPLE_SEARCH, false);
		queryInputWidget.setShowSimpleSearchButtonVisible(false);
	}

	private void showAdvancedSearchUI() {
		actionMenu.setActionVisible(Action.SHOW_ADVANCED_SEARCH, false);
		// SWC-3762: show the simple search link if facets exist, or if the user can set up facets.
		boolean showSimpleSearch = isFacets() || canEdit;
		actionMenu.setActionVisible(Action.SHOW_SIMPLE_SEARCH, showSimpleSearch);
		queryInputWidget.setShowSimpleSearchButtonVisible(showSimpleSearch);
		queryResultsWidget.setFacetsVisible(false);
		actionMenu.setActionVisible(Action.SHOW_QUERY, false);
		queryInputWidget.setQueryInputVisible(true);
	}

	@Override
	public void onShowSimpleSearch() {
		if (isFacets()) {
			// does the current query have a where clause?
			if (isAdvancedQuery()) {
				// we must wipe it out. Confirm with the user that this is acceptable.
				view.showConfirmDialog(RESET_SEARCH_QUERY, RESET_SEARCH_QUERY_MESSAGE, new Callback() {
					@Override
					public void invoke() {
						showSimpleSearchUI();
						setQuery(getDefaultQuery(), false);
					}
				});
			} else {
				showSimpleSearchUI();
			}
		} else {
			// show error, must define facets for simple search.
			view.showErrorMessage(NO_FACETS_SIMPLE_SEARCH_UNSUPPORTED);
		}
	}

	public void onShowAdvancedSearch() {
		// set query based on selected facets
		AsyncCallback<String> callback = new AsyncCallback<String>() {
			@Override
			public void onSuccess(String sql) {
				Query q = getDefaultQuery();
				q.setSql(sql);
				showAdvancedSearchUI();
				// set the current query. results have not changed, so set isFromResults=true
				setQuery(q, true);
			}

			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		};
		generateSqlWithFacets(callback);
	}

	private void generateSqlWithFacets(AsyncCallback<String> callback) {
		if (currentQuery.getSelectedFacets() == null || currentQuery.getSelectedFacets().isEmpty()) {
			callback.onSuccess(currentQuery.getSql());
		} else {
			synapseClient.generateSqlWithFacets(currentQuery.getSql(), currentQuery.getSelectedFacets(), tableBundle.getColumnModels(), callback);
		}
	}

	/**
	 * Set the view to show no columns message.
	 */
	private void setNoColumnsState() {
		String message = getNoColumnsMessage(tableType, this.canEdit);
		// There can be no query when there are no columns
		if (this.queryChangeHandler.getQueryString() != null) {
			this.queryChangeHandler.onQueryChange(null);
		}
		view.setQueryInputVisible(false);
		view.setQueryResultsVisible(false);
		view.showTableMessage(AlertType.INFO, message);
		view.setTableMessageVisible(true);
	}

	private void setNoScopeState() {
		if (this.entityBundle.getEntity() instanceof View) {
			String message = noScopeMessage(tableType, this.canEdit);
			// There can be no query when there are no items
			if (this.queryChangeHandler.getQueryString() != null) {
				this.queryChangeHandler.onQueryChange(null);
			}
			view.setQueryInputVisible(false);
			view.setQueryResultsVisible(false);
			view.showTableMessage(AlertType.INFO, message);
			view.setTableMessageVisible(true);
		}
	}

	/**
	 * Build the default query based on the current table data.
	 * 
	 * @return
	 */
	public Query getDefaultQuery() {
		return QueryBundleUtils.getDefaultQuery(tableId, isCurrentVersion, tableVersionNumber);
	}

	/**
	 * Get the default page size based on the current state of the table.
	 * 
	 * @return
	 */
	public long getDefaultPageSize() {
		if (this.tableBundle.getMaxRowsPerPage() == null) {
			return DEFAULT_LIMIT;
		}
		long maxRowsPerPage = this.tableBundle.getMaxRowsPerPage();
		long maxTwoThirds = maxRowsPerPage - maxRowsPerPage / 3l;
		return Math.min(maxTwoThirds, DEFAULT_LIMIT);
	}

	@Override
	public void queryExecutionStarted() {
		// Pass this along to the input widget.
		this.queryInputWidget.queryExecutionStarted();
		// Disabling menu items does not seem to work well so we hide the items instead.
		this.actionMenu.setActionVisible(Action.EDIT_TABLE_DATA, false);
		this.actionMenu.setActionVisible(Action.DOWNLOAD_TABLE_QUERY_RESULTS, false);
		view.setTableToolbarVisible(false);
	}

	@Override
	public void queryExecutionFinished(boolean wasSuccessful, boolean resultsEditable) {
		// Pass this along to the input widget.
		this.queryInputWidget.queryExecutionFinished(wasSuccessful, resultsEditable);
		this.actionMenu.setActionVisible(Action.EDIT_TABLE_DATA, wasSuccessful && canEditResults && resultsEditable);
		this.actionMenu.setActionVisible(Action.DOWNLOAD_TABLE_QUERY_RESULTS, wasSuccessful);

		// Set this as the query if it was successful
		if (wasSuccessful) {
			this.queryChangeHandler.onQueryChange(this.currentQuery);

			// PORTALS-596: if being directed to Synapse.org to download a file set, then automatically show the
			// "Add To Download List" UI.
			if (Header.isShowingPortalAlert && ginInjector.getAuthenticationController().isLoggedIn()) {
				try {
					boolean isDownloadTable = Header.portalAlertJson.getBoolean(IS_INVOKING_DOWNLOAD_TABLE);
					if (isDownloadTable) {
						onAddToDownloadList();
					}
					Header.portalAlertJson.put(IS_INVOKING_DOWNLOAD_TABLE, false);
				} catch (Exception e) {
					ginInjector.getSynapseJSNIUtils().consoleError(e);
				}
			}
		}
		view.setTableToolbarVisible(true);
	}

	/**
	 * Called when the user executes a new query from the query input box. When the SQL changes reset
	 * back to the first page.
	 */
	@Override
	public void onExecuteQuery(String sql) {
		this.currentQuery.setSql(sql);
		this.currentQuery.setSort(new ArrayList<SortItem>());
		this.currentQuery.setAdditionalFilters(new ArrayList<QueryFilter>());
		this.currentQuery.setLimit(DEFAULT_LIMIT);
		this.currentQuery.setOffset(DEFAULT_OFFSET);
		setQuery(this.currentQuery, false);
	}

	@Override
	public void onEditResults() {
		if (isCurrentVersion) {
			preflightController.checkUploadToEntity(this.entityBundle, new Callback() {
				@Override
				public void invoke() {
					postCheckEditResults();
				}
			});
		} else {
			view.showErrorMessage("Can only edit data in the most recent table/view version.");
		}
	}

	/**
	 * Called only when all pre-flight checks on entity edit have been met.
	 */
	public void postCheckEditResults() {
		queryResultsWidget.onEditRows();
	}

	@Override
	public void onDownloadResults() {
		getDownloadTableQueryModalWidget().configure(this.queryInputWidget.getInputSQL(), this.tableId, currentQuery.getSelectedFacets());
		getDownloadTableQueryModalWidget().showModal();
	}

	public void onUploadTableData() {
		if (isCurrentVersion) {
			// proceed as long as the user has meet all upload pre-flight checks
			this.preflightController.checkUploadToEntity(this.entityBundle, new Callback() {
				@Override
				public void invoke() {
					postCheckonUploadTableData();
				}
			});
		} else {
			view.showErrorMessage("Can only upload data to the most recent table/view version.");
		}

	}

	/**
	 * Called after all pre-flight checks for upload has passed.
	 */
	private void postCheckonUploadTableData() {
		Table table = (Table) entityBundle.getEntity();
		getUploadTableModalWidget().configure(table.getParentId(), tableId);
		getUploadTableModalWidget().showModal(new WizardCallback() {
			@Override
			public void onFinished() {
				// SWC-3488: successfully uploaded data to table/view. The current query may be invalid, so rerun
				// with default query.
				setQuery(getDefaultQuery(), false);
			}

			@Override
			public void onCanceled() {}
		});
	}

	@Override
	public void onStartingNewQuery(Query newQuery) {
		setQuery(newQuery, true);
	}

	@Override
	public void onShowQuery() {
		// show the sql executed
		AsyncCallback<String> callback = new AsyncCallback<String>() {
			@Override
			public void onSuccess(String sql) {
				getCopyTextModal().setText(sql);
				getCopyTextModal().show();
			}

			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		};
		generateSqlWithFacets(callback);
	}

	@Override
	public void onShowDownloadFilesProgrammatically() {
		AsyncCallback<String> callback = new AsyncCallback<String>() {
			@Override
			public void onSuccess(String sql) {
				String escapedSql = sql.replace("\"", "\\\"");
				fileViewClientsHelp.setQuery(escapedSql);
				fileViewClientsHelp.show();
			}

			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		};
		generateSqlWithFacets(callback);
	}

	@Override
	public void onAddToDownloadList() {
		AddToDownloadListV2 newAddToDownloadList = ginInjector.getAddToDownloadListV2();
		view.setAddToDownloadList(newAddToDownloadList);
		newAddToDownloadList.configure(entityBundle.getEntity().getId(), currentQuery);
	}

	@Override
	public DatasetEditorProps getItemsEditorProps() {
		DatasetEditorProps props =
				DatasetEditorProps.create(
						entityBundle.getEntity().getId(),
						() -> {
							ToastMessageOptions toastOptions = new ToastMessageOptions.Builder()
									.setTitle("Dataset Saved")
									.setPrimaryButton("Show Schema", () -> this.actionMenu.onAction(Action.SHOW_TABLE_SCHEMA))
									.build();
							DisplayUtils.notify("Edit the Dataset Schema to add additional annotation columns to this dataset", DisplayUtils.NotificationVariant.SUCCESS, toastOptions);
							eventBus.fireEvent(new EntityUpdatedEvent());
							closeItemsEditor();
						},
						() -> closeItemsEditor()
				);
		return props;
	}

	private void showDatasetItemsEditor() {
		actionMenu.setActionVisible(Action.EDIT_DATASET_ITEMS, false);
		view.setItemsEditorVisible(true);
		view.setQueryResultsVisible(false);
		view.setTableMessageVisible(false);
	}

	public void closeItemsEditor() {
		actionMenu.setActionVisible(Action.EDIT_DATASET_ITEMS, true);
		view.setItemsEditorVisible(false);
		view.setQueryResultsVisible(true);
		reconfigureState();
	}
}
