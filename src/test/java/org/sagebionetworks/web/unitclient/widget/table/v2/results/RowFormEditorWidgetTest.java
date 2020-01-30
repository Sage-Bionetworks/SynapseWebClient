package org.sagebionetworks.web.unitclient.widget.table.v2.results;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.repo.model.table.Row;
import org.sagebionetworks.web.client.security.AuthenticationController;
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
	AuthenticationController mockAuthController;
	@Mock
	RowFormView mockView;
	@Mock
	CellFactory mockCellFactory;

	List<ColumnModel> types;
	List<CellStub> cellStubs;
	String tableId;
	RowFormEditorWidget rowWidget;

	public static final String CURRENT_USER_ID = "12984";

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
		when(mockCellFactory.createFormEditor(any(ColumnModel.class))).thenAnswer(answer);
		types = TableModelTestUtils.createOneOfEachType();
		rowWidget = new RowFormEditorWidget(mockAuthController, mockView, mockCellFactory);
		when(mockAuthController.getCurrentUserPrincipalId()).thenReturn(CURRENT_USER_ID);
	}

	@Test
	public void testConfgureEditor() {
		rowWidget.configure(tableId, types);
		Row extracted = rowWidget.getRow();
		assertEquals(types.size(), extracted.getValues().size());
		verify(mockView, times(types.size())).addCell(anyString(), any(Cell.class));
	}

	@Test
	public void testIsValid() {
		rowWidget.configure(tableId, types);
		assertTrue(rowWidget.isValid());
		cellStubs.get(4).setIsValid(false);
		assertFalse(rowWidget.isValid());
	}

	@Test
	public void testSubmitterColumns() {
		// set the date column name to 'submissionTimestamp' and user column to 'submitterUserId'
		// verify that these column values are automatically filled in when we get the row!
		int userIdColIndex = -1, timestampColIndex = -1;
		for (int i = 0; i < types.size(); i++) {
			ColumnModel cm = types.get(i);
			if (ColumnType.USERID.equals(cm.getColumnType())) {
				userIdColIndex = i;
				cm.setName(RowFormEditorWidget.SUBMITTER_USER_ID_COLUMN_NAME);
			} else if (ColumnType.DATE.equals(cm.getColumnType())) {
				timestampColIndex = i;
				cm.setName(RowFormEditorWidget.SUBMISSION_TIMESTAMP_COLUMN_NAME);
			}
		}

		rowWidget.configure(tableId, types);

		// verify that the special submitter cells were not added to the view.
		verify(mockView, times(types.size() - 2)).addCell(anyString(), any(Cell.class));
		Row extracted = rowWidget.getRow();
		assertEquals(types.size(), extracted.getValues().size());

		String userIdInRow = extracted.getValues().get(userIdColIndex);
		assertEquals(CURRENT_USER_ID, userIdInRow);

		String timestamp = extracted.getValues().get(timestampColIndex);
		assertNotNull(timestamp);
		new Date(Long.parseLong(timestamp));
	}
}
