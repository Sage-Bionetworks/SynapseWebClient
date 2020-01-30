package org.sagebionetworks.web.unitclient.widget.table.v2.results.cell;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.DateCellRenderer;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.StringRendererCellView;

public class DateCellRendererImplTest {

	DateCellRenderer renderer;
	@Mock
	StringRendererCellView mockStringRendererCell;
	@Mock
	DateTimeUtils mockDateTimeUtils;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		renderer = new DateCellRenderer(mockStringRendererCell, mockDateTimeUtils);
	}

	@Test
	public void testSetValue() {
		String dateString = "a formatted date string";
		when(mockDateTimeUtils.getDateTimeString(any(Date.class))).thenReturn(dateString);
		renderer.setValue("1");
		verify(mockStringRendererCell).setValue(dateString);
	}

	@Test
	public void testSetValueEmpty() {
		renderer.setValue("");
		verify(mockStringRendererCell).setValue("");
	}

	@Test
	public void testSetValueNull() {
		renderer.setValue(null);
		verify(mockStringRendererCell).setValue("");
	}

	// SWC-2944: Note that the renderer does not use a bootstrap DateTimePicker, only the editor uses
	// this component.
}
