package org.sagebionetworks.web.unitclient.widget.table.v2;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.gwtbootstrap3.client.ui.constants.AlertType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.repo.model.table.FacetColumnRequest;
import org.sagebionetworks.repo.model.table.FacetColumnValuesRequest;
import org.sagebionetworks.repo.model.table.FacetType;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.repo.model.table.TableBundle;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.CopyTextModal;
import org.sagebionetworks.web.client.widget.entity.controller.PreflightController;
import org.sagebionetworks.web.client.widget.entity.menu.v2.Action;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.table.QueryChangeHandler;
import org.sagebionetworks.web.client.widget.table.modal.download.DownloadTableQueryModalWidget;
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
 * @author John
 *
 */
public class TableEntityWidgetTest {

	AdapterFactory adapterFactory;
	List<ColumnModel> columns;
	TableBundle tableBundle;
	TableEntity tableEntity;
	ActionMenuWidget mockActionMenu;
	DownloadTableQueryModalWidget mockDownloadTableQueryModalWidget;
	UploadTableModalWidget mockUploadTableModalWidget;
	PreflightController mockPreflightController;
	TableEntityWidgetView mockView;
	QueryChangeHandler mockQueryChangeHandler;
	TableQueryResultWidget mockQueryResultsWidget;
	QueryInputWidget mockQueryInputWidget;
	TableEntityWidget widget;
	EntityBundle entityBundle;
	SynapseClientAsync mockSynapseClient;
	@Mock
	CopyTextModal mockCopyTextModal;
	
	@Captor
	ArgumentCaptor<Callback> callbackCaptor;
	@Captor
	ArgumentCaptor<Query> queryCaptor;
	
	String facetBasedSql = "select * from syn123 where x>1";
	
	@Before
	public void before(){
		MockitoAnnotations.initMocks(this);
		// mocks
		mockActionMenu = Mockito.mock(ActionMenuWidget.class);
		mockView = Mockito.mock(TableEntityWidgetView.class);
		mockDownloadTableQueryModalWidget = Mockito.mock(DownloadTableQueryModalWidget.class);
		mockQueryChangeHandler = Mockito.mock(QueryChangeHandler.class);
		mockSynapseClient = Mockito.mock(SynapseClientAsync.class);
		mockQueryResultsWidget = Mockito.mock(TableQueryResultWidget.class);
		mockQueryInputWidget = Mockito.mock(QueryInputWidget.class);
		mockUploadTableModalWidget = Mockito.mock(UploadTableModalWidget.class);
		mockPreflightController = Mockito.mock(PreflightController.class);
		// stubs
		adapterFactory = new AdapterFactoryImpl();
		columns = TableModelTestUtils.createOneOfEachType();
		tableEntity = new TableEntity();
		tableEntity.setId("syn123");
		tableEntity.setColumnIds(TableModelTestUtils.getColumnModelIds(columns));
		tableBundle = new TableBundle();
		tableBundle.setMaxRowsPerPage(4L);
		tableBundle.setColumnModels(columns);
		widget = new TableEntityWidget(mockView, mockQueryResultsWidget, mockQueryInputWidget, mockDownloadTableQueryModalWidget, mockUploadTableModalWidget, mockPreflightController, mockCopyTextModal, mockSynapseClient);
		
		AsyncMockStubber.callSuccessWith(facetBasedSql).when(mockSynapseClient).generateSqlWithFacets(anyString(), anyList(), anyList(), any(AsyncCallback.class));
		// The test bundle
		entityBundle = new EntityBundle();
		entityBundle.setEntity(tableEntity);
		entityBundle.setTableBundle(tableBundle);
		
		String sql = "SELECT * FROM "+tableEntity.getId()+" LIMIT 3 OFFSET 0";
		Query query = new Query();
		query.setSql(sql);
		when(mockQueryChangeHandler.getQueryString()).thenReturn(query);
	}
	
	private void configureBundleWithView() {
		EntityView view = new EntityView();
		view.setId("syn456");
		view.setColumnIds(TableModelTestUtils.getColumnModelIds(columns));
		entityBundle.setEntity(view);
	}

	@Test
	public void testGetDefaultPageSizeMaxUnder(){
		tableBundle.setMaxRowsPerPage(4L);
		// Configure with the default values
		widget.configure(entityBundle, true, mockQueryChangeHandler, mockActionMenu);
		// since the size from the bundle is less than the default,
		// the value used should be 3/4ths of the max allowed for the schema.
		assertEquals(3l, widget.getDefaultPageSize());
	}
	
