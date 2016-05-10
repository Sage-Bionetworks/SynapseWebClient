package org.sagebionetworks.web.unitclient.widget.table.v2.results;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.Row;
import org.sagebionetworks.web.client.widget.table.v2.results.RowFormEditorWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.RowFormView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.Cell;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellFactory;
import org.sagebionetworks.web.unitclient.widget.table.v2.TableModelTestUtils;

/**
 * Unit tests for RowFormEditorWidget business logic.
 * 
 * @author Jay
 *
 */
public class RowFormEditorWidgetTest {
	
	@Mock
	RowFormView mockView;
	@Mock
	CellFactory mockCellFactory;
	
	List<ColumnModel> types;
	List<CellStub> cellStubs;
	String tableId;
	RowFormEditorWidget rowWidget;
	@Before
	public void before(){
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
		when(mockCellFactory.createFormEditor(any(ColumnModel.class))).thenAnswer(answer);
		types = TableModelTestUtils.createOneOfEachType();
		rowWidget = new RowFormEditorWidget(mockView, mockCellFactory);
	}
	
	
	@Test
	public void testConfgureEditor(){
		rowWidget.configure(tableId, types);
		Row extracted = rowWidget.getRow();
		assertEquals(types.size(), extracted.getValues().size());
	}
	
	@Test
	public void testIsValid(){
		rowWidget.configure(tableId, types);
		assertTrue(rowWidget.isValid());
		cellStubs.get(4).setIsValid(false);
		assertFalse(rowWidget.isValid());
	}
}
