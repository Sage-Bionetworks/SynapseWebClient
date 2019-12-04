package org.sagebionetworks.web.unitclient.widget.table.v2.results;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.Row;
import org.sagebionetworks.web.client.widget.table.modal.fileview.TableType;
import org.sagebionetworks.web.client.widget.table.modal.fileview.ViewDefaultColumns;
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

	@Mock
	RowView mockView;
	@Mock
	CellFactory mockCellFactory;
	@Mock
	RowSelectionListener mockListner;
	RowWidget rowWidget;
	List<ColumnModel> types;
	Row aRow;
	List<CellStub> cellStubs;
	String tableId;
	TableType tableType;
	@Mock
	ViewDefaultColumns mockFileViewDefaultColumns;
	List<ColumnModel> defaultColumnModels;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
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
		defaultColumnModels = new ArrayList<ColumnModel>();
		when(mockFileViewDefaultColumns.getDefaultViewColumns(anyBoolean(), anyBoolean())).thenReturn(defaultColumnModels);
		types = TableModelTestUtils.createOneOfEachType();
		// Create a row that matches the type.
		aRow = TableModelTestUtils.createRows(types, 1).get(0);
		rowWidget = new RowWidget(mockView, mockCellFactory, mockFileViewDefaultColumns);
		tableType = TableType.table;
	}

	/**
	 * A basic round trip test where a widget is configured with a row then the row is extracted.
	 */
	@Test
	public void testConfigureAndGet() {
		boolean isEditor = false;
		rowWidget.configure(tableId, types, isEditor, tableType, aRow, null);
		Row extracted = rowWidget.getRow();
		assertNotNull(extracted);
		assertFalse("The extracted row must not be same instance as the configured row.", aRow == extracted);
		assertEquals(aRow, extracted);
	}

	@Test
	public void testConfigureEditor() {
		boolean isEditor = true;
		rowWidget.configure(tableId, types, isEditor, tableType, aRow, null);
		Row extracted = rowWidget.getRow();
		assertEquals(aRow, extracted);
	}

	@Test
	public void testNullSelectionListner() {
		boolean isEditor = true;
		rowWidget.configure(tableId, types, isEditor, tableType, aRow, null);
		// selection should not be shown without a listener.
		verify(mockView).setSelectVisible(false);
	}

	@Test
	public void testWithSelectionListner() {
		boolean isEditor = true;
		rowWidget.configure(tableId, types, isEditor, tableType, aRow, mockListner);
		// selection must be shown when given a listener.
		verify(mockView).setSelectVisible(true);
	}

	@Test
	public void testViewSelectNotVisible() {
		boolean isEditor = true;
		tableType = TableType.files;
		rowWidget.configure(tableId, types, isEditor, tableType, aRow, mockListner);
		// selection must be shown when given a listener.
		verify(mockView).setSelectVisible(false);
	}

	@Test
	public void testIsValid() {
		boolean isEditor = true;
		rowWidget.configure(tableId, types, isEditor, tableType, aRow, mockListner);
		assertTrue(rowWidget.isValid());
		cellStubs.get(4).setIsValid(false);
		assertFalse(rowWidget.isValid());
	}

	@Test
	public void testTakesAddressCell() {
		TakesAddressCell mockTakesAddress = Mockito.mock(TakesAddressCell.class);
		when(mockCellFactory.createRenderer(any(ColumnModel.class))).thenReturn(mockTakesAddress);
		boolean isEditor = false;
		rowWidget.configure(tableId, types, isEditor, tableType, aRow, mockListner);
		verify(mockTakesAddress).setCellAddresss(new CellAddress(tableId, types.get(0), aRow.getRowId(), aRow.getVersionNumber(), tableType));
		verify(mockTakesAddress).setCellAddresss(new CellAddress(tableId, types.get(1), aRow.getRowId(), aRow.getVersionNumber(), tableType));
	}

	@Test
	public void testTakesAddressCellIsView() {
		tableType = TableType.projects;
		TakesAddressCell mockTakesAddress = Mockito.mock(TakesAddressCell.class);
		when(mockCellFactory.createRenderer(any(ColumnModel.class))).thenReturn(mockTakesAddress);
		boolean isEditor = false;
		rowWidget.configure(tableId, types, isEditor, tableType, aRow, mockListner);
		verify(mockTakesAddress).setCellAddresss(new CellAddress(tableId, types.get(0), aRow.getRowId(), aRow.getVersionNumber(), tableType));
		verify(mockTakesAddress).setCellAddresss(new CellAddress(tableId, types.get(1), aRow.getRowId(), aRow.getVersionNumber(), tableType));
	}

	@Test
	public void testEditDefaultColumnModelsIsView() {
		defaultColumnModels.addAll(types);
		tableType = TableType.files;
		boolean isEditor = true;
		rowWidget.configure(tableId, types, isEditor, tableType, aRow, mockListner);
		verify(mockCellFactory, times(types.size())).createRenderer(any(ColumnModel.class));
		verify(mockCellFactory, never()).createEditor(any(ColumnModel.class));
	}

	@Test
	public void testEditDefaultColumnModelsIsTable() {
		defaultColumnModels.addAll(types);
		boolean isEditor = true;
		rowWidget.configure(tableId, types, isEditor, tableType, aRow, mockListner);
		verify(mockCellFactory, never()).createRenderer(any(ColumnModel.class));
		verify(mockCellFactory, times(types.size())).createEditor(any(ColumnModel.class));
	}


}
