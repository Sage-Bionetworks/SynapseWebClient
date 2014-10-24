package org.sagebionetworks.web.unitclient.widget.table.v2;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;

import org.gwtbootstrap3.client.ui.constants.AlertType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.repo.model.table.TableBundle;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.widget.table.QueryChangeHandler;
import org.sagebionetworks.web.client.widget.table.modal.download.DownloadTableQueryModalWidget;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadTableModalWidget;
import org.sagebionetworks.web.client.widget.table.v2.QueryInputWidget;
import org.sagebionetworks.web.client.widget.table.v2.TableEntityWidget;
import org.sagebionetworks.web.client.widget.table.v2.TableEntityWidgetView;
import org.sagebionetworks.web.client.widget.table.v2.results.TableQueryResultWidget;

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
	DownloadTableQueryModalWidget mockDownloadTableQueryModalWidget;
	UploadTableModalWidget mockUploadTableModalWidget;
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
		mockView = Mockito.mock(TableEntityWidgetView.class);
		mockDownloadTableQueryModalWidget = Mockito.mock(DownloadTableQueryModalWidget.class);
		mockQueryChangeHandler = Mockito.mock(QueryChangeHandler.class);
		mockSynapseClient = Mockito.mock(SynapseClientAsync.class);
		mockQueryResultsWidget = Mockito.mock(TableQueryResultWidget.class);
		mockQueryInputWidget = Mockito.mock(QueryInputWidget.class);
		mockUploadTableModalWidget = Mockito.mock(UploadTableModalWidget.class);
		// stubs
		adapterFactory = new AdapterFactoryImpl();
		columns = TableModelTestUtils.createOneOfEachType();
		tableEntity = new TableEntity();
		tableEntity.setId("syn123");
		tableEntity.setColumnIds(TableModelTestUtils.getColumnModelIds(columns));
		tableBundle = new TableBundle();
		tableBundle.setMaxRowsPerPage(4L);
		tableBundle.setColumnModels(columns);
		widget = new TableEntityWidget(mockView, mockQueryResultsWidget, mockQueryInputWidget, mockDownloadTableQueryModalWidget, mockUploadTableModalWidget);
		// The test bundle
		entityBundle = new EntityBundle(tableEntity, null, null, null, null, null, null, tableBundle);
		
		String sql = "SELECT * FROM "+tableEntity.getId()+" LIMIT 3 OFFSET 0";
		Query query = new Query();
		query.setSql(sql);
		when(mockQueryChangeHandler.getQueryString()).thenReturn(query);
	}
	
	@Test
	public void testGetDefaultPageSizeMaxUnder(){
		tableBundle.setMaxRowsPerPage(4L);
		// Configure with the default values
		widget.configure(entityBundle, true, mockQueryChangeHandler);
		// since the size from the bundle is less than the default,
		// the value used should be 3/4ths of the max allowed for the schema.
		assertEquals(3l, widget.getDefaultPageSize());
	}
	
	@Test
	public void testGetDefaultPageSizeMaxOver(){
		tableBundle.setMaxRowsPerPage(TableEntityWidget.DEFAULT_LIMIT *2L);
		// Configure with the default values
		widget.configure(entityBundle, true, mockQueryChangeHandler);
		// since the size from the bundle is greater than the default
		// the default should be used.
		assertEquals(TableEntityWidget.DEFAULT_LIMIT, widget.getDefaultPageSize());
	}
	
	@Test
	public void testGetDefaultPageSizeNull(){
		tableBundle.setMaxRowsPerPage(null);
		// Configure with the default values
		widget.configure(entityBundle, true, mockQueryChangeHandler);
		// when null the default should be used.
		assertEquals(TableEntityWidget.DEFAULT_LIMIT, widget.getDefaultPageSize());
	}
	
	@Test 
	public void testDefaultQueryString(){
		tableBundle.setMaxRowsPerPage(4L);
		widget.configure(entityBundle, true, mockQueryChangeHandler);
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
		widget.configure(entityBundle, true, mockQueryChangeHandler);
		// The widget must not change the query when it is passed in.
		verify(mockQueryChangeHandler, never()).onQueryChange(any(Query.class));
	}
	
	@Test
	public void testNoColumnsWithEdit(){
		entityBundle.getTableBundle().setColumnModels(new LinkedList<ColumnModel>());
		boolean canEdit = true;
		widget.configure(entityBundle, canEdit, mockQueryChangeHandler);
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
		widget.configure(entityBundle, canEdit, mockQueryChangeHandler);
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
		widget.configure(entityBundle, canEdit, mockQueryChangeHandler);
		widget.queryExecutionStarted();
		verify(mockQueryInputWidget).queryExecutionStarted();
	}
	
	@Test
	public void testQueryExecutionFinishedSuccess(){
		boolean canEdit = true;
		boolean wasExecutionSuccess = true;
		Query startQuery = new Query();
		startQuery.setSql("select * from syn123");
		when(mockQueryChangeHandler.getQueryString()).thenReturn(startQuery);
		widget.configure(entityBundle, canEdit, mockQueryChangeHandler);
		widget.queryExecutionFinished(wasExecutionSuccess);
		verify(mockQueryInputWidget).queryExecutionFinished(wasExecutionSuccess);
		verify(mockQueryChangeHandler).onQueryChange(startQuery);
	}
	
	@Test
	public void testQueryExecutionFinishedFailed(){
		boolean canEdit = true;
		boolean wasExecutionSuccess = false;
		Query startQuery = new Query();
		startQuery.setSql("select * from syn123");
		when(mockQueryChangeHandler.getQueryString()).thenReturn(startQuery);
		widget.configure(entityBundle, canEdit, mockQueryChangeHandler);
		widget.queryExecutionFinished(wasExecutionSuccess);
		verify(mockQueryInputWidget).queryExecutionFinished(wasExecutionSuccess);
		verify(mockQueryChangeHandler, never()).onQueryChange(any(Query.class));
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
		widget.configure(entityBundle, canEdit, mockQueryChangeHandler);
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
}
