package org.sagebionetworks.web.unitclient.widget.table.v2.results;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.repo.model.table.FacetColumnRequest;
import org.sagebionetworks.repo.model.table.FacetColumnResult;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.repo.model.table.QueryResult;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.repo.model.table.Row;
import org.sagebionetworks.repo.model.table.RowSet;
import org.sagebionetworks.repo.model.table.SelectColumn;
import org.sagebionetworks.repo.model.table.SortDirection;
import org.sagebionetworks.repo.model.table.SortItem;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.pagination.BasicPaginationWidget;
import org.sagebionetworks.web.client.widget.pagination.PageChangeListener;
import org.sagebionetworks.web.client.widget.table.KeyboardNavigationHandler;
import org.sagebionetworks.web.client.widget.table.KeyboardNavigationHandler.RowOfWidgets;
import org.sagebionetworks.web.client.widget.table.modal.fileview.TableType;
import org.sagebionetworks.web.client.widget.table.modal.fileview.ViewDefaultColumns;
import org.sagebionetworks.web.client.widget.table.v2.results.PagingAndSortingListener;
import org.sagebionetworks.web.client.widget.table.v2.results.RowSelectionListener;
import org.sagebionetworks.web.client.widget.table.v2.results.RowWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.SortableTableHeader;
import org.sagebionetworks.web.client.widget.table.v2.results.StaticTableHeader;
import org.sagebionetworks.web.client.widget.table.v2.results.TablePageView;
import org.sagebionetworks.web.client.widget.table.v2.results.TablePageWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.Cell;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellFactory;
import org.sagebionetworks.web.client.widget.table.v2.results.facets.FacetsWidget;
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
	BasicPaginationWidget mockPaginationWidget;
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
	TableType tableType;
	@Mock
	FacetsWidget mockFacetsWidget;
	@Mock
	CallbackP<FacetColumnRequest> mockFacetChangedHandler;
	@Mock
	Callback mockResetFacetsHandler;
	@Mock
	FacetColumnResult mockFacetColumnResult;
	@Mock
	ViewDefaultColumns mockFileViewDefaultColumns;
	
	List<ColumnModel> defaultColumnModels;
	public static final String ENTITY_ID = "syn123";
	List<FacetColumnResult> facets;

	@Before
	public void before(){
		MockitoAnnotations.initMocks(this);
		facets = new ArrayList<FacetColumnResult>();
		facets.add(mockFacetColumnResult);
		mockView = Mockito.mock(TablePageView.class);
		mockGinInjector = Mockito.mock(PortalGinInjector.class);
		mockCellFactory = Mockito.mock(CellFactory.class);
		mockListner = Mockito.mock(RowSelectionListener.class);
		mockPaginationWidget = Mockito.mock(BasicPaginationWidget.class);
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
				return new RowWidget(new RowViewStub(), mockCellFactory, mockFileViewDefaultColumns);
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
		defaultColumnModels = new ArrayList<ColumnModel>();
		when(mockFileViewDefaultColumns.getDefaultViewColumns(anyBoolean(), anyBoolean())).thenReturn(defaultColumnModels);
		widget = new TablePageWidget(mockView, mockGinInjector, mockPaginationWidget,mockFacetsWidget);
		
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
		set.setTableId(ENTITY_ID);
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
		query.setSql("select * from " + ENTITY_ID);
		bundle.setFacets(facets);
		when(mockFacetsWidget.isShowingFacets()).thenReturn(true);
		tableType = TableType.table;
	}
	
	@Test
	public void testConfigureRoundTrip(){
		boolean isEditable = true;
		widget.configure(bundle, query, null, isEditable, tableType, null, mockPageChangeListner, mockFacetChangedHandler, mockResetFacetsHandler);
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
		widget.configure(bundle, query, null, isEditable, tableType, null, mockPageChangeListner, mockFacetChangedHandler, mockResetFacetsHandler);
		// Pagination should be setup since a page change listener was provided.
		long rowCount = rows.size();
		verify(mockPaginationWidget).configure(query.getLimit(), query.getOffset(), rowCount, mockPageChangeListner);
		verify(mockView).setPaginationWidgetVisible(true);
		verify(mockView).setEditorBufferVisible(true);
	}
	
	@Test
	public void testConfigureEditable(){
		boolean isEditable = true;
		// Static headers should be used for edits
		assertTrue(staticHeader.isEmpty());
		widget.configure(bundle, query, null, isEditable, tableType, null, mockPageChangeListner, mockFacetChangedHandler, mockResetFacetsHandler);
		verify(mockView, times(2)).setFacetsVisible(false);
		verify(mockView, never()).setFacetsVisible(true);
		long rowCount = rows.size();
		verify(mockPaginationWidget).configure(query.getLimit(), query.getOffset(), rowCount, mockPageChangeListner);
		verify(mockView).setEditorBufferVisible(true);
		assertEquals(bundle.getColumnModels().size()+1, staticHeader.size());
	}
	
	@Test
	public void testConfigureNotEditable(){
		boolean isEditable = false;
		// Sortable headers should be used for views.
		assertTrue(sortHeaders.isEmpty());
		widget.setFacetsVisible(true);
		widget.configure(bundle, query, null, isEditable, tableType, null, mockPageChangeListner, mockFacetChangedHandler, mockResetFacetsHandler);
		verify(mockFacetsWidget).configure(eq(facets), eq(mockFacetChangedHandler), anyList());
		long rowCount = rows.size();
		verify(mockPaginationWidget).configure(query.getLimit(), query.getOffset(), rowCount, mockPageChangeListner);
		verify(mockView).setEditorBufferVisible(false);
		assertEquals(bundle.getColumnModels().size()+1, sortHeaders.size());
		verify(mockView, times(2)).setFacetsVisible(true);
	}
	
	@Test
	public void testConfigureNotEditableNoFacets(){
		boolean isEditable = false;
		facets.clear();
		widget.configure(bundle, query, null, isEditable, tableType, null, mockPageChangeListner, mockFacetChangedHandler, mockResetFacetsHandler);
		verify(mockView, times(2)).setFacetsVisible(false);
		verify(mockView, never()).setFacetsVisible(true);
	}
	
	@Test
	public void testConfigureWithSortDescending(){
		int sortColumnIndex = 2;
		SortItem sort = new SortItem();
		sort.setColumn(schema.get(sortColumnIndex).getName());
		sort.setDirection(SortDirection.DESC);
		List<SortItem> sortList = new ArrayList<SortItem>();
		sortList.add(sort);
		boolean isEditable = false;
		widget.configure(bundle, query, sortList, isEditable, tableType, null, mockPageChangeListner, mockFacetChangedHandler, mockResetFacetsHandler);
		// Pagination should be setup since a page change listener was provided.
		long rowCount = rows.size();
		verify(mockPaginationWidget).configure(query.getLimit(), query.getOffset(), rowCount, mockPageChangeListner);
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
		List<SortItem> sortList = new ArrayList<SortItem>();
		sortList.add(sort);
		boolean isEditable = false;
		widget.configure(bundle, query, sortList, isEditable, tableType, null, mockPageChangeListner, mockFacetChangedHandler, mockResetFacetsHandler);
		// Pagination should be setup since a page change listener was provided.
		long rowCount = rows.size();
		verify(mockPaginationWidget).configure(query.getLimit(), query.getOffset(), rowCount, mockPageChangeListner);
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
	public void testConfigureWithMultipleSorts(){
		List<SortItem> sortList = new ArrayList<SortItem>();
		int ascColumnIndex = 1;
		int descColumnIndex = 2;
		SortItem sort = new SortItem();
		sort.setColumn(schema.get(ascColumnIndex).getName());
		sort.setDirection(SortDirection.ASC);
		sortList.add(sort);
		sort = new SortItem();
		sort.setColumn(schema.get(descColumnIndex).getName());
		sort.setDirection(SortDirection.DESC);
		sortList.add(sort);
		boolean isEditable = false;
		widget.configure(bundle, query, sortList, isEditable, tableType, null, mockPageChangeListner, mockFacetChangedHandler, mockResetFacetsHandler);
		// Pagination should be setup since a page change listener was provided.
		long rowCount = rows.size();
		verify(mockPaginationWidget).configure(query.getLimit(), query.getOffset(), rowCount, mockPageChangeListner);
		verify(mockView).setPaginationWidgetVisible(true);
		
		// Check each header
		for(int i=0; i<sortHeaders.size(); i++){
			SortableTableHeader sth = sortHeaders.get(i);
			if(i == ascColumnIndex){
				verify(sth).setIcon(IconType.SORT_ASC);
			}else if(i == descColumnIndex){
				verify(sth).setIcon(IconType.SORT_DESC);
			}else {
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
		List<SortItem> sortList = new ArrayList<SortItem>();
		sortList.add(sort);
		boolean isEditable = false;
		widget.configure(bundle, query, sortList, isEditable, tableType, null, mockPageChangeListner, mockFacetChangedHandler, mockResetFacetsHandler);
		// Pagination should be setup since a page change listener was provided.
		long rowCount = rows.size();
		verify(mockPaginationWidget).configure(query.getLimit(), query.getOffset(), rowCount, mockPageChangeListner);
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
		widget.configure(bundle, null, null, isEditable, tableType, null, null, mockFacetChangedHandler, mockResetFacetsHandler);
		verify(mockPaginationWidget, never()).configure(anyLong(), anyLong(), anyLong(), any(PageChangeListener.class));
		verify(mockView).setPaginationWidgetVisible(false);
	}
	
	@Test
	public void testOnAddNewRow(){
		boolean isEditable = true;
		widget.configure(bundle, query, null, isEditable, tableType, null, mockPageChangeListner, mockFacetChangedHandler, mockResetFacetsHandler);
		widget.onAddNewRow();
		widget.onAddNewRow();
		widget.onAddNewRow();
		List<Row> extracted = widget.extractRowSet();
		assertEquals(rows.size()+3, extracted.size());
	}
	
	@Test
	public void testSelectAllAndDeleteSelected(){
		boolean isEditable = true;
		widget.configure(bundle, null, null, isEditable, tableType, mockListner, null, mockFacetChangedHandler, mockResetFacetsHandler);
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
		widget.configure(bundle, null, null, isEditable, tableType, mockListner, null, mockFacetChangedHandler, mockResetFacetsHandler);
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
		widget.configure(bundle, null, null, isEditable, tableType, mockListner, null, mockFacetChangedHandler, mockResetFacetsHandler);
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
		widget.configure(bundle, null, null, isEditable, tableType, mockListner, null, mockFacetChangedHandler, mockResetFacetsHandler);
		assertTrue(widget.isValid());
		// Set on cell to be invalid
		cellStubs.get(3).setIsValid(false);
		assertFalse(widget.isValid());
	}
	
	@Test
	public void testConfigureFacetsAvailableNotVisible(){
		boolean isEditable = false;
		//facets would have been shown, but force advanced mode.
		widget.setFacetsVisible(false);
		widget.configure(bundle, query, null, isEditable, tableType, null, mockPageChangeListner, mockFacetChangedHandler, mockResetFacetsHandler);
		verify(mockFacetsWidget).configure(eq(facets), eq(mockFacetChangedHandler), anyList());
		verify(mockView, never()).setFacetsVisible(true);
		verify(mockView, atLeastOnce()).setFacetsVisible(false);
	}
}
