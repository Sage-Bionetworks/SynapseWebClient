package org.sagebionetworks.web.client.widget.table.v2;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import org.gwtbootstrap3.client.ui.constants.AlertType;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityTypeUtils;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.repo.model.table.TableBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cache.SessionStorage;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.CopyTextModal;
import org.sagebionetworks.web.client.widget.clienthelp.FileViewClientsHelp;
import org.sagebionetworks.web.client.widget.entity.controller.PreflightController;
import org.sagebionetworks.web.client.widget.entity.file.AddToDownloadList;
import org.sagebionetworks.web.client.widget.entity.menu.v2.Action;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.table.QueryChangeHandler;
import org.sagebionetworks.web.client.widget.table.modal.download.DownloadTableQueryModalWidget;
import org.sagebionetworks.web.client.widget.table.modal.fileview.TableType;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadTableModalWidget;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget.WizardCallback;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryInputListener;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryResultsListener;
import org.sagebionetworks.web.client.widget.table.v2.results.TableQueryResultWidget;
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
public class TableEntityWidget implements IsWidget, TableEntityWidgetView.Presenter, QueryResultsListener, QueryInputListener {

	public static final String IS_INVOKING_DOWNLOAD_TABLE = "isInvokingDownloadTable";
	public static final String NO_FACETS_SIMPLE_SEARCH_UNSUPPORTED = "In order to use simple search, you must first set columns to be facets in the schema editor.";
	public static final String RESET_SEARCH_QUERY_MESSAGE = "The search query will be reset. Are you sure that you would like to switch to simple search mode?";

	public static final String RESET_SEARCH_QUERY = "Reset search query?";
	public static final long DEFAULT_OFFSET = 0L;
	public static final String SELECT_FROM = "SELECT * FROM ";
	public static final String NO_COLUMNS_EDITABLE = "This table does not have any columns.  Select 'Schema' to add columns to the this table.";
	public static final String NO_COLUMNS_NOT_EDITABLE = "This table does not have any columns.";
	public static final long DEFAULT_LIMIT = 25;
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
	boolean isShowingSchema, isShowingScope;
	public static final String SHOW = "Show ";
	public static final String HIDE = "Hide ";
	public static final String SCOPE = "Scope of ";
	public static final String SCHEMA = " Schema";
	String entityTypeDisplay;
	PortalGinInjector ginInjector;
	AddToDownloadList addToDownloadList;

	@Inject
	public TableEntityWidget(TableEntityWidgetView view, TableQueryResultWidget queryResultsWidget, QueryInputWidget queryInputWidget, PreflightController preflightController, SynapseClientAsync synapseClient, FileViewClientsHelp fileViewClientsHelp, AddToDownloadList addToDownloadList, PortalGinInjector ginInjector, SessionStorage sessionStorage) {
		this.view = view;
		this.queryResultsWidget = queryResultsWidget;
		this.queryInputWidget = queryInputWidget;
		this.preflightController = preflightController;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.fileViewClientsHelp = fileViewClientsHelp;
		this.addToDownloadList = addToDownloadList;
		this.ginInjector = ginInjector;
		this.sessionStorage = sessionStorage;
		this.view.setPresenter(this);
		this.view.setQueryResultsWidget(this.queryResultsWidget);
		this.view.setQueryInputWidget(this.queryInputWidget);
		view.setAddToDownloadList(addToDownloadList);
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
		queryInputWidget.setDownloadFilesVisible(tableType.isIncludeFiles());
		this.tableId = bundle.getEntity().getId();
		this.tableVersionNumber = versionNumber;
		this.isCurrentVersion = tableVersionNumber == null;
		this.tableBundle = bundle.getTableBundle();
		this.canEdit = canEdit;
		this.canEditResults = canEdit;
		this.queryChangeHandler = qch;
		this.view.configure(bundle, this.canEdit && isCurrentVersion);
		this.actionMenu = actionMenu;
		this.entityTypeDisplay = EntityTypeUtils.getDisplayName(EntityTypeUtils.getEntityTypeForClass(entityBundle.getEntity().getClass()));
		addToDownloadList.clear();
		configureActions();
		checkState();
		initSimpleAdvancedQueryState();
	}

