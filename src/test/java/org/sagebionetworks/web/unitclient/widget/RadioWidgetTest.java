package org.sagebionetworks.web.unitclient.widget;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.widget.RadioWidget;
import org.sagebionetworks.web.client.widget.RadioWidgetView;

import com.google.gwt.user.client.ui.Widget;

public class RadioWidgetTest {
	@Mock
	RadioWidgetView mockView;
	@Mock
	Widget mockWidget;
	RadioWidget widget;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		widget = new RadioWidget(mockView);
	}

	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}

	@Test
	public void testAdd() {
		widget.add(mockWidget);
		verify(mockView).add(mockWidget);
	}

	@Test
	public void testClear() {
		widget.clear();
		verify(mockView).clear();
	}

	@Test
	public void testIterator() {
		widget.iterator();
		verify(mockView).iterator();
	}

	@Test
	public void testRemove() {
		widget.remove(mockWidget);
		verify(mockView).remove(mockWidget);
	}

}