	@Test
	public void testGetDefaultPageSizeMaxOver(){
		tableBundle.setMaxRowsPerPage(TableEntityWidget.DEFAULT_LIMIT *2L);
		// Configure with the default values
		widget.configure(entityBundle, true, mockQueryChangeHandler, mockActionMenu);
		// since the size from the bundle is greater than the default
		// the default should be used.
		assertEquals(TableEntityWidget.DEFAULT_LIMIT, widget.getDefaultPageSize());
	}
	
	@Test
	public void testGetDefaultPageSizeNull(){
		tableBundle.setMaxRowsPerPage(null);
		// Configure with the default values
		widget.configure(entityBundle, true, mockQueryChangeHandler, mockActionMenu);
		// when null the default should be used.
		assertEquals(TableEntityWidget.DEFAULT_LIMIT, widget.getDefaultPageSize());
	}
	
	@Test 
	public void testDefaultQueryString(){
		tableBundle.setMaxRowsPerPage(4L);
		widget.configure(entityBundle, true, mockQueryChangeHandler, mockActionMenu);
		String expected = "SELECT * FROM "+tableEntity.getId();
		Query query = new Query();
		query.setSql(expected);
		query.setIsConsistent(true);
		query.setLimit(TableEntityWidget.DEFAULT_LIMIT);
		query.setOffset(TableEntityWidget.DEFAULT_OFFSET);
		assertEquals(query, widget.getDefaultQuery());
	}
	
	@Test
	public void testConfigureNotNullDefaultQuery(){
		tableBundle.setMaxRowsPerPage(4L);
		// This time we pass a query
		String sql = "SELECT * FROM "+tableEntity.getId()+" LIMIT 3 OFFSET 0";
		Query query = new Query();
		query.setSql(sql);
		when(mockQueryChangeHandler.getQueryString()).thenReturn(query);
		widget.configure(entityBundle, true, mockQueryChangeHandler, mockActionMenu);
		// The widget must not change the query when it is passed in.
		verify(mockQueryChangeHandler, never()).onQueryChange(any(Query.class));
	}
	
	@Test
	public void testConfigureEdit(){
		boolean canEdit = true;
		widget.configure(entityBundle, canEdit, mockQueryChangeHandler, mockActionMenu);
		
		verify(mockActionMenu).setActionVisible(Action.EDIT_TABLE_DATA, true);
		verify(mockActionMenu).setActionVisible(Action.UPLOAD_TABLE_DATA, true);
		verify(mockActionMenu).setActionVisible(Action.DOWNLOAD_TABLE_QUERY_RESULTS, true);
		verify(mockActionMenu).setActionVisible(Action.TOGGLE_TABLE_SCHEMA, true);
		
		verify(mockActionMenu).setBasicDivderVisible(true);
	}
	
	@Test
	public void testConfigureNoEdit(){
		boolean canEdit = false;
		widget.configure(entityBundle, canEdit, mockQueryChangeHandler, mockActionMenu);
		
		verify(mockActionMenu).setActionVisible(Action.EDIT_TABLE_DATA, false);
		verify(mockActionMenu).setActionVisible(Action.UPLOAD_TABLE_DATA, false);
		verify(mockActionMenu).setActionVisible(Action.DOWNLOAD_TABLE_QUERY_RESULTS, true);
		verify(mockActionMenu).setActionVisible(Action.TOGGLE_TABLE_SCHEMA, true);
		
		verify(mockActionMenu).setBasicDivderVisible(false);
	}
	
	@Test
	public void testConfigureViewEdit(){
		boolean canEdit = true;
		configureBundleWithView();
		
		widget.configure(entityBundle, canEdit, mockQueryChangeHandler, mockActionMenu);
		
		verify(mockActionMenu).setActionVisible(Action.EDIT_TABLE_DATA, true);
		verify(mockActionMenu).setActionVisible(Action.UPLOAD_TABLE_DATA, true);
		verify(mockActionMenu).setActionVisible(Action.DOWNLOAD_TABLE_QUERY_RESULTS, true);
		verify(mockActionMenu).setActionVisible(Action.TOGGLE_TABLE_SCHEMA, true);
		
		verify(mockActionMenu).setBasicDivderVisible(true);
	}
	@Test
	public void testConfigureViewNoEdit(){
		boolean canEdit = false;
		configureBundleWithView();
		
		widget.configure(entityBundle, canEdit, mockQueryChangeHandler, mockActionMenu);
		
		verify(mockActionMenu).setActionVisible(Action.EDIT_TABLE_DATA, false);
		verify(mockActionMenu).setActionVisible(Action.UPLOAD_TABLE_DATA, false);
		verify(mockActionMenu).setActionVisible(Action.DOWNLOAD_TABLE_QUERY_RESULTS, true);
		verify(mockActionMenu).setActionVisible(Action.TOGGLE_TABLE_SCHEMA, true);
		
		verify(mockActionMenu).setBasicDivderVisible(false);
	}
		
