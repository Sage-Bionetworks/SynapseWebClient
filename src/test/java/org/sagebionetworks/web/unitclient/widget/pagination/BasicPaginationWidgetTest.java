package org.sagebionetworks.web.unitclient.widget.pagination;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.widget.pagination.BasicPaginationView;
import org.sagebionetworks.web.client.widget.pagination.BasicPaginationWidget;
import org.sagebionetworks.web.client.widget.pagination.PageChangeListener;

public class BasicPaginationWidgetTest {

	@Mock
	PageChangeListener mockPageChangeListener;
	@Mock
	BasicPaginationView mockView;

	BasicPaginationWidget widget;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		widget = new BasicPaginationWidget(mockView);
	}

	@Test
	public void testZeroRows() {
		long limit = 10;
		long offset = 0;
		long currentRowCount = 0;
		widget.configure(limit, offset, currentRowCount, mockPageChangeListener);
		verify(mockView).setNextVisible(false);
		verify(mockView).setPreviousVisible(false);
		long currentPageNumber = 1;
		verify(mockView).setCurrentPage(currentPageNumber);
		verify(mockView).setVisible(false);
	}

	@Test
	public void testLessThanAPage() {
		long limit = 10;
		long offset = 0;
		long currentRowCount = 5;
		widget.configure(limit, offset, currentRowCount, mockPageChangeListener);
		verify(mockView).setNextVisible(false);
		verify(mockView).setPreviousVisible(false);
		long currentPageNumber = 1;
		verify(mockView).setCurrentPage(currentPageNumber);
		verify(mockView).setVisible(false);
	}


	@Test
	public void testOnePage() {
		long limit = 10;
		long offset = 0;
		long currentRowCount = 10;
		widget.configure(limit, offset, currentRowCount, mockPageChangeListener);
		// full page of results, will show next page button (even if these are all of the results)
		verify(mockView).setNextVisible(true);
		verify(mockView).setPreviousVisible(false);
		long currentPageNumber = 1;
		verify(mockView).setCurrentPage(currentPageNumber);
		verify(mockView).setVisible(true);
	}

	@Test
	public void testTwoOfThreePages() {
		long limit = 10;
		long offset = limit * 1;
		long currentRowCount = 10;
		widget.configure(limit, offset, currentRowCount, mockPageChangeListener);
		verify(mockView).setNextVisible(true);
		verify(mockView).setPreviousVisible(true);
		long currentPageNumber = 2;
		verify(mockView).setCurrentPage(currentPageNumber);
		verify(mockView).setVisible(true);
	}

	@Test
	public void testThreeOfThreePages() {
		long limit = 10;
		long offset = limit * 2;
		long currentRowCount = 1;
		widget.configure(limit, offset, currentRowCount, mockPageChangeListener);
		verify(mockView).setNextVisible(false);
		verify(mockView).setPreviousVisible(true);
		long currentPageNumber = 3;
		verify(mockView).setCurrentPage(currentPageNumber);
		verify(mockView).setVisible(true);
	}

	@Test
	public void testOnNext() {
		long limit = 10;
		long offset = limit * 1;
		long currentRowCount = 10;
		widget.configure(limit, offset, currentRowCount, mockPageChangeListener);
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
		long currentRowCount = 10;
		widget.configure(limit, offset, currentRowCount, mockPageChangeListener);
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
