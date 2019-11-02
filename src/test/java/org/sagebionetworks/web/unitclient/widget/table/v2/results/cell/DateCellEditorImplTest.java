package org.sagebionetworks.web.unitclient.widget.table.v2.results.cell;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.GlobalApplicationStateImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.DateCellEditor;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.DateCellEditorView;

public class DateCellEditorImplTest {
	DateCellEditor editor;
	@Mock
	DateCellEditorView mockView;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		when(mockGlobalApplicationState.isShowingUTCTime()).thenReturn(false);
		editor = new DateCellEditor(mockView, mockGlobalApplicationState);
	}

	@Test
	public void testSetValueNull() {
		editor.setValue(null);
		verify(mockView).setValue(null);
	}

	@Test
	public void testSetValueEmpty() {
		editor.setValue("");
		// empty should be treated as null
		verify(mockView).setValue(null);
	}

	@Test
	public void testSetValueReal() {
		Date now = new Date(System.currentTimeMillis());
		String sValue = Long.toString(now.getTime());
		editor.setValue(sValue);
		verify(mockView).setValue(eq(now));
	}

	@Test
	public void testUTCTime() {
		when(mockGlobalApplicationState.isShowingUTCTime()).thenReturn(true);
		Date now = new Date(System.currentTimeMillis());
		editor.setValue(Long.toString(now.getTime()));
		Date offsetDate = new Date(now.getTime() + GlobalApplicationStateImpl.getTimezoneOffsetMs());
		verify(mockView).setValue(eq(offsetDate));
	}

	@Test
	public void testGetUTCTime() {
		when(mockGlobalApplicationState.isShowingUTCTime()).thenReturn(true);
		Date now = new Date(System.currentTimeMillis());
		String sValue = Long.toString(1L);
		editor.setValue(sValue);
		Long newDate = now.getTime();
		when(mockView.getValue()).thenReturn(new Date(newDate));
		Long offsetNewDate = newDate - GlobalApplicationStateImpl.getTimezoneOffsetMs();
		assertEquals(Long.toString(offsetNewDate), editor.getValue());
	}

	@Test
	public void testGetValueNull() {
		when(mockView.getValue()).thenReturn(null);
		assertEquals(null, editor.getValue());
	}

	@Test
	public void testGetValueReal() {
		// get value from the editor, setValue is never called.
		Date old = new Date(123);
		when(mockView.getValue()).thenReturn(old);
		assertEquals("123", editor.getValue());
	}

	@Test
	public void testSetValueMilliSecondLoss() {
		// SWC-2944: Simulate precision loss by the view (Bootstrap extras DateTimePicker to be more
		// specific).
		String sValue = Long.toString(1457401179123L);
		editor.setValue(sValue);
		when(mockView.getValue()).thenReturn(new Date(1457401179000L));

		// the view has lost the milliseconds, should return the original value
		assertEquals(sValue, editor.getValue());
	}

	@Test
	public void testSetValueViewHasDifferentDate() {
		// SWC-2944
		String sValue = Long.toString(1457401179123L);
		editor.setValue(sValue);
		Long newDate = 1457487584000L;
		when(mockView.getValue()).thenReturn(new Date(newDate));

		// the user has selected a different value in the view, verify that it is not overwritten by the
		// original value
		assertEquals(Long.toString(newDate), editor.getValue());
	}

}
