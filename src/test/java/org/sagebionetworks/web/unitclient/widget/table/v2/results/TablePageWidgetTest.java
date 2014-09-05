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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.repo.model.table.Row;
import org.sagebionetworks.repo.model.table.RowSet;
import org.sagebionetworks.web.client.PortalGinInjector;
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
	TablePageWidget widget;
	List<ColumnModel> schema;
	List<String> headers;
	QueryResultBundle bundle;
	List<Row> rows;
	
	@Before
	public void before(){
		mockView = Mockito.mock(TablePageView.class);
		mockGinInjector = Mockito.mock(PortalGinInjector.class);
		mockCellFactory = Mockito.mock(CellFactory.class);
		mockListner = Mockito.mock(RowSelectionListener.class);
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
		
		widget = new TablePageWidget(mockView, mockGinInjector);
		
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
		bundle.setQueryResults(set);
		bundle.setSelectColumns(schema);
	}
	
	@Test
	public void testConfigureRoundTrip(){
		boolean isEditable = true;
		widget.configure(bundle, isEditable, null);
		List<Row> extracted = widget.extractRowSet();
		assertEquals(rows, extracted);
		List<String> headers = widget.extractHeaders();
		List<String> expected = TableModelTestUtils.getColumnModelIds(schema);
		// there should be a null at the end for the aggregate function.
		expected.add(null);
		assertEquals(expected, headers);
	}
	
	@Test
	public void testOnAddNewRow(){
		boolean isEditable = true;
		widget.configure(bundle, isEditable, null);
		widget.onAddNewRow();
		widget.onAddNewRow();
		widget.onAddNewRow();
		List<Row> extracted = widget.extractRowSet();
		assertEquals(rows.size()+3, extracted.size());
	}
	
	@Test
	public void testSelectAllAndDeleteSelected(){
		boolean isEditable = true;
		widget.configure(bundle, isEditable, mockListner);
		widget.onSelectAll();
		// The handler should be called once
		verify(mockListner).onSelectionChanged();
		assertTrue(widget.isOneRowOrMoreRowsSelected());
		reset(mockListner);
		widget.onDeleteSelected();
		// The handler should be called once
		verify(mockListner).onSelectionChanged();
		assertFalse(widget.isOneRowOrMoreRowsSelected());
		List<Row> extracted = widget.extractRowSet();
		assertTrue(extracted.isEmpty());
	}
	
	@Test
	public void testSelectNone(){
		boolean isEditable = true;
		widget.configure(bundle, isEditable, mockListner);
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
		widget.configure(bundle, isEditable, mockListner);
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
