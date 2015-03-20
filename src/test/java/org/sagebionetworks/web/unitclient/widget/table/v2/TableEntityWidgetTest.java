package org.sagebionetworks.web.unitclient.widget.table.v2;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;

import org.gwtbootstrap3.client.ui.constants.AlertType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.repo.model.table.TableBundle;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
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
	
	@Before
	public void before(){
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
		widget = new TableEntityWidget(mockView, mockQueryResultsWidget, mockQueryInputWidget, mockDownloadTableQueryModalWidget, mockUploadTableModalWidget, mockPreflightController);
		// The test bundle
		entityBundle = new EntityBundle();
		entityBundle.setEntity(tableEntity);
		entityBundle.setTableBundle(tableBundle);
		
		String sql = "SELECT * FROM "+tableEntity.getId()+" LIMIT 3 OFFSET 0";
		Query query = new Query();
		query.setSql(sql);
		when(mockQueryChangeHandler.getQueryString()).thenReturn(query);
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
		boolean canEdit = true;
		// Start with a query that is not on the first page
		Query startQuery = new Query();
		startQuery.setSql("select * from syn123");
		startQuery.setLimit(100L);
		startQuery.setOffset(101L);
		when(mockQueryChangeHandler.getQueryString()).thenReturn(startQuery);
		widget.configure(entityBundle, canEdit, mockQueryChangeHandler, mockActionMenu);
		// Start query get passed to the results
		verify(mockQueryResultsWidget).configure(startQuery, canEdit, widget);
		reset(mockQueryResultsWidget);
		// Set new sql
		String newSQL = "select 1,2,3 from syn123";
		widget.onExecuteQuery(newSQL);
		// Limit and offset should be back to default, and the new SQL included.
		Query expected = new Query();
		expected.setSql(newSQL);
		expected.setLimit(TableEntityWidget.DEFAULT_LIMIT);
		expected.setOffset(TableEntityWidget.DEFAULT_OFFSET);
		verify(mockQueryResultsWidget).configure(expected, canEdit, widget);
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
		verify(mockQueryResultsWidget, never()).configure(any(Query.class), anyBoolean(), any(QueryResultsListener.class));
	}
}
