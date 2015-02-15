package org.sagebionetworks.web.unitclient.widget.table.v2.results.cell;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

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

}
