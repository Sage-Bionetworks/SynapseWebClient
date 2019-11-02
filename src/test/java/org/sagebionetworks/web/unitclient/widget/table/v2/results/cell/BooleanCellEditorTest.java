package org.sagebionetworks.web.unitclient.widget.table.v2.results.cell;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.widget.table.v2.results.cell.BooleanCellEditor.FALSE;
import static org.sagebionetworks.web.client.widget.table.v2.results.cell.BooleanCellEditor.NOTHING_SELECTED;
import static org.sagebionetworks.web.client.widget.table.v2.results.cell.BooleanCellEditor.TRUE;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.BooleanCellEditor;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.ListCellEdtiorView;

public class BooleanCellEditorTest {

	ListCellEdtiorView mockView;
	BooleanCellEditor editor;

	@Before
	public void before() {
		mockView = Mockito.mock(ListCellEdtiorView.class);
		editor = new BooleanCellEditor(mockView);
	}

	@Test
	public void testConfigure() {
		verify(mockView).configure(Arrays.asList(NOTHING_SELECTED, TRUE, FALSE));
	}

	@Test
	public void testSetNull() {
		editor.setValue(null);
		// Null should set the value to "nothing selected" at index zero
		verify(mockView).setValue(0);
	}

	@Test
	public void testSetEmpty() {
		editor.setValue("");
		// same as null
		verify(mockView).setValue(0);
	}

	@Test
	public void testSetTrue() {
		editor.setValue("True");
		verify(mockView).setValue(1);
	}

	@Test
	public void testSetFalse() {
		editor.setValue("False");
		verify(mockView).setValue(2);
	}

	@Test
	public void testGetNothingSelected() {
		when(mockView.getValue()).thenReturn(0);
		assertEquals(null, editor.getValue());
	}

	@Test
	public void testGetTrue() {
		when(mockView.getValue()).thenReturn(1);
		assertEquals("true", editor.getValue());
	}

	@Test
	public void testGetFalse() {
		when(mockView.getValue()).thenReturn(2);
		assertEquals("false", editor.getValue());
	}
}
