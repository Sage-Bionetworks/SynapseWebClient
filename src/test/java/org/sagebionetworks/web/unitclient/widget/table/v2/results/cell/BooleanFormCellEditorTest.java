package org.sagebionetworks.web.unitclient.widget.table.v2.results.cell;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.widget.table.v2.results.cell.BooleanCellEditor.FALSE;
import static org.sagebionetworks.web.client.widget.table.v2.results.cell.BooleanCellEditor.TRUE;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.BooleanFormCellEditor;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.RadioCellEditorView;

public class BooleanFormCellEditorTest {

	RadioCellEditorView mockView;
	BooleanFormCellEditor editor;

	@Before
	public void before() {
		mockView = Mockito.mock(RadioCellEditorView.class);
		editor = new BooleanFormCellEditor(mockView);
	}

	@Test
	public void testConfigure() {
		verify(mockView).configure(Arrays.asList(TRUE, FALSE));
	}

	@Test
	public void testSetNull() {
		editor.setValue(null);
		verify(mockView, never()).setValue(anyInt());
	}

	@Test
	public void testSetEmpty() {
		editor.setValue("");
		verify(mockView, never()).setValue(anyInt());
	}

	@Test
	public void testSetTrue() {
		editor.setValue("True");
		verify(mockView).setValue(0);
	}

	@Test
	public void testSetFalse() {
		editor.setValue("False");
		verify(mockView).setValue(1);
	}

	@Test
	public void testGetNothingSelected() {
		when(mockView.getValue()).thenReturn(null);
		assertEquals(null, editor.getValue());
	}

	@Test
	public void testGetTrue() {
		when(mockView.getValue()).thenReturn(0);
		assertEquals("true", editor.getValue());
	}

	@Test
	public void testGetFalse() {
		when(mockView.getValue()).thenReturn(1);
		assertEquals("false", editor.getValue());
	}
}
