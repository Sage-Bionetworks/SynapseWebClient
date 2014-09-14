package org.sagebionetworks.web.unitclient.widget.pagination;

import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;
import org.mockito.Mockito;
import org.sagebionetworks.web.client.widget.pagination.BasicPaginationView;
import org.sagebionetworks.web.client.widget.pagination.BasicPaginationWidget;
import org.sagebionetworks.web.client.widget.pagination.PageChangeListener;

public class BasicPaginationWidgetTest {
	
	PageChangeListener mockPageChangeListener;
	BasicPaginationView mockView;
	BasicPaginationWidget widget;
	
	@Before
	public void before(){
		mockView = Mockito.mock(BasicPaginationView.class);
		mockPageChangeListener = Mockito.mock(PageChangeListener.class);
		widget = new BasicPaginationWidget(mockView);
	}
	
	@Test
	public void testCountZero(){
		long limit = 10;
		long offset = 0;
		long count = 0;
		widget.configure(limit, offset, count, mockPageChangeListener);
		verify(mockView).setNextEnabled(false);
		verify(mockView).setPreviousEnabled(false);
		long currentPageNumber = 1;
		long totalNumberOfPages = 1;
		verify(mockView).setPageNumbers(currentPageNumber, totalNumberOfPages);
	}
	
	@Test
	public void testOnePage(){
		long limit = 10;
		long offset = 0;
		long count = 10;
		widget.configure(limit, offset, count, mockPageChangeListener);
		verify(mockView).setNextEnabled(false);
		verify(mockView).setPreviousEnabled(false);
		long currentPageNumber = 1;
		long totalNumberOfPages = 1;
		verify(mockView).setPageNumbers(currentPageNumber, totalNumberOfPages);
	}
	
	@Test
	public void testOneOfThreePages(){
		long limit = 10;
		long offset = limit*0;
		long count = 21;
		widget.configure(limit, offset, count, mockPageChangeListener);
		verify(mockView).setNextEnabled(true);
		verify(mockView).setPreviousEnabled(false);
		long currentPageNumber = 1;
		long totalNumberOfPages = 3;
		verify(mockView).setPageNumbers(currentPageNumber, totalNumberOfPages);
	}

	@Test
	public void testTwoOfThreePages(){
		long limit = 10;
		long offset = limit*1;
		long count = 21;
		widget.configure(limit, offset, count, mockPageChangeListener);
		verify(mockView).setNextEnabled(true);
		verify(mockView).setPreviousEnabled(true);
		long currentPageNumber = 2;
		long totalNumberOfPages = 3;
		verify(mockView).setPageNumbers(currentPageNumber, totalNumberOfPages);
	}
	
	@Test
	public void testThreeOfThreePages(){
		long limit = 10;
		long offset = limit*2;
		long count = 21;
		widget.configure(limit, offset, count, mockPageChangeListener);
		verify(mockView).setNextEnabled(false);
		verify(mockView).setPreviousEnabled(true);
		long currentPageNumber = 3;
		long totalNumberOfPages = 3;
		verify(mockView).setPageNumbers(currentPageNumber, totalNumberOfPages);
	}
	
	@Test
	public void testOnNext(){
		long limit = 10;
		long offset = limit*1;
		long count = 21;
		widget.configure(limit, offset, count, mockPageChangeListener);
		verify(mockView).setNextEnabled(true);
		verify(mockView).setPreviousEnabled(true);
		reset(mockView);
		// on next
		widget.onNext();
		verify(mockView).setNextEnabled(false);
		verify(mockView).setPreviousEnabled(false);
		verify(mockPageChangeListener).onPageChange(20L);
	}
	
	@Test
	public void testOnPrevious(){
		long limit = 10;
		long offset = limit*1;
		long count = 21;
		widget.configure(limit, offset, count, mockPageChangeListener);
		verify(mockView).setNextEnabled(true);
		verify(mockView).setPreviousEnabled(true);
		reset(mockView);
		// on previous
		widget.onPrevious();
		verify(mockView).setNextEnabled(false);
		verify(mockView).setPreviousEnabled(false);
		verify(mockPageChangeListener).onPageChange(0L);
	}
}
