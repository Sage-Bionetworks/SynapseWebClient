package org.sagebionetworks.web.unitclient.widget.table.v2.results.cell;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.sagebionetworks.web.client.widget.table.v2.results.cell.BooleanCellEditorImpl.FALSE;
import static org.sagebionetworks.web.client.widget.table.v2.results.cell.BooleanCellEditorImpl.NOTHING_SELECTED;
import static org.sagebionetworks.web.client.widget.table.v2.results.cell.BooleanCellEditorImpl.TRUE;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.BooleanCellEditorImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.BooleanFormCellEditorImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.ListCellEdtiorView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.RadioCellEditorView;

public class BooleanFormCellEditorTest {

	RadioCellEditorView mockView;
	BooleanFormCellEditorImpl editor;
	
	@Before
	public void before(){
		mockView = Mockito.mock(RadioCellEditorView.class);
		editor = new BooleanFormCellEditorImpl(mockView);
	}

	@Test
	public void testConfigure(){
		verify(mockView).configure(Arrays.asList(TRUE, FALSE));
	}
	
	@Test
	public void testSetNull(){
		editor.setValue(null);
		verify(mockView, never()).setValue(anyInt());
	}
	
	@Test
	public void testSetEmpty(){
		editor.setValue("");
		verify(mockView, never()).setValue(anyInt());
	}
	
	@Test
	public void testSetTrue(){
		editor.setValue("True");
		verify(mockView).setValue(0);
	}
	
	@Test
	public void testSetFalse(){
		editor.setValue("False");
		verify(mockView).setValue(1);
	}
	
	@Test
	public void testGetNothingSelected(){
		when(mockView.getValue()).thenReturn(null);
		assertEquals(null, editor.getValue());
	}
	
	@Test
	public void testGetTrue(){
		when(mockView.getValue()).thenReturn(0);
		assertEquals("true", editor.getValue());
	}
	
	@Test
	public void testGetFalse(){
		when(mockView.getValue()).thenReturn(1);
		assertEquals("false", editor.getValue());
	}
}
