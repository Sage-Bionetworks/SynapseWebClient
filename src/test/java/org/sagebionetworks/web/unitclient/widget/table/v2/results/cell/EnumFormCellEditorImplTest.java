package org.sagebionetworks.web.unitclient.widget.table.v2.results.cell;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import javax.annotation.meta.When;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EnumCellEditorImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EnumFormCellEditorImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.ListCellEdtiorView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.RadioCellEditorView;

public class EnumFormCellEditorImplTest {
	
	RadioCellEditorView mockView;
	EnumFormCellEditorImpl editor;
	
	@Before
	public void before(){
		mockView = Mockito.mock(RadioCellEditorView.class);
		editor = new EnumFormCellEditorImpl(mockView);
	}

	@Test
	public void testConfigure(){
		List<String> values = Arrays.asList("one", "two");
		editor.configure(values);
		verify(mockView).configure(Arrays.asList("one", "two"));
	}
	
	@Test
	public void testSetNull(){
		List<String> values = Arrays.asList("one", "two");
		editor.configure(values);
		editor.setValue(null);
		verify(mockView, never()).setValue(anyInt());
	}
	
	@Test
	public void testSetEmpty(){
		List<String> values = Arrays.asList("one", "two");
		editor.configure(values);
		editor.setValue("");
		verify(mockView, never()).setValue(anyInt());
	}
	
	@Test
	public void testSetValue(){
		List<String> values = Arrays.asList("one", "two");
		editor.configure(values);
		editor.setValue("one");
		verify(mockView).setValue(0);
	}
	
	@Test
	public void testGetNull(){
		when(mockView.getValue()).thenReturn(null);
		List<String> values = Arrays.asList("one", "two");
		editor.configure(values);
		assertEquals(null, editor.getValue());
	}
	
	@Test
	public void testGetValue(){
		when(mockView.getValue()).thenReturn(1);
		List<String> values = Arrays.asList("one", "two");
		editor.configure(values);
		assertEquals("two", editor.getValue());
	}
	
	
}