	@Test
	public void testNoColumnsWithEdit(){
		entityBundle.getTableBundle().setColumnModels(new LinkedList<ColumnModel>());
		boolean canEdit = true;
		widget.configure(entityBundle, canEdit, mockQueryChangeHandler, mockActionMenu);
		verify(mockView).setQueryInputVisible(false);
		verify(mockView).setQueryResultsVisible(false);
		verify(mockView).setTableMessageVisible(true);
		verify(mockView).showTableMessage(AlertType.INFO, TableEntityWidget.NO_COLUMNS_EDITABLE);
		// The query should be cleared when there are no columns
		verify(mockQueryChangeHandler).onQueryChange(null);
	}
	
	@Test
	public void testNoColumnsWithWihtouEdit(){
		entityBundle.getTableBundle().setColumnModels(new LinkedList<ColumnModel>());
		boolean canEdit = false;
		widget.configure(entityBundle, canEdit, mockQueryChangeHandler, mockActionMenu);
		verify(mockView).setQueryInputVisible(false);
		verify(mockView).setQueryResultsVisible(false);
		verify(mockView).setTableMessageVisible(true);
		verify(mockView).showTableMessage(AlertType.INFO, TableEntityWidget.NO_COLUMNS_NOT_EDITABLE);
		// The query should be cleared when there are no columns
		verify(mockQueryChangeHandler).onQueryChange(null);
	}
	
	@Test
	public void testQueryExecutionStarted(){
		boolean canEdit = true;
		widget.configure(entityBundle, canEdit, mockQueryChangeHandler, mockActionMenu);
		widget.queryExecutionStarted();
		verify(mockQueryInputWidget).queryExecutionStarted();
		verify(mockActionMenu).setActionVisible(Action.EDIT_TABLE_DATA, false);
		verify(mockActionMenu).setActionVisible(Action.DOWNLOAD_TABLE_QUERY_RESULTS, false);
	}
	
	@Test
	public void testQueryExecutionFinishedSuccess(){
		boolean canEdit = true;
		boolean wasExecutionSuccess = true;
		boolean resultsEditable = true;
		Query startQuery = new Query();
		startQuery.setSql("select * from syn123");
		when(mockQueryChangeHandler.getQueryString()).thenReturn(startQuery);
		widget.configure(entityBundle, canEdit, mockQueryChangeHandler, mockActionMenu);
		reset(mockActionMenu);
		widget.queryExecutionFinished(wasExecutionSuccess, resultsEditable);
		verify(mockQueryInputWidget).queryExecutionFinished(wasExecutionSuccess, resultsEditable);
		verify(mockQueryChangeHandler).onQueryChange(startQuery);
		verify(mockActionMenu).setActionVisible(Action.EDIT_TABLE_DATA, true);
		verify(mockActionMenu).setActionVisible(Action.DOWNLOAD_TABLE_QUERY_RESULTS, true);
	}

	@Test
	public void testViewQueryExecutionFinishedSuccess(){
		configureBundleWithView();
		boolean canEdit = true;
		boolean wasExecutionSuccess = true;
		boolean resultsEditable = true;
		Query startQuery = new Query();
		startQuery.setSql("select * from syn123");
		when(mockQueryChangeHandler.getQueryString()).thenReturn(startQuery);
		widget.configure(entityBundle, canEdit, mockQueryChangeHandler, mockActionMenu);
		reset(mockActionMenu);
		widget.queryExecutionFinished(wasExecutionSuccess, resultsEditable);
		verify(mockQueryInputWidget).queryExecutionFinished(wasExecutionSuccess, resultsEditable);
		verify(mockQueryChangeHandler).onQueryChange(startQuery);
		verify(mockActionMenu).setActionVisible(Action.EDIT_TABLE_DATA, true);
		verify(mockActionMenu).setActionVisible(Action.DOWNLOAD_TABLE_QUERY_RESULTS, true);
	}
	
