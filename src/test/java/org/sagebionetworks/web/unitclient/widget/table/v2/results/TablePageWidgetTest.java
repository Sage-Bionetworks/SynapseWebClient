package org.sagebionetworks.web.unitclient.widget.table.v2.results;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.matchers.Any;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.repo.model.table.QueryResult;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.repo.model.table.Row;
import org.sagebionetworks.repo.model.table.RowSet;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.pagination.DetailedPaginationWidget;
import org.sagebionetworks.web.client.widget.pagination.PageChangeListener;
import org.sagebionetworks.web.client.widget.pagination.PaginationWidget;
import org.sagebionetworks.web.client.widget.pagination.PagingAndSortingListener;
import org.sagebionetworks.web.client.widget.table.KeyboardNavigationHandler;
import org.sagebionetworks.web.client.widget.table.KeyboardNavigationHandler.RowOfWidgets;
import org.sagebionetworks.web.client.widget.table.v2.results.RowSelectionListener;
import org.sagebionetworks.web.client.widget.table.v2.results.RowWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.TablePageView;
import org.sagebionetworks.web.client.widget.table.v2.results.TablePageWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.Cell;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellFactory;
import org.sagebionetworks.web.unitclient.widget.table.v2.TableModelTestUtils;

/**
 * Business logic unit tests for the TablePageWidget.
 * 
 * @author John
 *
 */
public class TablePageWidgetTest {

	CellFactory mockCellFactory;
	TablePageView mockView;
	PortalGinInjector mockGinInjector;
	RowSelectionListener mockListner;
	PagingAndSortingListener mockPageChangeListner;
	DetailedPaginationWidget mockPaginationWidget;
	KeyboardNavigationHandler mockKeyboardNavigationHandler;
	TablePageWidget widget;
	List<ColumnModel> schema;
	List<String> headers;
	QueryResultBundle bundle;
	List<Row> rows;
	Query query;
	
	@Before
	public void before(){
		mockView = Mockito.mock(TablePageView.class);
		mockGinInjector = Mockito.mock(PortalGinInjector.class);
		mockCellFactory = Mockito.mock(CellFactory.class);
		mockListner = Mockito.mock(RowSelectionListener.class);
		mockPaginationWidget = Mockito.mock(DetailedPaginationWidget.class);
		mockPageChangeListner = Mockito.mock(PagingAndSortingListener.class);
		mockKeyboardNavigationHandler = Mockito.mock(KeyboardNavigationHandler.class);
		
		// Use stubs for all cells.
		Answer<Cell> cellAnswer = new Answer<Cell>() {
			@Override
			public Cell answer(InvocationOnMock invocation) throws Throwable {
				return new CellStub();
			}
		};
		when(mockCellFactory.createEditor(any(ColumnModel.class))).thenAnswer(cellAnswer);
		when(mockCellFactory.createRenderer(any(ColumnModel.class))).thenAnswer(cellAnswer);
		when(mockGinInjector.createRowWidget()).thenAnswer(new Answer<RowWidget>(){
			@Override
			public RowWidget answer(InvocationOnMock invocation)
					throws Throwable {
				return new RowWidget(new RowViewStub(), mockCellFactory);
			}});
		when(mockGinInjector.createKeyboardNavigationHandler()).thenReturn(mockKeyboardNavigationHandler);
		widget = new TablePageWidget(mockView, mockGinInjector, mockPaginationWidget);
		
		schema = TableModelTestUtils.createOneOfEachType();
		List<String> headers = TableModelTestUtils.getColumnModelIds(schema);
		// Include an aggregate result in the headers.
		headers.add("sum(four)");
		rows = TableModelTestUtils.createRows(schema, 3);
		// Add an additional column to the rows for the aggregate results
		int i =0;
		for(Row row: rows){
			row.getValues().add("agg"+i);
			i++;
		}
		RowSet set = new RowSet();
		set.setHeaders(headers);
		set.setRows(rows);
		bundle = new QueryResultBundle();
		QueryResult qr = new QueryResult();
		qr.setQueryResults(set);
		bundle.setQueryResult(qr);
		bundle.setSelectColumns(schema);
		bundle.setQueryCount(99L);
		
		query = new Query();
		query.setIsConsistent(true);
		query.setLimit(100L);
		query.setOffset(0L);
		query.setSql("select * from syn123");
	}
	
