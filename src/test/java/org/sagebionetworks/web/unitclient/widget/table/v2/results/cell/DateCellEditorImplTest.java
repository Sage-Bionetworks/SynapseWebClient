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
		//get value from the editor, setValue is never called.
		Date old = new Date(123);
		when(mockView.getValue()).thenReturn(old);
		assertEquals("123", editor.getValue());
	}
	
	@Test
	public void testSetValueMilliSecondLoss(){
		//SWC-2944: Simulate precision loss by the view (Bootstrap extras DateTimePicker to be more specific).
		String sValue = Long.toString(1457401179123L);
		editor.setValue(sValue);
		when(mockView.getValue()).thenReturn(new Date(1457401179000L));
		
		//the view has lost the milliseconds, should return the original value
		assertEquals(sValue, editor.getValue());
	}
	
	@Test
	public void testSetValueViewHasDifferentDate(){
		//SWC-2944
		String sValue = Long.toString(1457401179123L);
		editor.setValue(sValue);
		Long newDate = 1457487584000L;
		when(mockView.getValue()).thenReturn(new Date(newDate));
		
		//the user has selected a different value in the view, verify that it is not overwritten by the original value
		assertEquals(Long.toString(newDate), editor.getValue());
	}

}
