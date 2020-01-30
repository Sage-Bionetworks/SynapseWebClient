package org.sagebionetworks.web.unitclient.widget.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.List;
import org.junit.Test;
import org.sagebionetworks.web.client.widget.search.PaginationEntry;
import org.sagebionetworks.web.client.widget.search.PaginationUtil;

public class PaginationUtilTest {

	@Test
	public void testGetPagination() {
		List<PaginationEntry> list = null;

		// test invalid inputs
		try {
			list = PaginationUtil.getPagination(-1, 5, 10, 10);
			assertNotNull(null);
		} catch (IllegalArgumentException ex) {
			assertNull(list);
		}

		try {
			list = PaginationUtil.getPagination(5, -1, 10, 10);
			assertNotNull(null);
		} catch (IllegalArgumentException ex) {
			assertNull(list);
		}

		try {
			list = PaginationUtil.getPagination(5, 5, 0, 10);
			assertNotNull(null);
		} catch (IllegalArgumentException ex) {
			assertNull(list);
		}

		try {
			list = PaginationUtil.getPagination(5, 5, 10, 0);
			assertNotNull(null);
		} catch (IllegalArgumentException ex) {
			assertNull(list);
		}

		// test empty search results
		list = PaginationUtil.getPagination(0, 0, 10, 10);
		assertEquals(0, list.size());

		// test two pages. less than total pages
		list = PaginationUtil.getPagination(11, 0, 10, 10);
		assertEquals(2, list.size());
		assertTrue(new PaginationEntry("1", 0, true).equals(list.get(0)));
		assertTrue(new PaginationEntry("2", 10, false).equals(list.get(1)));

		// test two pages, second page selected
		list = PaginationUtil.getPagination(11, 10, 10, 10);
		assertEquals(2, list.size());
		assertTrue(new PaginationEntry("1", 0, false).equals(list.get(0)));
		assertTrue(new PaginationEntry("2", 10, true).equals(list.get(1)));

		// test middle selection
		list = PaginationUtil.getPagination(100, 50, 10, 10);
		assertEquals(10, list.size());
		assertTrue(new PaginationEntry("1", 0, false).equals(list.get(0)));
		assertTrue(new PaginationEntry("6", 50, true).equals(list.get(5)));

		// test next button
		list = PaginationUtil.getPagination(120, 50, 10, 10);
		assertEquals(11, list.size());
		assertTrue(new PaginationEntry("1", 0, false).equals(list.get(0)));
		assertTrue(new PaginationEntry("6", 50, true).equals(list.get(5)));
		assertTrue(new PaginationEntry("Next", 60, false).equals(list.get(10)));

		// test prev button
		list = PaginationUtil.getPagination(110, 60, 10, 10);
		assertEquals(11, list.size());
		assertTrue(new PaginationEntry("Prev", 50, false).equals(list.get(0)));
		assertTrue(new PaginationEntry("2", 10, false).equals(list.get(1)));
		assertTrue(new PaginationEntry("7", 60, true).equals(list.get(6)));
		assertTrue(new PaginationEntry("11", 100, false).equals(list.get(10)));

		// text next and previous and middle point with both
		list = PaginationUtil.getPagination(120, 60, 10, 10);
		assertEquals(12, list.size());
		assertTrue(new PaginationEntry("Prev", 50, false).equals(list.get(0)));
		assertTrue(new PaginationEntry("2", 10, false).equals(list.get(1)));
		assertTrue(new PaginationEntry("7", 60, true).equals(list.get(6)));
		assertTrue(new PaginationEntry("11", 100, false).equals(list.get(10)));
		assertTrue(new PaginationEntry("Next", 70, false).equals(list.get(11)));


		// test select last
		list = PaginationUtil.getPagination(150, 140, 10, 10);
		assertEquals(7, list.size());
		assertTrue(new PaginationEntry("Prev", 130, false).equals(list.get(0)));
		assertTrue(new PaginationEntry("10", 90, false).equals(list.get(1)));
		assertTrue(new PaginationEntry("15", 140, true).equals(list.get(6)));

	}
}