	@Test
	public void testQueryExecutionFinishedSuccessNoEdit(){
		boolean canEdit = false;
		boolean wasExecutionSuccess = true;
		boolean resultsEditable = true;
		Query startQuery = new Query();
		startQuery.setSql("select * from syn123");
		when(mockQueryChangeHandler.getQueryString()).thenReturn(startQuery);
		widget.configure(entityBundle, canEdit, mockQueryChangeHandler, mockActionMenu);
		reset(mockActionMenu);
		widget.queryExecutionFinished(wasExecutionSuccess, resultsEditable);
		verify(mockQueryInputWidget).queryExecutionFinished(wasExecutionSuccess, resultsEditable);
		verify(mockQueryChangeHandler).onQueryChange(startQuery);
		verify(mockActionMenu, never()).setActionVisible(Action.EDIT_TABLE_DATA, true);
		verify(mockActionMenu, never()).setActionVisible(Action.DOWNLOAD_TABLE_QUERY_RESULTS, true);
	}
	
	@Test
	public void testQueryExecutionFinishedFailed(){
		boolean canEdit = true;
		boolean wasExecutionSuccess = false;
		boolean resultsEditable = true;
		Query startQuery = new Query();
		startQuery.setSql("select * from syn123");
		when(mockQueryChangeHandler.getQueryString()).thenReturn(startQuery);
		widget.configure(entityBundle, canEdit, mockQueryChangeHandler, mockActionMenu);
		reset(mockActionMenu);
		widget.queryExecutionFinished(wasExecutionSuccess, resultsEditable);
		verify(mockQueryInputWidget).queryExecutionFinished(wasExecutionSuccess, resultsEditable);
		verify(mockQueryChangeHandler, never()).onQueryChange(any(Query.class));
		verify(mockActionMenu, never()).setActionVisible(Action.EDIT_TABLE_DATA, true);
		verify(mockActionMenu, never()).setActionVisible(Action.DOWNLOAD_TABLE_QUERY_RESULTS, true);
	}
	
	@Test
	public void testOnExecuteQuery(){
		boolean isView = false;
		boolean canEdit = true;
		// Start with a query that is not on the first page
		Query startQuery = new Query();
		startQuery.setSql("select * from syn123");
		startQuery.setLimit(100L);
		startQuery.setOffset(101L);
		when(mockQueryChangeHandler.getQueryString()).thenReturn(startQuery);
		widget.configure(entityBundle, canEdit, mockQueryChangeHandler, mockActionMenu);
		// Start query get passed to the results
		verify(mockQueryResultsWidget).configure(startQuery, canEdit, isView, widget);
		reset(mockQueryResultsWidget);
		// Set new sql
		String newSQL = "select 1,2,3 from syn123";
		widget.onExecuteQuery(newSQL);
		// Limit and offset should be back to default, and the new SQL included.
		Query expected = new Query();
		expected.setSql(newSQL);
		expected.setLimit(TableEntityWidget.DEFAULT_LIMIT);
		expected.setOffset(TableEntityWidget.DEFAULT_OFFSET);
		verify(mockQueryResultsWidget).configure(expected, canEdit, isView, widget);
	}

	@Test
	public void testOnExecuteViewQuery(){
		boolean isView = true;
		configureBundleWithView();
		boolean canEdit = true;
		// Start with a query that is not on the first page
		Query startQuery = new Query();
		startQuery.setSql("select * from syn123");
		startQuery.setLimit(100L);
		startQuery.setOffset(101L);
		when(mockQueryChangeHandler.getQueryString()).thenReturn(startQuery);
		widget.configure(entityBundle, canEdit, mockQueryChangeHandler, mockActionMenu);
		// Start query get passed to the results
		verify(mockQueryResultsWidget).configure(startQuery, canEdit, isView, widget);
	}
	
	@Test
	public void testOnSchemaToggleShown(){
		boolean canEdit = true;
		boolean schemaShown = true;
		widget.configure(entityBundle, canEdit, mockQueryChangeHandler, mockActionMenu);
		widget.onSchemaToggle(schemaShown);
		verify(mockActionMenu).setActionIcon(Action.TOGGLE_TABLE_SCHEMA, IconType.TOGGLE_DOWN);
	}
	
