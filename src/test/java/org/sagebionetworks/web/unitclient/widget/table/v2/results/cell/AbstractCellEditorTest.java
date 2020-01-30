package org.sagebionetworks.web.unitclient.widget.table.v2.results.cell;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellEditorView;

public class AbstractCellEditorTest {

	CellEditorView mockView;
	TestAbstractCellEditor editor;

	@Before
	public void before() {
		mockView = Mockito.mock(CellEditorView.class);
		editor = new TestAbstractCellEditor(mockView);
	}

	@Test
	public void testSetValue() {
		String value = "value";
		editor.setValue(value);
		verify(mockView).setValue(value);
	}

	@Test
	public void testGetValue() {
		String value = "value";
		when(mockView.getValue()).thenReturn(value);
		assertEquals(value, editor.getValue());
	}
}
