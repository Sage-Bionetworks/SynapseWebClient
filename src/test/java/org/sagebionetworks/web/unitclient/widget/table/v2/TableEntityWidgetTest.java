package org.sagebionetworks.web.unitclient.widget.table.v2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.widget.table.v2.TableEntityWidget.HIDE;
import static org.sagebionetworks.web.client.widget.table.v2.TableEntityWidget.SCHEMA;
import static org.sagebionetworks.web.client.widget.table.v2.TableEntityWidget.SCOPE;
import static org.sagebionetworks.web.client.widget.table.v2.TableEntityWidget.SHOW;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.gwtbootstrap3.client.ui.constants.AlertType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.repo.model.table.FacetColumnRequest;
import org.sagebionetworks.repo.model.table.FacetColumnValuesRequest;
import org.sagebionetworks.repo.model.table.FacetType;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.repo.model.table.TableBundle;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.repo.model.table.ViewType;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cache.SessionStorage;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.CopyTextModal;
import org.sagebionetworks.web.client.widget.clienthelp.FileViewClientsHelp;
import org.sagebionetworks.web.client.widget.entity.controller.PreflightController;
import org.sagebionetworks.web.client.widget.entity.file.AddToDownloadList;
import org.sagebionetworks.web.client.widget.entity.menu.v2.Action;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget.ActionListener;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.table.QueryChangeHandler;
import org.sagebionetworks.web.client.widget.table.modal.download.DownloadTableQueryModalWidget;
import org.sagebionetworks.web.client.widget.table.modal.fileview.TableType;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadTableModalWidget;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget.WizardCallback;
import org.sagebionetworks.web.client.widget.table.v2.QueryInputWidget;
import org.sagebionetworks.web.client.widget.table.v2.TableEntityWidget;
import org.sagebionetworks.web.client.widget.table.v2.TableEntityWidgetView;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryResultsListener;
import org.sagebionetworks.web.client.widget.table.v2.results.TableQueryResultWidget;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Business logic tests for the TableEntityWidget
 * 
 * @author John
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class TableEntityWidgetTest {

	AdapterFactory adapterFactory;
	List<ColumnModel> columns;
	TableBundle tableBundle;
	TableEntity tableEntity;
	@Mock
	ActionMenuWidget mockActionMenu;
	@Mock
	DownloadTableQueryModalWidget mockDownloadTableQueryModalWidget;
	@Mock
	UploadTableModalWidget mockUploadTableModalWidget;
	@Mock
	PreflightController mockPreflightController;
	@Mock
	TableEntityWidgetView mockView;
	@Mock
	QueryChangeHandler mockQueryChangeHandler;
	@Mock
	TableQueryResultWidget mockQueryResultsWidget;
	@Mock
	QueryInputWidget mockQueryInputWidget;
	TableEntityWidget widget;
	EntityBundle entityBundle;
	Long versionNumber;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	CopyTextModal mockCopyTextModal;

	@Captor
	ArgumentCaptor<Callback> callbackCaptor;
	@Captor
	ArgumentCaptor<Query> queryCaptor;

	public static final String FACET_SQL = "select * from syn123 where \"x\" = 'a'";
	public static final String EXPECTED_SQL_FOR_CLIENT = "select * from syn123 where \\\"x\\\" = 'a'";
	@Mock
	FileViewClientsHelp mockFileViewClientsHelp;
	@Mock
	PortalGinInjector mockPortalGinInjector;
	@Mock
	AddToDownloadList mockAddToDownloadList;
	@Mock
	SessionStorage mockSessionStorage;
	@Mock
	AuthenticationController mockAuthController;
	@Captor
	ArgumentCaptor<ActionListener> actionListenerCaptor;

	JSONObjectAdapterImpl portalJson = new JSONObjectAdapterImpl();

	@Before
	public void before() {
		// stubs
		adapterFactory = new AdapterFactoryImpl();
		columns = TableModelTestUtils.createOneOfEachType();
		tableEntity = new TableEntity();
		tableEntity.setId("syn123");
		tableEntity.setColumnIds(TableModelTestUtils.getColumnModelIds(columns));
		tableBundle = new TableBundle();
		tableBundle.setMaxRowsPerPage(4L);
		tableBundle.setColumnModels(columns);
		when(mockPortalGinInjector.getDownloadTableQueryModalWidget()).thenReturn(mockDownloadTableQueryModalWidget);
		when(mockPortalGinInjector.getUploadTableModalWidget()).thenReturn(mockUploadTableModalWidget);
		when(mockPortalGinInjector.getCopyTextModal()).thenReturn(mockCopyTextModal);
		when(mockPortalGinInjector.getAuthenticationController()).thenReturn(mockAuthController);

		widget = new TableEntityWidget(mockView, mockQueryResultsWidget, mockQueryInputWidget, mockPreflightController, mockSynapseClient, mockFileViewClientsHelp, mockAddToDownloadList, mockPortalGinInjector, mockSessionStorage);

		AsyncMockStubber.callSuccessWith(FACET_SQL).when(mockSynapseClient).generateSqlWithFacets(anyString(), anyList(), anyList(), any(AsyncCallback.class));
		// The test bundle
		entityBundle = new EntityBundle();
		entityBundle.setEntity(tableEntity);
		entityBundle.setTableBundle(tableBundle);

		String sql = "SELECT * FROM " + tableEntity.getId() + " LIMIT 3 OFFSET 0";
		Query query = new Query();
		query.setSql(sql);
		when(mockQueryChangeHandler.getQueryString()).thenReturn(query);
		Header.isShowingPortalAlert = false;
		Header.portalAlertJson = null;
	}

	private void configureBundleWithView(ViewType viewType) {
		EntityView view = new EntityView();
		view.setId("syn456");
		view.setColumnIds(TableModelTestUtils.getColumnModelIds(columns));
		view.setType(viewType);
		entityBundle.setEntity(view);
	}

	@Test
	public void testGetDefaultPageSizeMaxUnder() {
		tableBundle.setMaxRowsPerPage(4L);
		// Configure with the default values
		widget.configure(entityBundle, versionNumber, true, mockQueryChangeHandler, mockActionMenu);
		// since the size from the bundle is less than the default,
		// the value used should be 3/4ths of the max allowed for the schema.
		assertEquals(3l, widget.getDefaultPageSize());
	}

	@Test
	public void testGetDefaultPageSizeMaxOver() {
		tableBundle.setMaxRowsPerPage(TableEntityWidget.DEFAULT_LIMIT * 2L);
		// Configure with the default values
		widget.configure(entityBundle, versionNumber, true, mockQueryChangeHandler, mockActionMenu);
		// since the size from the bundle is greater than the default
		// the default should be used.
		assertEquals(TableEntityWidget.DEFAULT_LIMIT, widget.getDefaultPageSize());
	}

	@Test
	public void testGetDefaultPageSizeNull() {
		tableBundle.setMaxRowsPerPage(null);
		// Configure with the default values
		widget.configure(entityBundle, versionNumber, true, mockQueryChangeHandler, mockActionMenu);
		// when null the default should be used.
		assertEquals(TableEntityWidget.DEFAULT_LIMIT, widget.getDefaultPageSize());
	}

	@Test
	public void testDefaultQueryString() {
		tableBundle.setMaxRowsPerPage(4L);
		widget.configure(entityBundle, versionNumber, true, mockQueryChangeHandler, mockActionMenu);
		String expected = "SELECT * FROM " + tableEntity.getId();
		Query query = new Query();
		query.setSql(expected);
		query.setIsConsistent(true);
		query.setIncludeEntityEtag(true);
		query.setLimit(TableEntityWidget.DEFAULT_LIMIT);
		query.setOffset(TableEntityWidget.DEFAULT_OFFSET);
		assertEquals(query, widget.getDefaultQuery());
	}

	@Test
	public void testConfigureNotNullDefaultQuery() {
		tableBundle.setMaxRowsPerPage(4L);
		// This time we pass a query
		String sql = "SELECT * FROM " + tableEntity.getId() + " LIMIT 3 OFFSET 0";
		Query query = new Query();
		query.setSql(sql);
		when(mockQueryChangeHandler.getQueryString()).thenReturn(query);
		widget.configure(entityBundle, versionNumber, true, mockQueryChangeHandler, mockActionMenu);
		// The widget must not change the query when it is passed in.
		verify(mockQueryChangeHandler, never()).onQueryChange(any(Query.class));
	}

	@Test
	public void testConfigureEdit() {
		boolean canEdit = true;
		widget.configure(entityBundle, versionNumber, canEdit, mockQueryChangeHandler, mockActionMenu);

		// download files help not visible for Table, only Views
		verify(mockQueryInputWidget).setDownloadFilesVisible(false);
	}

	@Test
	public void testConfigureViewEdit() {
		boolean canEdit = true;
		configureBundleWithView(ViewType.file);

		widget.configure(entityBundle, versionNumber, canEdit, mockQueryChangeHandler, mockActionMenu);
		// verify download help is shown for file views
		verify(mockQueryInputWidget).setDownloadFilesVisible(true);
	}

	@Test
	public void testNoColumnsWithEdit() {
		entityBundle.getTableBundle().setColumnModels(new LinkedList<ColumnModel>());
		boolean canEdit = true;
		widget.configure(entityBundle, versionNumber, canEdit, mockQueryChangeHandler, mockActionMenu);
		verify(mockView).setQueryInputVisible(false);
		verify(mockView).setQueryResultsVisible(false);
		verify(mockView).setTableMessageVisible(true);
		verify(mockView).showTableMessage(AlertType.INFO, TableEntityWidget.NO_COLUMNS_EDITABLE);
		// The query should be cleared when there are no columns
		verify(mockQueryChangeHandler).onQueryChange(null);
	}

	@Test
	public void testNoColumnsWithWihtouEdit() {
		entityBundle.getTableBundle().setColumnModels(new LinkedList<ColumnModel>());
		boolean canEdit = false;
		widget.configure(entityBundle, versionNumber, canEdit, mockQueryChangeHandler, mockActionMenu);
		verify(mockView).setQueryInputVisible(false);
		verify(mockView).setQueryResultsVisible(false);
		verify(mockView).setTableMessageVisible(true);
		verify(mockView).showTableMessage(AlertType.INFO, TableEntityWidget.NO_COLUMNS_NOT_EDITABLE);
		// The query should be cleared when there are no columns
		verify(mockQueryChangeHandler).onQueryChange(null);
	}

	@Test
	public void testQueryExecutionStarted() {
		boolean canEdit = true;
		widget.configure(entityBundle, versionNumber, canEdit, mockQueryChangeHandler, mockActionMenu);
		widget.queryExecutionStarted();
		verify(mockQueryInputWidget).queryExecutionStarted();
		verify(mockActionMenu).setActionVisible(Action.EDIT_TABLE_DATA, false);
		verify(mockActionMenu).setActionVisible(Action.DOWNLOAD_TABLE_QUERY_RESULTS, false);
	}

	@Test
	public void testQueryExecutionFinishedSuccess() {
		boolean canEdit = true;
		boolean wasExecutionSuccess = true;
		boolean resultsEditable = true;
		Query startQuery = new Query();
		startQuery.setSql("select * from syn123");
		when(mockQueryChangeHandler.getQueryString()).thenReturn(startQuery);
		widget.configure(entityBundle, versionNumber, canEdit, mockQueryChangeHandler, mockActionMenu);
		reset(mockActionMenu);
		widget.queryExecutionFinished(wasExecutionSuccess, resultsEditable);
		verify(mockQueryInputWidget).queryExecutionFinished(wasExecutionSuccess, resultsEditable);
		verify(mockQueryChangeHandler).onQueryChange(startQuery);
		verify(mockActionMenu).setActionVisible(Action.EDIT_TABLE_DATA, true);
		verify(mockActionMenu).setActionVisible(Action.DOWNLOAD_TABLE_QUERY_RESULTS, true);
	}

	@Test
	public void testViewQueryExecutionFinishedSuccess() {
		configureBundleWithView(ViewType.file);
		boolean canEdit = true;
		boolean wasExecutionSuccess = true;
		boolean resultsEditable = true;
		Query startQuery = new Query();
		startQuery.setSql("select * from syn123");
		when(mockQueryChangeHandler.getQueryString()).thenReturn(startQuery);
		widget.configure(entityBundle, versionNumber, canEdit, mockQueryChangeHandler, mockActionMenu);
		reset(mockActionMenu);
		widget.queryExecutionFinished(wasExecutionSuccess, resultsEditable);
		verify(mockQueryInputWidget).queryExecutionFinished(wasExecutionSuccess, resultsEditable);
		verify(mockQueryChangeHandler).onQueryChange(startQuery);
		verify(mockActionMenu).setActionVisible(Action.EDIT_TABLE_DATA, true);
		verify(mockActionMenu).setActionVisible(Action.DOWNLOAD_TABLE_QUERY_RESULTS, true);
	}

	@Test
	public void testQueryExecutionFinishedSuccessNoEdit() {
		boolean canEdit = false;
		boolean wasExecutionSuccess = true;
		boolean resultsEditable = true;
		Query startQuery = new Query();
		startQuery.setSql("select * from syn123");
		when(mockQueryChangeHandler.getQueryString()).thenReturn(startQuery);
		widget.configure(entityBundle, versionNumber, canEdit, mockQueryChangeHandler, mockActionMenu);
		reset(mockActionMenu);
		widget.queryExecutionFinished(wasExecutionSuccess, resultsEditable);
		verify(mockQueryInputWidget).queryExecutionFinished(wasExecutionSuccess, resultsEditable);
		verify(mockQueryChangeHandler).onQueryChange(startQuery);
		verify(mockActionMenu, never()).setActionVisible(Action.EDIT_TABLE_DATA, true);
		// do not need edit access to download the query results
		verify(mockActionMenu).setActionVisible(Action.DOWNLOAD_TABLE_QUERY_RESULTS, true);
	}

	@Test
	public void testQueryExecutionFinishedFailed() {
		boolean canEdit = true;
		boolean wasExecutionSuccess = false;
		boolean resultsEditable = true;
		Query startQuery = new Query();
		startQuery.setSql("select * from syn123");
		when(mockQueryChangeHandler.getQueryString()).thenReturn(startQuery);
		widget.configure(entityBundle, versionNumber, canEdit, mockQueryChangeHandler, mockActionMenu);
		reset(mockActionMenu);
		widget.queryExecutionFinished(wasExecutionSuccess, resultsEditable);
		verify(mockQueryInputWidget).queryExecutionFinished(wasExecutionSuccess, resultsEditable);
		verify(mockQueryChangeHandler, never()).onQueryChange(any(Query.class));
		verify(mockActionMenu, never()).setActionVisible(Action.EDIT_TABLE_DATA, true);
		verify(mockActionMenu, never()).setActionVisible(Action.DOWNLOAD_TABLE_QUERY_RESULTS, true);
	}

	@Test
	public void testOnExecuteQuery() {
		TableType tableType = TableType.table;
		boolean canEdit = true;
		// Start with a query that is not on the first page
		Query startQuery = new Query();
		startQuery.setSql("select * from syn123");
		startQuery.setLimit(100L);
		startQuery.setOffset(101L);
		when(mockQueryChangeHandler.getQueryString()).thenReturn(startQuery);
		widget.configure(entityBundle, versionNumber, canEdit, mockQueryChangeHandler, mockActionMenu);
		// Start query get passed to the results
		verify(mockQueryResultsWidget).configure(startQuery, canEdit, tableType, widget);
		reset(mockQueryResultsWidget);
		// Set new sql
		String newSQL = "select 1,2,3 from syn123";
		widget.onExecuteQuery(newSQL);
		// Limit and offset should be back to default, and the new SQL included.
		Query expected = new Query();
		expected.setSql(newSQL);
		expected.setLimit(TableEntityWidget.DEFAULT_LIMIT);
		expected.setOffset(TableEntityWidget.DEFAULT_OFFSET);
		verify(mockQueryResultsWidget).configure(expected, canEdit, tableType, widget);
	}

	@Test
	public void testOnExecuteViewQuery() {
		TableType tableType = TableType.projects;
		configureBundleWithView(ViewType.project);
		boolean canEdit = true;
		// Start with a query that is not on the first page
		Query startQuery = new Query();
		startQuery.setSql("select * from syn123");
		startQuery.setLimit(100L);
		startQuery.setOffset(101L);
		when(mockQueryChangeHandler.getQueryString()).thenReturn(startQuery);
		widget.configure(entityBundle, versionNumber, canEdit, mockQueryChangeHandler, mockActionMenu);
		// Start query get passed to the results
		verify(mockQueryResultsWidget).configure(startQuery, canEdit, tableType, widget);
	}

	@Test
	public void testShowSchema() {
		boolean canEdit = true;
		widget.configure(entityBundle, versionNumber, canEdit, mockQueryChangeHandler, mockActionMenu);
		verify(mockActionMenu).setActionListener(eq(Action.SHOW_TABLE_SCHEMA), actionListenerCaptor.capture());
		ActionListener listener = actionListenerCaptor.getValue();
		verify(mockView).setSchemaVisible(false);
		listener.onAction(Action.SHOW_TABLE_SCHEMA);
		verify(mockView).setSchemaVisible(true);
		verify(mockActionMenu).setActionText(Action.SHOW_TABLE_SCHEMA, HIDE + "Table" + SCHEMA);
		listener.onAction(Action.SHOW_TABLE_SCHEMA);
		verify(mockView, times(2)).setSchemaVisible(false);
		verify(mockActionMenu, times(2)).setActionText(Action.SHOW_TABLE_SCHEMA, SHOW + "Table" + SCHEMA);
	}

	@Test
	public void testShowViewScope() {
		boolean canEdit = true;
		widget.configure(entityBundle, versionNumber, canEdit, mockQueryChangeHandler, mockActionMenu);
		verify(mockActionMenu).setActionListener(eq(Action.SHOW_VIEW_SCOPE), actionListenerCaptor.capture());
		ActionListener listener = actionListenerCaptor.getValue();
		verify(mockView).setScopeVisible(false);
		listener.onAction(Action.SHOW_VIEW_SCOPE);
		verify(mockView).setScopeVisible(true);
		verify(mockActionMenu).setActionText(Action.SHOW_VIEW_SCOPE, HIDE + SCOPE + "Table");
		listener.onAction(Action.SHOW_VIEW_SCOPE);
		verify(mockView, times(2)).setScopeVisible(false);
		verify(mockActionMenu, times(2)).setActionText(Action.SHOW_VIEW_SCOPE, SHOW + SCOPE + "Table");
	}

	@Test
	public void testUploadTableCSVPreflightFailed() {
		AsyncMockStubber.callNoInvovke().when(mockPreflightController).checkUploadToEntity(any(EntityBundle.class), any(Callback.class));
		boolean canEdit = true;
		widget.configure(entityBundle, versionNumber, canEdit, mockQueryChangeHandler, mockActionMenu);
		widget.onUploadTableData();
		// should not proceed to upload.
		verify(mockUploadTableModalWidget, never()).showModal(any(WizardCallback.class));
	}

	@Test
	public void testUploadTableCSVPreflightPassed() {
		AsyncMockStubber.callWithInvoke().when(mockPreflightController).checkUploadToEntity(any(EntityBundle.class), any(Callback.class));
		boolean canEdit = true;
		widget.configure(entityBundle, versionNumber, canEdit, mockQueryChangeHandler, mockActionMenu);
		widget.onUploadTableData();
		// proceed to upload
		verify(mockUploadTableModalWidget).showModal(any(WizardCallback.class));
	}

	@Test
	public void testEditTablePreflightFailed() {
		AsyncMockStubber.callNoInvovke().when(mockPreflightController).checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
		boolean canEdit = true;
		widget.configure(entityBundle, versionNumber, canEdit, mockQueryChangeHandler, mockActionMenu);
		widget.onEditResults();
		// should not proceed to edit
		verify(mockQueryResultsWidget, never()).onEditRows();
	}

	@Test
	public void testEditTablePreflightPassed() {
		AsyncMockStubber.callWithInvoke().when(mockPreflightController).checkUploadToEntity(any(EntityBundle.class), any(Callback.class));
		boolean canEdit = true;
		widget.configure(entityBundle, versionNumber, canEdit, mockQueryChangeHandler, mockActionMenu);
		widget.onEditResults();
		// proceed to edit
		verify(mockQueryResultsWidget).onEditRows();
	}

	@Test
	public void testOnStartingnewQuery() {
		boolean canEdit = true;
		// Start with a query that is not on the first page
		Query startQuery = new Query();
		startQuery.setSql("select * from syn123");
		startQuery.setLimit(100L);
		startQuery.setOffset(101L);
		when(mockQueryChangeHandler.getQueryString()).thenReturn(startQuery);
		widget.configure(entityBundle, versionNumber, canEdit, mockQueryChangeHandler, mockActionMenu);
		reset(mockQueryResultsWidget);
		// Set new sql
		Query newQuery = new Query();
		newQuery.setSql("select 1,2,3 from syn123");
		widget.onStartingNewQuery(newQuery);
		// Should get passed to the input widget
		verify(mockQueryInputWidget).configure(newQuery.getSql(), widget, canEdit);
		// Should not be sent to the results as that is where it came from.
		verify(mockQueryResultsWidget, never()).configure(any(Query.class), anyBoolean(), any(TableType.class), any(QueryResultsListener.class));
	}


	@Test
	public void testCanEditViewResults() {
		configureBundleWithView(ViewType.file);
		// we can edit the view, but verify that the query input widget should be told that editing results
		// is not possible
		boolean canEdit = true;
		// Start with a query that is not on the first page
		Query startQuery = new Query();
		startQuery.setSql("select * from syn123");
		startQuery.setLimit(100L);
		startQuery.setOffset(101L);
		when(mockQueryChangeHandler.getQueryString()).thenReturn(startQuery);
		widget.configure(entityBundle, versionNumber, canEdit, mockQueryChangeHandler, mockActionMenu);

		verify(mockQueryInputWidget).configure(startQuery.getSql(), widget, canEdit);
	}


	private void verifySimpleSearchUI() {
		verify(mockView).setAdvancedSearchLinkVisible(true);
		verify(mockView).setSimpleSearchLinkVisible(false);
		verify(mockQueryResultsWidget).setFacetsVisible(true);
		verify(mockQueryInputWidget).setShowQueryVisible(true);
		verify(mockQueryInputWidget).setQueryInputVisible(false);
	}

	private void verifyAdvancedSearchUI() {
		verify(mockView).setAdvancedSearchLinkVisible(false);
		verify(mockView).setSimpleSearchLinkVisible(true);
		verify(mockQueryResultsWidget).setFacetsVisible(false);
		verify(mockQueryInputWidget).setShowQueryVisible(false);
		verify(mockQueryInputWidget).setQueryInputVisible(true);
	}

	@Test
	public void testInitSimpleSearchUI() {
		configureBundleWithView(ViewType.file);
		// mark a column as being faceted
		columns.get(0).setFacetType(FacetType.enumeration);
		boolean canEdit = false;
		Query startQuery = new Query();
		startQuery.setSql("select * from syn123");
		FacetColumnRequest facetColumnRequest = new FacetColumnValuesRequest();
		facetColumnRequest.setColumnName("col1");
		((FacetColumnValuesRequest) facetColumnRequest).setFacetValues(Collections.singleton("a"));
		startQuery.setSelectedFacets(Collections.singletonList(facetColumnRequest));
		when(mockQueryChangeHandler.getQueryString()).thenReturn(startQuery);
		widget.configure(entityBundle, versionNumber, canEdit, mockQueryChangeHandler, mockActionMenu);

		verifySimpleSearchUI();

		// show query
		// verify facet select info sql is used
		widget.onShowQuery();

		verify(mockCopyTextModal).setText(FACET_SQL);
		verify(mockCopyTextModal).show();

		reset(mockQueryResultsWidget);
		// change to advanced (verify sql that has facet selection info sql used)
		widget.onShowAdvancedSearch();
		verifyAdvancedSearchUI();
		// SWC-4275: query results widget is not reconfigured when switching to advanced mode (since
		// currently shown results are the same)
		verify(mockQueryResultsWidget, never()).configure(any(Query.class), anyBoolean(), eq(TableType.files), eq(widget));
	}

	@Test
	public void testInitAdvancedQueryStateUI1() {
		// simple query, no facets, but user can edit
		configureBundleWithView(ViewType.file);
		boolean canEdit = true;
		Query startQuery = new Query();
		startQuery.setSql("select * from syn123");
		when(mockQueryChangeHandler.getQueryString()).thenReturn(startQuery);
		widget.configure(entityBundle, versionNumber, canEdit, mockQueryChangeHandler, mockActionMenu);

		verifyAdvancedSearchUI();

		// try to flip to simple search
		widget.onShowSimpleSearch();
		verify(mockView).showErrorMessage(TableEntityWidget.NO_FACETS_SIMPLE_SEARCH_UNSUPPORTED);
	}

	@Test
	public void testInitAdvancedQueryStateUI2() {
		// facet, but not a simple query
		configureBundleWithView(ViewType.project);
		columns.get(0).setFacetType(FacetType.enumeration);
		boolean canEdit = false;
		Query startQuery = new Query();
		startQuery.setSql("select * from syn123 where x='1'");
		when(mockQueryChangeHandler.getQueryString()).thenReturn(startQuery);
		widget.configure(entityBundle, versionNumber, canEdit, mockQueryChangeHandler, mockActionMenu);

		verifyAdvancedSearchUI();

		// now test the confirmation when switching to simple search mode
		widget.onShowSimpleSearch();
		verify(mockView).showConfirmDialog(eq(TableEntityWidget.RESET_SEARCH_QUERY), eq(TableEntityWidget.RESET_SEARCH_QUERY_MESSAGE), callbackCaptor.capture());

		startQuery.setSql(" select pos, count(*) as c from syn123 group BY pos    ");
		widget.onShowSimpleSearch();
		verify(mockView, times(2)).showConfirmDialog(eq(TableEntityWidget.RESET_SEARCH_QUERY), eq(TableEntityWidget.RESET_SEARCH_QUERY_MESSAGE), callbackCaptor.capture());

		startQuery.setSql(" select * FROM syn123 LIMIT 2 offset 1 ");
		widget.onShowSimpleSearch();
		verify(mockView, times(3)).showConfirmDialog(eq(TableEntityWidget.RESET_SEARCH_QUERY), eq(TableEntityWidget.RESET_SEARCH_QUERY_MESSAGE), callbackCaptor.capture());

		// on confirmation, show simple search ui
		callbackCaptor.getValue().invoke();
		verifySimpleSearchUI();
		// reset query
		verify(mockQueryResultsWidget).configure(startQuery, canEdit, TableType.projects, widget);
	}

	@Test
	public void testInitAdvancedQueryStateUI3() {
		// facet, but not a simple query
		configureBundleWithView(ViewType.file);
		columns.get(0).setFacetType(FacetType.enumeration);
		boolean canEdit = false;
		Query startQuery = new Query();
		startQuery.setSql("select * from syn123 where x='1'");
		when(mockQueryChangeHandler.getQueryString()).thenReturn(startQuery);
		widget.configure(entityBundle, versionNumber, canEdit, mockQueryChangeHandler, mockActionMenu);

		verifyAdvancedSearchUI();

		// simple query
		startQuery.setSql("select * from syn123");

		// no confirmation necessary
		widget.onShowSimpleSearch();
		verify(mockView, never()).showConfirmDialog(eq(TableEntityWidget.RESET_SEARCH_QUERY), eq(TableEntityWidget.RESET_SEARCH_QUERY_MESSAGE), any(Callback.class));
		verifySimpleSearchUI();
	}

	@Test
	public void testInitAdvancedQueryStateUI4() {
		// simple query, no facets, and user can't edit
		configureBundleWithView(ViewType.file);
		boolean canEdit = false;
		Query startQuery = new Query();
		startQuery.setSql("select * from syn123");
		when(mockQueryChangeHandler.getQueryString()).thenReturn(startQuery);
		widget.configure(entityBundle, versionNumber, canEdit, mockQueryChangeHandler, mockActionMenu);

		// verifyAdvancedSearchUI, but simple search link is not shown
		verify(mockView).setAdvancedSearchLinkVisible(false);
		verify(mockView).setSimpleSearchLinkVisible(false);
		verify(mockQueryResultsWidget).setFacetsVisible(false);
		verify(mockQueryInputWidget).setShowQueryVisible(false);
		verify(mockQueryInputWidget).setQueryInputVisible(true);
	}

	@Test
	public void testOnShowDownloadFiles() {
		Query startQuery = new Query();
		startQuery.setSql(FACET_SQL);
		when(mockQueryChangeHandler.getQueryString()).thenReturn(startQuery);
		widget.configure(entityBundle, versionNumber, true, mockQueryChangeHandler, mockActionMenu);

		widget.onShowDownloadFilesProgrammatically();

		verify(mockFileViewClientsHelp).setQuery(EXPECTED_SQL_FOR_CLIENT);
		verify(mockFileViewClientsHelp).show();
	}

	@Test
	public void testHideFiltering() {
		widget.configure(entityBundle, versionNumber, true, mockQueryChangeHandler, mockActionMenu);

		widget.hideFiltering();

		verify(mockQueryInputWidget).setVisible(false);
		verify(mockQueryResultsWidget, atLeastOnce()).setFacetsVisible(false);
		verify(mockView).setSimpleSearchLinkVisible(false);
		verify(mockView, atLeastOnce()).setAdvancedSearchLinkVisible(false);
	}

	@Test
	public void testAutoAddToDownloadList() throws JSONObjectAdapterException {
		when(mockAuthController.isLoggedIn()).thenReturn(true);
		configureBundleWithView(ViewType.file);
		when(mockQueryChangeHandler.getQueryString()).thenReturn(new Query());
		Header.isShowingPortalAlert = true;
		Header.portalAlertJson = portalJson;
		portalJson.put(TableEntityWidget.IS_INVOKING_DOWNLOAD_TABLE, true);

		widget.configure(entityBundle, versionNumber, true, mockQueryChangeHandler, mockActionMenu);
		widget.queryExecutionFinished(true, false);

		verify(mockAddToDownloadList).addToDownloadList(anyString(), any(Query.class));
		assertFalse(portalJson.getBoolean(TableEntityWidget.IS_INVOKING_DOWNLOAD_TABLE));
	}

	@Test
	public void testAutoAddToDownloadListFalse() throws JSONObjectAdapterException {
		when(mockAuthController.isLoggedIn()).thenReturn(true);
		configureBundleWithView(ViewType.file);
		when(mockQueryChangeHandler.getQueryString()).thenReturn(new Query());
		Header.isShowingPortalAlert = true;
		Header.portalAlertJson = portalJson;
		portalJson.put(TableEntityWidget.IS_INVOKING_DOWNLOAD_TABLE, false);

		widget.configure(entityBundle, versionNumber, true, mockQueryChangeHandler, mockActionMenu);
		widget.queryExecutionFinished(true, false);

		verify(mockAddToDownloadList, never()).addToDownloadList(anyString(), any(Query.class));
	}

	@Test
	public void testAutoAddToDownloadListNotLoggedIn() throws JSONObjectAdapterException {
		when(mockAuthController.isLoggedIn()).thenReturn(false);
		configureBundleWithView(ViewType.file);
		when(mockQueryChangeHandler.getQueryString()).thenReturn(new Query());
		Header.isShowingPortalAlert = true;
		Header.portalAlertJson = portalJson;
		portalJson.put(TableEntityWidget.IS_INVOKING_DOWNLOAD_TABLE, true);

		widget.configure(entityBundle, versionNumber, true, mockQueryChangeHandler, mockActionMenu);
		widget.queryExecutionFinished(true, false);

		verify(mockAddToDownloadList, never()).addToDownloadList(anyString(), any(Query.class));
	}
}
