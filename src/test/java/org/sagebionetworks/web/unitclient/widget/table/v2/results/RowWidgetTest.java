package org.sagebionetworks.web.unitclient.widget.table.v2.results;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.Row;
import org.sagebionetworks.web.client.widget.table.v2.results.RowSelectionListener;
import org.sagebionetworks.web.client.widget.table.v2.results.RowView;
import org.sagebionetworks.web.client.widget.table.v2.results.RowWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.Cell;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellFactory;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.TakesAddressCell;
import org.sagebionetworks.web.shared.table.CellAddress;
import org.sagebionetworks.web.unitclient.widget.table.v2.TableModelTestUtils;

/**
 * Unit tests for RowWidget business logic.
 * 
 * @author John
 *
 */
public class RowWidgetTest {
	
	RowView mockView;
	CellFactory mockCellFactory;
	RowSelectionListener mockListner;
	RowWidget rowWidget;
	List<ColumnModel> types;
	Row aRow;
	List<CellStub> cellStubs;
	String tableId;
	
	@Before
	public void before(){
		mockView = Mockito.mock(RowView.class);
		mockCellFactory = Mockito.mock(CellFactory.class);
		mockListner = Mockito.mock(RowSelectionListener.class);
		cellStubs = new LinkedList<CellStub>();
		tableId = "syn123";
		// Use stubs for all cells.
		Answer<Cell> answer = new Answer<Cell>() {
			@Override
			public Cell answer(InvocationOnMock invocation) throws Throwable {
				CellStub stub = new CellStub();
				cellStubs.add(stub);
				return stub;
			}
		};
		when(mockCellFactory.createEditor(any(ColumnModel.class))).thenAnswer(answer);
		when(mockCellFactory.createRenderer(any(ColumnModel.class))).thenAnswer(answer);
		
		types = TableModelTestUtils.createOneOfEachType();
		// Create a row that matches the type.
		aRow = TableModelTestUtils.createRows(types, 1).get(0);
		rowWidget = new RowWidget(mockView, mockCellFactory);
	}
	
	/**
	 * A basic round trip test where a widget is configured with a row then the row is extracted.
	 */
	@Test
	public void testConfigureAndGet(){
		boolean isEditor = false;
		rowWidget.configure(tableId, types, isEditor, aRow, null);
		Row extracted = rowWidget.getRow();
		assertNotNull(extracted);
		assertFalse("The extracted row must not be same instance as the configured row.", aRow == extracted);
		assertEquals(aRow, extracted);
	}
	
	@Test
	public void testConfgureEditor(){
		boolean isEditor = true;
		rowWidget.configure(tableId, types, isEditor, aRow, null);
		Row extracted = rowWidget.getRow();
		assertEquals(aRow, extracted);
	}
	
	@Test
	public void testNullSelectionListner(){
		boolean isEditor = true;
		rowWidget.configure(tableId, types, isEditor, aRow, null);
		// selection should not be shown without a listener.
		verify(mockView).setSelectVisible(false);
	}
	
	@Test
	public void testWithSelectionListner(){
		boolean isEditor = true;
		rowWidget.configure(tableId, types, isEditor, aRow, mockListner);
		// selection must be shown when given a listener.
		verify(mockView).setSelectVisible(true);
	}
	
	@Test
	public void testIsValid(){
		boolean isEditor = true;
		rowWidget.configure(tableId, types, isEditor, aRow, mockListner);
		assertTrue(rowWidget.isValid());
		cellStubs.get(4).setIsValid(false);
		assertFalse(rowWidget.isValid());
	}
	
	@Test
	public void testTakesAddressCell(){
		TakesAddressCell mockTakesAddress = Mockito.mock(TakesAddressCell.class);
		when(mockCellFactory.createRenderer(any(ColumnModel.class))).thenReturn(mockTakesAddress);
		boolean isEditor = false;
		rowWidget.configure(tableId, types, isEditor, aRow, mockListner);
		verify(mockTakesAddress).setCellAddresss(new CellAddress(tableId, types.get(0).getId(), aRow.getRowId(), aRow.getVersionNumber()));
		verify(mockTakesAddress).setCellAddresss(new CellAddress(tableId, types.get(1).getId(), aRow.getRowId(), aRow.getVersionNumber()));
	}

}
