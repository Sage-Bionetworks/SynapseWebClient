package org.sagebionetworks.web.unitclient.widget.table.v2.results.cell;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EnumFormCellEditor;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.RadioCellEditorView;

public class EnumFormCellEditorImplTest {

	RadioCellEditorView mockView;
	EnumFormCellEditor editor;

	@Before
	public void before() {
		mockView = Mockito.mock(RadioCellEditorView.class);
		editor = new EnumFormCellEditor(mockView);
	}

	@Test
	public void testConfigure() {
		List<String> values = Arrays.asList("one", "two");
		editor.configure(values);
		verify(mockView).configure(Arrays.asList("one", "two"));
	}

	@Test
	public void testSetNull() {
		List<String> values = Arrays.asList("one", "two");
		editor.configure(values);
		editor.setValue(null);
		verify(mockView, never()).setValue(anyInt());
	}

	@Test
	public void testSetEmpty() {
		List<String> values = Arrays.asList("one", "two");
		editor.configure(values);
		editor.setValue("");
		verify(mockView, never()).setValue(anyInt());
	}

	@Test
	public void testSetValue() {
		List<String> values = Arrays.asList("one", "two");
		editor.configure(values);
		editor.setValue("one");
		verify(mockView).setValue(0);
	}

	@Test
	public void testGetNull() {
		when(mockView.getValue()).thenReturn(null);
		List<String> values = Arrays.asList("one", "two");
		editor.configure(values);
		assertEquals(null, editor.getValue());
	}

	@Test
	public void testGetValue() {
		when(mockView.getValue()).thenReturn(1);
		List<String> values = Arrays.asList("one", "two");
		editor.configure(values);
		assertEquals("two", editor.getValue());
	}


}
