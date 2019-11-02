package org.sagebionetworks.web.unitclient.widget.table.v2.results;

import static org.junit.Assert.assertEquals;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.repo.model.table.QueryResult;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.repo.model.table.Row;
import org.sagebionetworks.repo.model.table.RowSet;
import org.sagebionetworks.repo.model.table.SelectColumn;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryBundleUtils;

public class QueryBundleUtilsTest {

	String tableId;
	Row row;
	RowSet rowSet;
	QueryResult results;
	SelectColumn select;
	QueryResultBundle bundle;

	@Before
	public void before() {
		row = new Row();
		row.setRowId(123L);
		select = new SelectColumn();
		select.setId("123");
		rowSet = new RowSet();
		tableId = "syn123";
		rowSet.setTableId("syn123");
		rowSet.setRows(Arrays.asList(row));
		rowSet.setHeaders(Arrays.asList(select));
		results = new QueryResult();
		results.setQueryResults(rowSet);
		bundle = new QueryResultBundle();
		bundle.setMaxRowsPerPage(123L);
		bundle.setQueryCount(88L);
		bundle.setQueryResult(results);
	}

	@Test
	public void testGetRowSet() {
		assertEquals(rowSet, QueryBundleUtils.getRowSet(bundle));
		bundle.setQueryResult(null);
		assertEquals(null, QueryBundleUtils.getRowSet(bundle));
		assertEquals(null, QueryBundleUtils.getRowSet(null));
	}

	@Test
	public void testGetTableId() {
		assertEquals(tableId, QueryBundleUtils.getTableId(bundle));
		bundle.getQueryResult().getQueryResults().setTableId(null);
		assertEquals(null, QueryBundleUtils.getTableId(bundle));
		bundle.setQueryResult(null);
		assertEquals(null, QueryBundleUtils.getTableId(bundle));
		assertEquals(null, QueryBundleUtils.getTableId((QueryResultBundle) null));
	}

	@Test
	public void testGetTableIdFromQuerySql() {
		assertEquals("syn456", QueryBundleUtils.getTableIdFromSql("SELECT syn123, BAR FROM SYN456 where id='syn789'"));
		assertEquals(null, QueryBundleUtils.getTableIdFromSql("SELECT syn123, foo FROM 456 where id='syn789'"));
	}

	@Test
	public void testGetTableIdQuery() {
		Query query = new Query();
		query.setSql("select * from syn999");
		assertEquals("syn999", QueryBundleUtils.getTableId(query));
		assertEquals(null, QueryBundleUtils.getTableId((Query) null));
	}


	@Test
	public void testGetSelect() {
		assertEquals(rowSet.getHeaders(), QueryBundleUtils.getSelectFromBundle(bundle));
		bundle.getQueryResult().getQueryResults().setHeaders(null);
		assertEquals(null, QueryBundleUtils.getSelectFromBundle(bundle));
		bundle.setQueryResult(null);
		assertEquals(null, QueryBundleUtils.getSelectFromBundle(bundle));
		assertEquals(null, QueryBundleUtils.getSelectFromBundle(null));
	}
}
