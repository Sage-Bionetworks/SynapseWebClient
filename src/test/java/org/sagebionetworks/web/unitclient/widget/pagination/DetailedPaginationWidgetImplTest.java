package org.sagebionetworks.web.unitclient.widget.pagination;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.sagebionetworks.web.client.widget.pagination.DetailedPaginationWidgetImpl.NEXT;
import static org.sagebionetworks.web.client.widget.pagination.DetailedPaginationWidgetImpl.PREVIOUS;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.web.client.widget.pagination.DetailedPaginationView;
import org.sagebionetworks.web.client.widget.pagination.DetailedPaginationWidgetImpl;
import org.sagebionetworks.web.client.widget.pagination.PageChangeListener;

public class DetailedPaginationWidgetImplTest {

	DetailedPaginationView mockView;
	DetailedPaginationWidgetImpl widget;
	PageChangeListener mockListener;
	Long limit;
	Long offset;
	Long count;
	int maxButtons = 4;

	@Before
	public void before() {
		mockView = Mockito.mock(DetailedPaginationView.class);
		mockListener = Mockito.mock(PageChangeListener.class);
		widget = new DetailedPaginationWidgetImpl(mockView);
		widget.setMaxPageButtons(maxButtons);
	}

	@Test
	public void testConfigureNull() {
		limit = null;
		offset = null;
		count = null;
		// call under test
		widget.configure(limit, offset, count, mockListener);
		verify(mockView).setPagerVisible(false);
		verify(mockView, never()).addButton(anyLong(), anyString(),
				anyBoolean());
		verify(mockView).setMessage("");
	}

	@Test
	public void testConfigureLessThanOne() {
		limit = -1L;
		offset = -1L;
		count = -1L;
		// call under test
		widget.configure(limit, offset, count, mockListener);
		verify(mockView).setPagerVisible(false);
		verify(mockView, never()).addButton(anyLong(), anyString(),
				anyBoolean());
		verify(mockView).setMessage("");
	}

	@Test
	public void testConfigureOnePage() {
		limit = 10L;
		offset = 0L;
		count = 1L;
		// call under test
		widget.configure(limit, offset, count, mockListener);
		verify(mockView).setPagerVisible(false);
		verify(mockView).setMessage("1 - 1");
	}

	@Test
	public void testConfigureTwoPagesOnPageOne() {
		int numberOfPage = 2;
		limit = 10L;
		offset = 0L;
		count = limit * numberOfPage;
		// call under test
		widget.configure(limit, offset, count, mockListener);
		verify(mockView).setPagerVisible(true);
		verify(mockView).addButton(0L, "1", true);
		verify(mockView).addButton(limit, "2", false);
		verify(mockView).addButton(limit, NEXT, false);
		verify(mockView).setMessage("1 - 10");
	}

	@Test
	public void testConfigureTwoPagesOnPageTwo() {
		int numberOfPage = 2;
		limit = 10L;
		offset = limit;
		count = limit * numberOfPage;
		// call under test
		widget.configure(limit, offset, count, mockListener);
		verify(mockView).addButton(0L, PREVIOUS, false);
		verify(mockView).addButton(0L, "1", false);
		verify(mockView).addButton(limit, "2", true);
		verify(mockView).setMessage("11 - 20");
	}

	@Test
	public void testConfigureThreePagesOnPageTwo() {
		int numberOfPage = 3;
		limit = 10L;
		offset = limit;
		count = limit * numberOfPage;
		// call under test
		widget.configure(limit, offset, count, mockListener);
		verify(mockView).addButton(0L, PREVIOUS, false);
		verify(mockView).addButton(0L, "1", false);
		verify(mockView).addButton(limit, "2", true);
		verify(mockView).addButton(limit * 2, "3", false);
		verify(mockView).addButton(limit * 2, NEXT, false);
		verify(mockView).setMessage("11 - 20");
	}
	
	@Test
	public void testConfigureManyPagesOnSecond() {
		int numberOfPage = 100;
		limit = 10L;
		offset = limit;
		count = limit * numberOfPage;
		// call under test
		widget.configure(limit, offset, count, mockListener);
		verify(mockView).addButton(0L, PREVIOUS, false);
		verify(mockView).addButton(0L, "1", false);
		verify(mockView).addButton(limit, "2", true);
		verify(mockView).addButton(limit * 2, "3", false);
		verify(mockView).addButton(limit * 2, NEXT, false);
		verify(mockView).setMessage("11 - 20");
	}
	
	@Test
	public void testConfigureManyPagesOnFirst() {
		int numberOfPage = 100;
		limit = 10L;
		offset = 0L;
		count = limit * numberOfPage;
		// call under test
		widget.configure(limit, offset, count, mockListener);
		verify(mockView).addButton(0L, "1", true);
		verify(mockView).addButton(limit, "2", false);
		verify(mockView).addButton(limit * 2, "3", false);
		verify(mockView).addButton(limit, NEXT, false);
		verify(mockView).setMessage("1 - 10");
	}
	
	@Test
	public void testConfigureManyPagesOnLast() {
		int numberOfPage = 10;
		limit = 25L;
		offset = limit*9;
		count = limit * numberOfPage;
		// call under test
		widget.configure(limit, offset, count, mockListener);
		verify(mockView).addButton(limit*8, PREVIOUS, false);
		verify(mockView).addButton(limit*6, "7", false);
		verify(mockView).addButton(limit*7, "8", false);
		verify(mockView).addButton(limit*8, "9", false);
		verify(mockView).addButton(limit*9, "10", true);
		verify(mockView).setMessage("226 - 250");
	}
	
	@Test
	public void testConfigureManyPagesOnNextToLast() {
		int numberOfPage = 10;
		limit = 25L;
		offset = limit*8;
		count = limit * numberOfPage;
		// call under test
		widget.configure(limit, offset, count, mockListener);
		verify(mockView).addButton(limit*7, PREVIOUS, false);
		verify(mockView).addButton(limit*6, "7", false);
		verify(mockView).addButton(limit*7, "8", false);
		verify(mockView).addButton(limit*8, "9", true);
		verify(mockView).addButton(limit*9, "10", false);
		verify(mockView).addButton(limit*9, NEXT, false);
		verify(mockView).setMessage("201 - 225");
	}
	
	@Test
	public void testConfigureManyPagesOnPageOne() {
		int numberOfPage = maxButtons;
		limit = 2L;
		offset = 0L;
		count = limit * numberOfPage;
		// call under test
		widget.configure(limit, offset, count, mockListener);
		int minPage = 1;
		int maxPage = numberOfPage;
		for(int i=minPage; i<maxPage+1; i++){
			boolean active = i==1;
			verify(mockView).addButton(limit*(i-1), ""+i, active);
		}
	}
	
	/**
	 * If the max number of page buttons is four then the following should be true: 
	 * The page buttons start off a 1,2,3,n...
	 * 
	 */
	@Test
	public void testConfigurePageButtonShift() {
		// The page number shift 
		int numberOfPage = maxButtons;
		limit = 2L;
		offset = 0L;
		count = limit * numberOfPage;
		// call under test
		widget.configure(limit, offset, count, mockListener);
		int minPage = 1;
		int maxPage = numberOfPage;
		for(int i=minPage; i<maxPage+1; i++){
			boolean active = i==1;
			verify(mockView).addButton(limit*(i-1), ""+i, active);
		}
	}
	
}