	@Test
	public void testOnSchemaToggleHide(){
		boolean canEdit = true;
		boolean schemaShown = false;
		widget.configure(entityBundle, canEdit, mockQueryChangeHandler, mockActionMenu);
		widget.onSchemaToggle(schemaShown);
		verify(mockActionMenu).setActionIcon(Action.TOGGLE_TABLE_SCHEMA, IconType.TOGGLE_RIGHT);
	}
	
	@Test
	public void testUploadTableCSVPreflightFailed(){
		AsyncMockStubber.callNoInvovke().when(mockPreflightController).checkUploadToEntity(any(EntityBundle.class), any(Callback.class));
		boolean canEdit = true;
		widget.configure(entityBundle, canEdit, mockQueryChangeHandler, mockActionMenu);
		widget.onUploadTableData();
		// should not proceed to upload.
		verify(mockUploadTableModalWidget, never()).showModal(any(WizardCallback.class));
	}
	
	@Test
	public void testUploadTableCSVPreflightPassed(){
		AsyncMockStubber.callWithInvoke().when(mockPreflightController).checkUploadToEntity(any(EntityBundle.class), any(Callback.class));
		boolean canEdit = true;
		widget.configure(entityBundle, canEdit, mockQueryChangeHandler, mockActionMenu);
		widget.onUploadTableData();
		// proceed to upload
		verify(mockUploadTableModalWidget).showModal(any(WizardCallback.class));
	}
	
	@Test
	public void testEditTablePreflightFailed(){
		AsyncMockStubber.callNoInvovke().when(mockPreflightController).checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
		boolean canEdit = true;
		widget.configure(entityBundle, canEdit, mockQueryChangeHandler, mockActionMenu);
		widget.onEditResults();
		// should not proceed to edit
		verify(mockQueryResultsWidget, never()).onEditRows();
	}
	
	@Test
	public void testEditTablePreflightPassed(){
		AsyncMockStubber.callWithInvoke().when(mockPreflightController).checkUploadToEntity(any(EntityBundle.class), any(Callback.class));
		boolean canEdit = true;
		widget.configure(entityBundle, canEdit, mockQueryChangeHandler, mockActionMenu);
		widget.onEditResults();
		// proceed to edit
		verify(mockQueryResultsWidget).onEditRows();
	}
	
	@Test
	public void testOnStartingnewQuery(){
		boolean canEdit = true;
		boolean isView = false;
		// Start with a query that is not on the first page
		Query startQuery = new Query();
		startQuery.setSql("select * from syn123");
		startQuery.setLimit(100L);
		startQuery.setOffset(101L);
		when(mockQueryChangeHandler.getQueryString()).thenReturn(startQuery);
		widget.configure(entityBundle, canEdit, mockQueryChangeHandler, mockActionMenu);
		reset(mockQueryResultsWidget);
		// Set new sql
		Query newQuery = new Query();
		newQuery.setSql("select 1,2,3 from syn123");
		widget.onStartingNewQuery(newQuery);
		// Should get passed to the input widget
		verify(mockQueryInputWidget).configure(newQuery.getSql(), widget, canEdit);
		// Should not be sent to the results as that is where it came from.
		verify(mockQueryResultsWidget, never()).configure(any(Query.class), anyBoolean(), eq(isView), any(QueryResultsListener.class));
	}
	

	@Test
	public void testCanEditViewResults(){
		configureBundleWithView();
		//we can edit the view, but verify that the query input widget should be told that editing results is not possible
		boolean canEdit = true;
		// Start with a query that is not on the first page
		Query startQuery = new Query();
		startQuery.setSql("select * from syn123");
		startQuery.setLimit(100L);
		startQuery.setOffset(101L);
		when(mockQueryChangeHandler.getQueryString()).thenReturn(startQuery);
		widget.configure(entityBundle, canEdit, mockQueryChangeHandler, mockActionMenu);
		
		verify(mockQueryInputWidget).configure(startQuery.getSql(), widget, canEdit);
	}
	
	
	private void verifySimpleSearchUI() {
		verify(mockView).setAdvancedSearchLinkVisbile(true);
		verify(mockView).setSimpleSearchLinkVisbile(false);
		verify(mockQueryResultsWidget).setFacetsVisible(true);
		verify(mockQueryInputWidget).setShowQueryVisible(true);
		verify(mockQueryInputWidget).setQueryInputVisible(false);
	}
	
	private void verifyAdvancedSearchUI() {
		verify(mockView).setAdvancedSearchLinkVisbile(false);
		verify(mockView).setSimpleSearchLinkVisbile(true);
		verify(mockQueryResultsWidget).setFacetsVisible(false);
		verify(mockQueryInputWidget).setShowQueryVisible(false);
		verify(mockQueryInputWidget).setQueryInputVisible(true);
	}
	
