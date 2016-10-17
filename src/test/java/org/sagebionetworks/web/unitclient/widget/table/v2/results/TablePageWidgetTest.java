package org.sagebionetworks.web.unitclient.widget.table.v2.results;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.repo.model.table.QueryResult;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.repo.model.table.Row;
import org.sagebionetworks.repo.model.table.RowSet;
import org.sagebionetworks.repo.model.table.SelectColumn;
import org.sagebionetworks.repo.model.table.SortDirection;
import org.sagebionetworks.repo.model.table.SortItem;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.pagination.DetailedPaginationWidget;
import org.sagebionetworks.web.client.widget.pagination.PageChangeListener;
import org.sagebionetworks.web.client.widget.table.KeyboardNavigationHandler;
import org.sagebionetworks.web.client.widget.table.KeyboardNavigationHandler.RowOfWidgets;
import org.sagebionetworks.web.client.widget.table.v2.results.PagingAndSortingListener;
import org.sagebionetworks.web.client.widget.table.v2.results.RowSelectionListener;
import org.sagebionetworks.web.client.widget.table.v2.results.RowWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.SortableTableHeader;
import org.sagebionetworks.web.client.widget.table.v2.results.StaticTableHeader;
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
	SelectColumn derivedColumn;
	List<SortableTableHeader> sortHeaders;
	List<StaticTableHeader> staticHeader;
	List<SelectColumn> headers;
	QueryResultBundle bundle;
	List<Row> rows;
	Query query;
	List<CellStub> cellStubs;
	boolean isView;
	@Before
	public void before(){
		mockView = Mockito.mock(TablePageView.class);
		mockGinInjector = Mockito.mock(PortalGinInjector.class);
		mockCellFactory = Mockito.mock(CellFactory.class);
		mockListner = Mockito.mock(RowSelectionListener.class);
		mockPaginationWidget = Mockito.mock(DetailedPaginationWidget.class);
		mockPageChangeListner = Mockito.mock(PagingAndSortingListener.class);
		mockKeyboardNavigationHandler = Mockito.mock(KeyboardNavigationHandler.class);
		cellStubs = new LinkedList<CellStub>();
		
		// Use stubs for all cells.
		Answer<Cell> cellAnswer = new Answer<Cell>() {
			@Override
			public Cell answer(InvocationOnMock invocation) throws Throwable {
				CellStub stub = new CellStub();
				cellStubs.add(stub);
				return stub;
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
		sortHeaders = new LinkedList<SortableTableHeader>();
		when(mockGinInjector.createSortableTableHeader()).thenAnswer(new Answer<SortableTableHeader>() {
			@Override
			public SortableTableHeader answer(InvocationOnMock invocation)
					throws Throwable {
				SortableTableHeader header = Mockito.mock(SortableTableHeader.class);
				sortHeaders.add(header);
				return header;
			}
		});
		staticHeader = new LinkedList<StaticTableHeader>();
		when(mockGinInjector.createStaticTableHeader()).thenAnswer(new Answer<StaticTableHeader>() {
			@Override
			public StaticTableHeader answer(InvocationOnMock invocation)
					throws Throwable {
				StaticTableHeader header = Mockito.mock(StaticTableHeader.class);
				staticHeader.add(header);
				return header;
			}
		});
		widget = new TablePageWidget(mockView, mockGinInjector, mockPaginationWidget);
		
		schema = TableModelTestUtils.createOneOfEachType();
		headers = TableModelTestUtils.buildSelectColumns(schema);
		// Include an aggregate result in the headers.
		derivedColumn = new SelectColumn();
		derivedColumn.setColumnType(ColumnType.DOUBLE);
		derivedColumn.setName("sum(four)");
		headers.add(derivedColumn);
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
		bundle.setSelectColumns(headers);
		bundle.setQueryCount(99L);
		bundle.setColumnModels(schema);
		
		query = new Query();
		query.setIsConsistent(true);
		query.setLimit(100L);
		query.setOffset(0L);
		query.setSql("select * from syn123");
	
		isView = false;
	}
	
	@Test
	public void testConfigureRoundTrip(){
		boolean isEditable = true;
		widget.configure(bundle, query, null, isEditable, isView, null, mockPageChangeListner);
		List<Row> extracted = widget.extractRowSet();
		assertEquals(rows, extracted);
		List<ColumnModel> headers = widget.extractHeaders();
		List<ColumnModel> expected = new LinkedList<ColumnModel>(schema);
		// there should be a derived column at the end for the for the aggregate function.
		ColumnModel derived = new ColumnModel();
		derived.setColumnType(derivedColumn.getColumnType());
		derived.setName(derivedColumn.getName());
		expected.add(derived);
		assertEquals(expected, headers);
		// are the rows registered?
		verify(mockKeyboardNavigationHandler, times(extracted.size())).bindRow(any(RowOfWidgets.class));
	}
	
	@Test
	public void testConfigureWithPaging(){
		boolean isEditable = true;
		widget.configure(bundle, query, null, isEditable, isView, null, mockPageChangeListner);
		// Pagination should be setup since a page change listener was provided.
		verify(mockPaginationWidget).configure(query.getLimit(), query.getOffset(), bundle.getQueryCount(), mockPageChangeListner);
		verify(mockView).setPaginationWidgetVisible(true);
		verify(mockView).setEditorBufferVisible(true);
	}
	
	@Test
	public void testConfigureEditable(){
		boolean isEditable = true;
		// Static headers should be used for edits
		assertTrue(staticHeader.isEmpty());
		widget.configure(bundle, query, null, isEditable, isView, null, mockPageChangeListner);
		verify(mockPaginationWidget).configure(query.getLimit(), query.getOffset(), bundle.getQueryCount(), mockPageChangeListner);
		verify(mockView).setEditorBufferVisible(true);
		assertEquals(bundle.getColumnModels().size()+1, staticHeader.size());
	}
	
	@Test
	public void testConfigureNotEditable(){
		boolean isEditable = false;
		// Sortable headers should be used for views.
		assertTrue(sortHeaders.isEmpty());
		widget.configure(bundle, query, null, isEditable, isView, null, mockPageChangeListner);
		verify(mockPaginationWidget).configure(query.getLimit(), query.getOffset(), bundle.getQueryCount(), mockPageChangeListner);
		verify(mockView).setEditorBufferVisible(false);
		assertEquals(bundle.getColumnModels().size()+1, sortHeaders.size());
	}
	
	@Test
	public void testConfigureWithSortDescending(){
		int sortColumnIndex = 2;
		SortItem sort = new SortItem();
		sort.setColumn(schema.get(sortColumnIndex).getName());
		sort.setDirection(SortDirection.DESC);
		boolean isEditable = false;
		widget.configure(bundle, query, sort, isEditable, isView, null, mockPageChangeListner);
		// Pagination should be setup since a page change listener was provided.
		verify(mockPaginationWidget).configure(query.getLimit(), query.getOffset(), bundle.getQueryCount(), mockPageChangeListner);
		verify(mockView).setPaginationWidgetVisible(true);
		
		// Check each header
		for(int i=0; i<sortHeaders.size(); i++){
			SortableTableHeader sth = sortHeaders.get(i);
			String headerName;
			if(i < bundle.getSelectColumns().size()){
				headerName = bundle.getSelectColumns().get(i).getName();
			}else{
				headerName = "sum(four)";
			}
			verify(sth).configure(headerName, mockPageChangeListner);
			if(i == sortColumnIndex){
				verify(sth).setIcon(IconType.SORT_DESC);
			}else{
				verify(sth, never()).setIcon(any(IconType.class));
			}
		}
	}
	
	@Test
	public void testConfigureWithSortAscending(){
		int sortColumnIndex = 1;
		SortItem sort = new SortItem();
		sort.setColumn(schema.get(sortColumnIndex).getName());
		sort.setDirection(SortDirection.ASC);
		boolean isEditable = false;
		widget.configure(bundle, query, sort, isEditable, isView, null, mockPageChangeListner);
		// Pagination should be setup since a page change listener was provided.
		verify(mockPaginationWidget).configure(query.getLimit(), query.getOffset(), bundle.getQueryCount(), mockPageChangeListner);
		verify(mockView).setPaginationWidgetVisible(true);
		
		// Check each header
		for(int i=0; i<sortHeaders.size(); i++){
			SortableTableHeader sth = sortHeaders.get(i);
			if(i == sortColumnIndex){
				verify(sth).setIcon(IconType.SORT_ASC);
			}else{
				verify(sth, never()).setIcon(any(IconType.class));
			}
		}
	}
	
	/**
	 * Test for SWC-2312
	 */
	@Test
	public void testConfigureWithSortDirectionNull(){
		int sortColumnIndex = 1;
		SortItem sort = new SortItem();
		sort.setColumn(schema.get(sortColumnIndex).getName());
		// When the direction is null
		sort.setDirection(null);
		boolean isEditable = false;
		widget.configure(bundle, query, sort, isEditable, isView, null, mockPageChangeListner);
		// Pagination should be setup since a page change listener was provided.
		verify(mockPaginationWidget).configure(query.getLimit(), query.getOffset(), bundle.getQueryCount(), mockPageChangeListner);
		verify(mockView).setPaginationWidgetVisible(true);
		
		// Check each header
		for(int i=0; i<sortHeaders.size(); i++){
			SortableTableHeader sth = sortHeaders.get(i);
			if(i == sortColumnIndex){
				verify(sth).setIcon(IconType.SORT_ASC);
			}else{
				verify(sth, never()).setIcon(any(IconType.class));
			}
		}
	}
	
	@Test
	public void testConfigureNoPaging(){
		boolean isEditable = true;
		widget.configure(bundle, null, null, isEditable, isView, null, null);
		verify(mockPaginationWidget, never()).configure(anyLong(), anyLong(), anyLong(), any(PageChangeListener.class));
		verify(mockView).setPaginationWidgetVisible(false);
	}
	
	@Test
	public void testOnAddNewRow(){
		boolean isEditable = true;
		widget.configure(bundle, query, null, isEditable, isView, null, mockPageChangeListner);
		widget.onAddNewRow();
		widget.onAddNewRow();
		widget.onAddNewRow();
		List<Row> extracted = widget.extractRowSet();
		assertEquals(rows.size()+3, extracted.size());
	}
	
	@Test
	public void testSelectAllAndDeleteSelected(){
		boolean isEditable = true;
		widget.configure(bundle, null, null, isEditable, isView, mockListner, null);
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
		widget.configure(bundle, null, null, isEditable, isView, mockListner, null);
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
		widget.configure(bundle, null, null, isEditable, isView, mockListner, null);
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
	
	@Test
	public void testIsValid(){
		boolean isEditable = true;
		widget.configure(bundle, null, null, isEditable, isView, mockListner, null);
		assertTrue(widget.isValid());
		// Set on cell to be invalid
		cellStubs.get(3).setIsValid(false);
		assertFalse(widget.isValid());
	}
}
