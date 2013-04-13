package org.sagebionetworks.web.unitserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.sagebionetworks.web.server.servlet.QueryStringUtils;
import org.sagebionetworks.web.shared.QueryConstants.ObjectType;
import org.sagebionetworks.web.shared.QueryConstants.WhereOperator;
import org.sagebionetworks.web.shared.SearchParameters;
import org.sagebionetworks.web.shared.WhereCondition;

public class QueryStringUtilsTest {
	
	private static String ROOT_URL = "http://localhost:9998";
	
	@Test
	public void testRoundTrip() throws URISyntaxException{
		// Create a simple query
		SearchParameters params = new SearchParameters();
		params.setFromType(ObjectType.dataset.name());
		// Now create the query string for this object
		URI uri = QueryStringUtils.writeQueryUri(ROOT_URL, params);
		assertNotNull(uri);
		System.out.println(uri);
		// Now make a copy from the string
		SearchParameters copy = QueryStringUtils.parseQueryString(uri.toString());
		assertNotNull(copy);
		// It should match the original
		assertEquals(params, copy);
	}
	
	@Test
	public void testRoundTripPaging() throws URISyntaxException{
		// Create a simple query
		SearchParameters params = new SearchParameters();
		params.setLimit(20);
		params.setOffset(50);
		params.setFromType(ObjectType.dataset.name());
		// Now create the query string for this object
		URI uri = QueryStringUtils.writeQueryUri(ROOT_URL, params);
		assertNotNull(uri);
		System.out.println(uri);
		// Now make a copy from the string
		SearchParameters copy = QueryStringUtils.parseQueryString(uri.toString());
		assertNotNull(copy);
		// It should match the original
		assertEquals(params, copy);
	}
	
	@Test
	public void testRoundTripPagingAndSorting() throws URISyntaxException{
		// Create a simple query
		SearchParameters params = new SearchParameters();
		params.setLimit(100);
		params.setOffset(31);
		params.setFromType(ObjectType.dataset.name());
		params.setAscending(false);
		params.setSort("sortKey");
		// Now create the query string for this object
		URI uri = QueryStringUtils.writeQueryUri(ROOT_URL, params);
		assertNotNull(uri);
		System.out.println(uri);
		// Now make a copy from the string
		SearchParameters copy = QueryStringUtils.parseQueryString(uri.toString());
		assertNotNull(copy);
		// It should match the original
		assertEquals(params, copy);
	}
	
	@Test
	public void testRoundTripZeroOffest() throws URISyntaxException{
		// Create a simple query
		SearchParameters params = new SearchParameters();
		params.setLimit(100);
		params.setOffset(0);
		params.setFromType(ObjectType.dataset.name());
		params.setAscending(true);
		params.setSort("sortKey");
		// Now create the query string for this object
		URI uri = QueryStringUtils.writeQueryUri(ROOT_URL, params);
		assertNotNull(uri);
		System.out.println(uri);
		// Now make a copy from the string
		SearchParameters copy = QueryStringUtils.parseQueryString(uri.toString());
		assertNotNull(copy);
		// The offset should have been changed to 1
		assertEquals(1, copy.getOffset());
	}
	
	@Test
	public void testRoundTripWhere() throws URISyntaxException{
		// Create a simple query
		SearchParameters params = new SearchParameters();
		params.setLimit(100);
		params.setOffset(0);
		params.setFromType(ObjectType.dataset.name());
		params.setAscending(true);
		params.setSort("sortKey");
		WhereCondition where = new WhereCondition("someId", WhereOperator.EQUALS, "123");
		List<WhereCondition> list = new ArrayList<WhereCondition>();
		list.add(where);
		params.setWhere(list);
		// Now create the query string for this object
		URI uri = QueryStringUtils.writeQueryUri(ROOT_URL, params);
		assertNotNull(uri);
		System.out.println(uri);
		// Now make a copy from the string
		SearchParameters copy = QueryStringUtils.parseQueryString(uri.toString());
		assertNotNull(copy);
		assertNotNull(copy.getWhere());
		assertEquals(1, copy.getWhere().size());
		// The offset should have been changed to 1
		assertEquals(where, copy.getWhere().get(0));
	}
	
	@Test
	public void testRoundTripWhereMultiple() throws URISyntaxException{
		// Create a simple query
		SearchParameters params = new SearchParameters();
		params.setLimit(100);
		params.setOffset(0);
		params.setFromType(ObjectType.dataset.name());
		params.setAscending(true);
		params.setSort("sortKey");
		WhereCondition whereOne = new WhereCondition("someId", WhereOperator.EQUALS, "123");
		WhereCondition wheretwo = new WhereCondition("someOtherId", WhereOperator.GREATER_THAN, "123");
		List<WhereCondition> list = new ArrayList<WhereCondition>();
		list.add(whereOne);
		list.add(wheretwo);
		params.setWhere(list);
		// Now create the query string for this object
		URI uri = QueryStringUtils.writeQueryUri(ROOT_URL, params);
		assertNotNull(uri);
		System.out.println(uri);
		// Now make a copy from the string
		SearchParameters copy = QueryStringUtils.parseQueryString(uri.toString());
		assertNotNull(copy);
		assertNotNull(copy.getWhere());
		assertEquals(2, copy.getWhere().size());
		// Check both
		assertEquals(whereOne, copy.getWhere().get(0));
		assertEquals(wheretwo, copy.getWhere().get(1));
	}


	@Test
	public void testNullColumnsTrip() throws URISyntaxException{
		// Create a simple query
		SearchParameters params = new SearchParameters();
		params.setFromType(ObjectType.dataset.name());
		params.setSelectColumns(null);
		// Now create the query string for this object
		URI uri = QueryStringUtils.writeQueryUri(ROOT_URL, params);
		assertNotNull(uri);
		System.out.println(uri);
		assertTrue(uri.toString().contains("select+*+"));		
	}
	
	@Test
	public void testEmptyColumnsTrip() throws URISyntaxException{
		// Create a simple query
		SearchParameters params = new SearchParameters();
		params.setFromType(ObjectType.dataset.name());
		params.setSelectColumns(new ArrayList<String>());
		// Now create the query string for this object
		URI uri = QueryStringUtils.writeQueryUri(ROOT_URL, params);
		assertNotNull(uri);
		System.out.println(uri);
		assertTrue(uri.toString().contains("select+*+"));
	}
	
}