	@Test
	public void testInitSimpleSearchUI(){
		configureBundleWithView();
		// mark a column as being faceted
		columns.get(0).setFacetType(FacetType.enumeration);
		boolean canEdit = false;
		Query startQuery = new Query();
		startQuery.setSql("select * from syn123");
		FacetColumnRequest facetColumnRequest = new FacetColumnValuesRequest();
		facetColumnRequest.setColumnName("col1");
		((FacetColumnValuesRequest)facetColumnRequest).setFacetValues(Collections.singleton("a"));
		startQuery.setSelectedFacets(Collections.singletonList(facetColumnRequest));
		when(mockQueryChangeHandler.getQueryString()).thenReturn(startQuery);
		widget.configure(entityBundle, canEdit, mockQueryChangeHandler, mockActionMenu);
		
		verifySimpleSearchUI();
		
		//show query
		//verify facet select info sql is used
		widget.onShowQuery();
		verify(mockCopyTextModal).setText(facetBasedSql);
		verify(mockCopyTextModal).show();
		
		reset(mockQueryResultsWidget);
		// change to advanced (verify sql that has facet selection info sql used)
		widget.onShowAdvancedSearch();
		verifyAdvancedSearchUI();
		verify(mockQueryResultsWidget).configure(queryCaptor.capture(), eq(canEdit), eq(true), eq(widget));
		Query query = queryCaptor.getValue();
		assertEquals(facetBasedSql, query.getSql());
		assertNull(query.getSelectedFacets());
	}
	
	@Test
	public void testInitAdvancedQueryStateUI1(){
		// simple query, but no facets
		configureBundleWithView();
		boolean canEdit = false;
		Query startQuery = new Query();
		startQuery.setSql("select * from syn123");
		when(mockQueryChangeHandler.getQueryString()).thenReturn(startQuery);
		widget.configure(entityBundle, canEdit, mockQueryChangeHandler, mockActionMenu);
		
		verifyAdvancedSearchUI();
		
		//try to flip to simple search
		widget.onShowSimpleSearch();
		verify(mockView).showErrorMessage(TableEntityWidget.NO_FACETS_SIMPLE_SEARCH_UNSUPPORTED);
	}
	
	@Test
	public void testInitAdvancedQueryStateUI2(){
		// facet, but not a simple query
		configureBundleWithView();
		columns.get(0).setFacetType(FacetType.enumeration);
		boolean canEdit = false;
		Query startQuery = new Query();
		startQuery.setSql("select * from syn123 where x='1'");
		when(mockQueryChangeHandler.getQueryString()).thenReturn(startQuery);
		widget.configure(entityBundle, canEdit, mockQueryChangeHandler, mockActionMenu);
		
		verifyAdvancedSearchUI();
		
		//now test the confirmation when switching to simple search mode
		widget.onShowSimpleSearch();
		
		verify(mockView).showConfirmDialog(eq(TableEntityWidget.RESET_SEARCH_QUERY), eq(TableEntityWidget.RESET_SEARCH_QUERY_MESSAGE), callbackCaptor.capture());
		//on confirmation, show simple search ui
		callbackCaptor.getValue().invoke();
		verifySimpleSearchUI();
		// reset query
		verify(mockQueryResultsWidget).configure(startQuery, canEdit, true, widget);
	}
	
	@Test
	public void testInitAdvancedQueryStateUI3(){
		// facet, but not a simple query
		configureBundleWithView();
		columns.get(0).setFacetType(FacetType.enumeration);
		boolean canEdit = false;
		Query startQuery = new Query();
		startQuery.setSql("select * from syn123 where x='1'");
		when(mockQueryChangeHandler.getQueryString()).thenReturn(startQuery);
		widget.configure(entityBundle, canEdit, mockQueryChangeHandler, mockActionMenu);
		
		verifyAdvancedSearchUI();

		// simple query
		startQuery.setSql("select * from syn123 order by x desc");
		
		//no confirmation necessary
		widget.onShowSimpleSearch();
		verify(mockView, never()).showConfirmDialog(eq(TableEntityWidget.RESET_SEARCH_QUERY), eq(TableEntityWidget.RESET_SEARCH_QUERY_MESSAGE), any(Callback.class));
		verifySimpleSearchUI();
	}
}