	/**
	 * Setup the actions for this widget.
	 */
	private void configureActions() {
		// Listen to action events.
		isShowingScope = false;
		isShowingSchema = false;
		view.setScopeVisible(false);
		view.setSchemaVisible(false);
		actionMenu.setActionText(Action.SHOW_TABLE_SCHEMA, SHOW + entityTypeDisplay + SCHEMA);
		actionMenu.setActionText(Action.SHOW_VIEW_SCOPE, SHOW + SCOPE + entityTypeDisplay);
		this.actionMenu.setActionListener(Action.UPLOAD_TABLE_DATA, action -> {
			onUploadTableData();
		});
		this.actionMenu.setActionListener(Action.DOWNLOAD_TABLE_QUERY_RESULTS, action -> {
			onDownloadResults();
		});
		this.actionMenu.setActionListener(Action.EDIT_TABLE_DATA, action -> {
			onEditResults();
		});
		this.actionMenu.setActionListener(Action.SHOW_TABLE_SCHEMA, action -> {
			isShowingSchema = !isShowingSchema;
			view.setSchemaVisible(isShowingSchema);
			String showHide = isShowingSchema ? HIDE : SHOW;
			actionMenu.setActionText(Action.SHOW_TABLE_SCHEMA, showHide + entityTypeDisplay + SCHEMA);
		});

		this.actionMenu.setActionListener(Action.SHOW_VIEW_SCOPE, action -> {
			isShowingScope = !isShowingScope;
			view.setScopeVisible(isShowingScope);
			String showHide = isShowingScope ? HIDE : SHOW;
			actionMenu.setActionText(Action.SHOW_VIEW_SCOPE, showHide + SCOPE + entityTypeDisplay);
		});
	}

	/**
	 * 
	 */
	public void checkState() {
		// If there are no columns, then the first thing to do is ask the user
		// to create some columns.
		if (this.tableBundle.getColumnModels().size() < 1) {
			setNoColumnsState();
		} else {
			// There are columns.
			Query startQuery = queryChangeHandler.getQueryString();
			if (startQuery == null) {
				// use a default query
				startQuery = getDefaultQuery();
			}
			setQuery(startQuery, false);
		}
	}

	/**
	 * Set the query used by this widget.
	 * 
	 * @param sql
	 */
	private void setQuery(Query query, boolean isFromResults) {
		this.currentQuery = query;
		this.queryInputWidget.configure(query.getSql(), this, this.canEditResults);
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
		view.setAdvancedSearchLinkVisible(true);
		view.setSimpleSearchLinkVisible(false);
		queryResultsWidget.setFacetsVisible(true);
		queryInputWidget.setShowQueryVisible(true);
		queryInputWidget.setQueryInputVisible(false);
	}

	public void hideFiltering() {
		queryInputWidget.setVisible(false);
		queryResultsWidget.setFacetsVisible(false);
		view.setSimpleSearchLinkVisible(false);
		view.setAdvancedSearchLinkVisible(false);
	}

	private void showAdvancedSearchUI() {
		view.setAdvancedSearchLinkVisible(false);
		// SWC-3762: show the simple search link if facets exist, or if the user can set up facets.
		view.setSimpleSearchLinkVisible(isFacets() || canEdit);
		queryResultsWidget.setFacetsVisible(false);
		queryInputWidget.setShowQueryVisible(false);
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

	@Override
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
		String message = null;
		if (this.canEdit) {
			message = NO_COLUMNS_EDITABLE;
		} else {
			message = NO_COLUMNS_NOT_EDITABLE;
		}
		// There can be no query when there are no columns
		if (this.queryChangeHandler.getQueryString() != null) {
			this.queryChangeHandler.onQueryChange(null);
		}
		view.setQueryInputVisible(false);
		view.setQueryResultsVisible(false);
		view.showTableMessage(AlertType.INFO, message);
		view.setTableMessageVisible(true);
	}

	/**
	 * Build the default query based on the current table data.
	 * 
	 * @return
	 */
	public Query getDefaultQuery() {
		StringBuilder builder = new StringBuilder();
		builder.append(SELECT_FROM);
		builder.append(this.tableId);
		if (!isCurrentVersion) {
			builder.append("." + this.tableVersionNumber);
		}
		Query query = new Query();
		query.setIncludeEntityEtag(true);
		query.setSql(builder.toString());
		query.setOffset(DEFAULT_OFFSET);
		query.setLimit(DEFAULT_LIMIT);
		query.setIsConsistent(true);
		return query;
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
		Entity table = entityBundle.getEntity();
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
		addToDownloadList.addToDownloadList(entityBundle.getEntity().getId(), currentQuery);
	}
}
