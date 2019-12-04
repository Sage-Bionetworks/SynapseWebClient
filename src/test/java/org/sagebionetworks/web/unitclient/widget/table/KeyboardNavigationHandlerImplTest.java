package org.sagebionetworks.web.unitclient.widget.table;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.web.client.widget.table.FocusSetter;
import org.sagebionetworks.web.client.widget.table.KeyboardNavigationHandler.RowOfWidgets;
import org.sagebionetworks.web.client.widget.table.KeyboardNavigationHandlerImpl;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.user.client.ui.IsWidget;

public class KeyboardNavigationHandlerImplTest {

	FocusSetter mockFocusSetter;
	KeyDownEvent mockEnterEvent;
	KeyDownEvent mockDownArrowEvent;
	KeyDownEvent mockUpArrowEvent;
	KeyDownEvent mockLeftArrowEvent;
	KeyDownEvent mockRightArrowEvent;

	KeyboardNavigationHandlerImpl keyboardNavigation;
	IsWidgetStub[][] table;
	List<RowOfWidgets> rows;
	int rowCount;
	int columnCount;

	@Before
	public void before() {
		mockFocusSetter = Mockito.mock(FocusSetter.class);
		// Key events
		mockEnterEvent = Mockito.mock(KeyDownEvent.class);
		when(mockEnterEvent.getNativeKeyCode()).thenReturn(KeyCodes.KEY_ENTER);
		// down
		mockDownArrowEvent = Mockito.mock(KeyDownEvent.class);
		when(mockDownArrowEvent.getNativeKeyCode()).thenReturn(KeyCodes.KEY_DOWN);
		// up
		mockUpArrowEvent = Mockito.mock(KeyDownEvent.class);
		when(mockUpArrowEvent.getNativeKeyCode()).thenReturn(KeyCodes.KEY_UP);
		// left
		mockLeftArrowEvent = Mockito.mock(KeyDownEvent.class);
		when(mockLeftArrowEvent.getNativeKeyCode()).thenReturn(KeyCodes.KEY_LEFT);
		// right
		mockRightArrowEvent = Mockito.mock(KeyDownEvent.class);
		when(mockRightArrowEvent.getNativeKeyCode()).thenReturn(KeyCodes.KEY_RIGHT);

		keyboardNavigation = new KeyboardNavigationHandlerImpl(mockFocusSetter);
		// Setup a table of widgets.
		rowCount = 3;
		columnCount = 3;
		table = new IsWidgetStub[rowCount][columnCount];
		rows = new ArrayList<RowOfWidgets>();
		for (int row = 0; row < rowCount; row++) {
			for (int col = 0; col < columnCount; col++) {
				table[row][col] = new IsWidgetStub(row, col);
			}
			RowOfWidgetsStub rowStub = new RowOfWidgetsStub(Arrays.asList(table[row]));
			rows.add(rowStub);
		}
		// Bind all of the rows.
		for (RowOfWidgets row : rows) {
			keyboardNavigation.bindRow(row);
		}
	}

	@Test
	public void testEnterKey() {
		// Fire a enter event on the first row.
		table[0][1].fireEvent(mockEnterEvent);
		// should trigger focus on the cell bellow
		verify(mockFocusSetter).attemptSetFocus(table[1][1], true);
	}

	@Test
	public void testEnterAtBottom() {
		// Fire a enter event on the last row.
		table[rowCount - 1][0].fireEvent(mockEnterEvent);
		// should not trigger an event
		verify(mockFocusSetter, never()).attemptSetFocus(any(IsWidget.class), anyBoolean());
	}

	@Test
	public void testDownArrow() {
		// Fire a down event on the first row
		table[0][1].fireEvent(mockDownArrowEvent);
		// should trigger focus on the cell bellow
		verify(mockFocusSetter).attemptSetFocus(table[1][1], true);
	}

	@Test
	public void testDownArrowAtBottom() {
		// Fire a down event on the last row.
		table[rowCount - 1][0].fireEvent(mockDownArrowEvent);
		// should not trigger an event
		verify(mockFocusSetter, never()).attemptSetFocus(any(IsWidget.class), anyBoolean());
	}

	@Test
	public void testUpArrow() {
		// Fire a down event on the first cell.
		table[1][1].fireEvent(mockUpArrowEvent);
		// should trigger focus on the cell bellow
		verify(mockFocusSetter).attemptSetFocus(table[0][1], true);
	}

	@Test
	public void testUpArrowAtTop() {
		// Fire a down event on the first cell.
		table[0][1].fireEvent(mockUpArrowEvent);
		// should not trigger an event
		verify(mockFocusSetter, never()).attemptSetFocus(any(IsWidget.class), anyBoolean());
	}

	@Test
	public void testLeftArrowKey() {
		// Fire a left the second column
		table[1][1].fireEvent(mockLeftArrowEvent);
		// should trigger on the first column.
		verify(mockFocusSetter).attemptSetFocus(table[1][0], true);
	}

	@Test
	public void testLeftArrowAtLeft() {
		// Fire a left on the first column.
		table[1][0].fireEvent(mockLeftArrowEvent);
		// should not trigger an event
		verify(mockFocusSetter, never()).attemptSetFocus(any(IsWidget.class), anyBoolean());
	}

	@Test
	public void testRightArrowKey() {
		// Fire a right on the second column
		table[1][1].fireEvent(mockRightArrowEvent);
		// should trigger on the first column.
		verify(mockFocusSetter).attemptSetFocus(table[1][2], true);
	}

	@Test
	public void testRightOnLastColumn() {
		// Fire a right on the last column.
		table[1][columnCount - 1].fireEvent(mockRightArrowEvent);
		// should not trigger an event
		verify(mockFocusSetter, never()).attemptSetFocus(any(IsWidget.class), anyBoolean());
	}

	@Test
	public void testRemoveRow() {
		// If we remove the middle row, then a down event from the first row should focus on the last row.
		keyboardNavigation.removeRow(rows.get(1));
		// fire a down from the firt row.
		table[0][1].fireEvent(mockDownArrowEvent);
		// should trigger focus on the cell bellow
		verify(mockFocusSetter).attemptSetFocus(table[2][1], true);
	}

	@Test
	public void testKeyDownAfterRemove() {
		// Remove the middle row.
		keyboardNavigation.removeRow(rows.get(1));
		// Any event fired from the removed row should have no effect.
		table[1][1].fireEvent(mockDownArrowEvent);
		// should trigger focus on the cell bellow
		verify(mockFocusSetter, never()).attemptSetFocus(any(IsWidget.class), anyBoolean());
	}

	@Test
	public void testKeyDownAfterRemoveAll() {
		// Remove all rows
		keyboardNavigation.removeAllRows();
		// Any event fired from the removed row should have no effect.
		table[1][1].fireEvent(mockDownArrowEvent);
		// should trigger focus on the cell bellow
		verify(mockFocusSetter, never()).attemptSetFocus(any(IsWidget.class), anyBoolean());
	}

	@Test
	public void testDoubleBind() {
		// bind a row that is already bound. This should move that row to the last row.
		keyboardNavigation.bindRow(rows.get(1));
		// Now fire an event down on this row.
		table[1][1].fireEvent(mockDownArrowEvent);
		// If the row is moved to the bottom then a key down should have no effect.
		verify(mockFocusSetter, never()).attemptSetFocus(any(IsWidget.class), anyBoolean());
		// a move up should work
		table[1][1].fireEvent(mockUpArrowEvent);
		// The last row should now be the middle row so a move up from the bottom should land there.
		verify(mockFocusSetter).attemptSetFocus(table[2][1], true);
	}

}
