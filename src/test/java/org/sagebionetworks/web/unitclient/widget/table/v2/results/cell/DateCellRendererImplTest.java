package org.sagebionetworks.web.unitclient.widget.table.v2.results.cell;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import org.sagebionetworks.web.client.widget.table.v2.results.cell.DateCellRendererImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.DateCellRendererView;

public class DateCellRendererImplTest {
	
	DateCellRendererView mockView;
	DateCellRendererImpl renderer;
	
	@Before
	public void before(){
		mockView = Mockito.mock(DateCellRendererView.class);
		renderer = new DateCellRendererImpl(mockView);
		verify(mockView).setFormat(DateCellRendererImpl.FORMAT);
	}
	
	@Test
	public void testSetValue(){
		renderer.setValue("1");
		verify(mockView).setValue(new Date(1));
	}
	
	@Test
	public void testSetValueEmpty(){
		renderer.setValue("");
		// Should be cleared and not passed a null date
		verify(mockView).clear();
		verify(mockView, never()).setValue(any(Date.class));
	}
	
	@Test
	public void testSetValueNull(){
		renderer.setValue(null);
		// Should be cleared and not passed a null date
		verify(mockView).clear();
		verify(mockView, never()).setValue(any(Date.class));
	}
	
	@Test
	public void testSetValueMilliSecondLoss(){
		//SWC-2944: Simulate precision loss by the view (Bootstrap extras DateTimePicker to be more specific).
		String sValue = Long.toString(1457401179123L);
		renderer.setValue(sValue);
		when(mockView.getValue()).thenReturn(new Date(1457401179000L));
		
		//the view has lost the milliseconds, should return the original value
		assertEquals(sValue, renderer.getValue());
	}
	
	@Test
	public void testSetValueViewHasDifferentDate(){
		//SWC-2944
		String sValue = Long.toString(1457401179123L);
		renderer.setValue(sValue);
		Long newDate = 1457487584000L;
		when(mockView.getValue()).thenReturn(new Date(newDate));
		
		//the user has selected a different value in the view, verify that it is not overwritten by the original value
		assertEquals(Long.toString(newDate), renderer.getValue());
	}

}
