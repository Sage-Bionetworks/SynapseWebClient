package org.sagebionetworks.web.unitclient.widget.pagination.countbased;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.web.client.widget.pagination.BasicPaginationView;
import org.sagebionetworks.web.client.widget.pagination.PageChangeListener;
import org.sagebionetworks.web.client.widget.pagination.countbased.BasicPaginationWidget;

public class BasicPaginationWidgetTest {

	PageChangeListener mockPageChangeListener;
	BasicPaginationView mockView;
	BasicPaginationWidget widget;

	@Before
	public void before() {
		mockView = Mockito.mock(BasicPaginationView.class);
		mockPageChangeListener = Mockito.mock(PageChangeListener.class);
		widget = new BasicPaginationWidget(mockView);
	}

	@Test
	public void testCountZero() {
		long limit = 10;
		long offset = 0;
		long count = 0;
		widget.configure(limit, offset, count, mockPageChangeListener);
		verify(mockView).setNextVisible(false);
		verify(mockView).setPreviousVisible(false);
		long currentPageNumber = 1;
		verify(mockView).setCurrentPage(currentPageNumber);
	}

	@Test
	public void testOnePage() {
		long limit = 10;
		long offset = 0;
		long count = 10;
		widget.configure(limit, offset, count, mockPageChangeListener);
		verify(mockView).setNextVisible(false);
		verify(mockView).setPreviousVisible(false);
		long currentPageNumber = 1;
		verify(mockView).setCurrentPage(currentPageNumber);
	}

	@Test
	public void testOneOfThreePages() {
		long limit = 10;
		long offset = limit * 0;
		long count = 21;
		widget.configure(limit, offset, count, mockPageChangeListener);
		verify(mockView).setNextVisible(true);
		verify(mockView).setPreviousVisible(false);
		long currentPageNumber = 1;
		verify(mockView).setCurrentPage(currentPageNumber);
	}

	@Test
	public void testTwoOfThreePages() {
		long limit = 10;
		long offset = limit * 1;
		long count = 21;
		widget.configure(limit, offset, count, mockPageChangeListener);
		verify(mockView).setNextVisible(true);
		verify(mockView).setPreviousVisible(true);
		long currentPageNumber = 2;
		verify(mockView).setCurrentPage(currentPageNumber);
	}

	@Test
	public void testThreeOfThreePages() {
		long limit = 10;
		long offset = limit * 2;
		long count = 21;
		widget.configure(limit, offset, count, mockPageChangeListener);
		verify(mockView).setNextVisible(false);
		verify(mockView).setPreviousVisible(true);
		long currentPageNumber = 3;
		verify(mockView).setCurrentPage(currentPageNumber);
	}

	@Test
	public void testOnNext() {
		long limit = 10;
		long offset = limit * 1;
		long count = 21;
		widget.configure(limit, offset, count, mockPageChangeListener);
		verify(mockView).setNextVisible(true);
		verify(mockView).setPreviousVisible(true);
		reset(mockView);
		// on next
		widget.onNext();
		verify(mockView).setNextVisible(false);
		verify(mockView).setPreviousVisible(false);
		verify(mockPageChangeListener).onPageChange(20L);
	}

	@Test
	public void testOnPrevious() {
		long limit = 10;
		long offset = limit * 1;
		long count = 21;
		widget.configure(limit, offset, count, mockPageChangeListener);
		verify(mockView).setNextVisible(true);
		verify(mockView).setPreviousVisible(true);
		reset(mockView);
		// on previous
		widget.onPrevious();
		verify(mockView).setNextVisible(false);
		verify(mockView).setPreviousVisible(false);
		verify(mockPageChangeListener).onPageChange(0L);
	}
}
