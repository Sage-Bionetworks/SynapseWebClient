package org.sagebionetworks.web.unitclient.widget.table.v2.results.cell;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.sagebionetworks.web.client.widget.table.v2.results.cell.DateCellEditorImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.DateCellEditorView;

public class DateCellEditorImplTest {
	
	DateCellEditorView mockView;
	DateCellEditorImpl editor;

	@Before
	public void before(){
		mockView = Mockito.mock(DateCellEditorView.class);
		editor = new DateCellEditorImpl(mockView);
	}
	
	@Test
	public void testSetValueNull(){
		editor.setValue(null);
		verify(mockView).setValue(null);
	}
	
	@Test
	public void testSetValueEmpty(){
		editor.setValue("");
		// empty should be treated as null
		verify(mockView).setValue(null);
	}
	
	@Test
	public void testSetValueReal(){
		Date now = new Date(System.currentTimeMillis());
		String sValue = Long.toString(now.getTime());
		editor.setValue(sValue);
		verify(mockView).setValue(eq(now));
	}
	
	@Test
	public void testGetValueNull(){
		when(mockView.getValue()).thenReturn(null);
		assertEquals(null, editor.getValue());
	}
	
	@Test
	public void testGetValueReal(){
		Date old = new Date(123);
		when(mockView.getValue()).thenReturn(old);
		assertEquals("123", editor.getValue());
	}
}