	@Test
	public void testConfigureRoundTrip(){
		boolean isEditable = true;
		widget.configure(bundle, query, isEditable, null, mockPageChangeListner);
		List<Row> extracted = widget.extractRowSet();
		assertEquals(rows, extracted);
		List<String> headers = widget.extractHeaders();
		List<String> expected = TableModelTestUtils.getColumnModelIds(schema);
		// there should be a null at the end for the aggregate function.
		expected.add(null);
		assertEquals(expected, headers);
		// are the rows registered?
		verify(mockKeyboardNavigationHandler, times(extracted.size())).bindRow(any(RowOfWidgets.class));
	}
	
	@Test
	public void testConfigureWithPaging(){
		boolean isEditable = true;
		widget.configure(bundle, query, isEditable, null, mockPageChangeListner);
		// Pagination should be setup since a page change listener was provided.
		verify(mockPaginationWidget).configure(query.getLimit(), query.getOffset(), bundle.getQueryCount(), mockPageChangeListner);
		verify(mockView).setPaginationWidgetVisible(true);
	}
	
	@Test
	public void testConfigureNoPaging(){
		boolean isEditable = true;
		widget.configure(bundle, null, isEditable, null, null);
		verify(mockPaginationWidget, never()).configure(anyLong(), anyLong(), anyLong(), any(PageChangeListener.class));
		verify(mockView).setPaginationWidgetVisible(false);
	}
	
	@Test
	public void testOnAddNewRow(){
		boolean isEditable = true;
		widget.configure(bundle, query, isEditable, null, mockPageChangeListner);
		widget.onAddNewRow();
		widget.onAddNewRow();
		widget.onAddNewRow();
		List<Row> extracted = widget.extractRowSet();
		assertEquals(rows.size()+3, extracted.size());
	}
	
	@Test
	public void testSelectAllAndDeleteSelected(){
		boolean isEditable = true;
		widget.configure(bundle, null, isEditable, mockListner, null);
		widget.onSelectAll();
		// The handler should be called once
		verify(mockListner).onSelectionChanged();
		assertTrue(widget.isOneRowOrMoreRowsSelected());
		reset(mockListner);
		widget.onDeleteSelected();
		// Are the rows removed from the keyboard navigator?
		verify(mockKeyboardNavigationHandler, times(3)).removeRow(any(RowOfWidgets.class));
		// The handler should be called once
		verify(mockListner).onSelectionChanged();
		assertFalse(widget.isOneRowOrMoreRowsSelected());
		List<Row> extracted = widget.extractRowSet();
		assertTrue(extracted.isEmpty());

	}
	
	@Test
	public void testSelectNone(){
		boolean isEditable = true;
		widget.configure(bundle, null, isEditable, mockListner, null);
		widget.onSelectAll();
		// The handler should be called once
		verify(mockListner).onSelectionChanged();
		assertTrue(widget.isOneRowOrMoreRowsSelected());
		reset(mockListner);
		widget.onSelectNone();
		// The handler should be called once
		verify(mockListner).onSelectionChanged();
		assertFalse(widget.isOneRowOrMoreRowsSelected());
	}
	
	@Test
	public void testToggleSelect(){
		boolean isEditable = true;
		widget.configure(bundle, null, isEditable, mockListner, null);
		widget.onSelectNone();
		// The handler should be called once
		verify(mockListner).onSelectionChanged();
		assertFalse(widget.isOneRowOrMoreRowsSelected());
		reset(mockListner);
		widget.onToggleSelect();
		// The handler should be called once
		verify(mockListner).onSelectionChanged();
		assertTrue(widget.isOneRowOrMoreRowsSelected());
		reset(mockListner);
		widget.onToggleSelect();
		// The handler should be called once
		verify(mockListner).onSelectionChanged();
		assertFalse(widget.isOneRowOrMoreRowsSelected());
	}
}
