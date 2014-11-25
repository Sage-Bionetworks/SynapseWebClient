package org.sagebionetworks.web.unitclient.widget.table.v2.results;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.PartialRow;
import org.sagebionetworks.repo.model.table.PartialRowSet;
import org.sagebionetworks.repo.model.table.Row;
import org.sagebionetworks.repo.model.table.RowSet;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.EntityFactory;
import org.sagebionetworks.web.client.widget.table.v2.results.RowSetUtils;
import org.sagebionetworks.web.unitclient.widget.table.v2.TableModelTestUtils;

public class RowSetUtilsTest {
	
	Row rowOne;
	Row rowTwo;
	List<ColumnModel> schema;
	List<String> headers;
	
	@Before
	public void before(){
		schema = TableModelTestUtils.createColumsWithNames("one", "two");
		headers = TableModelTestUtils.getColumnModelIds(schema);
		rowOne = new Row();
		rowOne.setValues(Arrays.asList("1,1","1,2"));
		rowTwo = new Row();
		rowTwo.setValues(Arrays.asList("2,1","2,2"));
	}
	
	@Test
	public void testAddOnly(){
		RowSet original = new RowSet();
		original.setTableId("syn999");
		List<Row> updateRows = Arrays.asList(rowOne, rowTwo);
		RowSet update = new RowSet();
		update.setHeaders(headers);
		update.setRows(updateRows);
		PartialRowSet prs = RowSetUtils.buildDelta(original, update);
		assertNotNull(prs);
		assertEquals(original.getTableId(), prs.getTableId());
		List<PartialRow> delta = prs.getRows();
		assertNotNull(delta);
		assertEquals(2, delta.size());
		// one
		PartialRow pr = delta.get(0);
		assertEquals(null, pr.getRowId());
		assertNotNull(pr.getValues());
		assertEquals(headers.size(), pr.getValues().size());
		assertEquals(rowOne.getRowId(), pr.getRowId());
		assertEquals("1,1", pr.getValues().get(headers.get(0)));
		assertEquals("1,2", pr.getValues().get(headers.get(1)));
		// two
		pr = delta.get(1);
		assertEquals(null, pr.getRowId());
		assertNotNull(pr.getValues());
		assertEquals(headers.size(), pr.getValues().size());
		assertEquals(rowTwo.getRowId(), pr.getRowId());
		assertEquals("2,1", pr.getValues().get(headers.get(0)));
		assertEquals("2,2", pr.getValues().get(headers.get(1)));
	}
	
	@Test
	public void testUpdateNoChange() throws JSONObjectAdapterException {
		RowSet original = new RowSet();
		rowOne.setRowId(123L);
		rowTwo.setRowId(456L);
		original.setRows(Arrays.asList(rowOne, rowTwo));
		// Clone the data.
		List<Row> updates = TableModelTestUtils.cloneObject(original.getRows(), Row.class);
		RowSet update = new RowSet();
		update.setHeaders(headers);
		update.setRows(updates);
		PartialRowSet prs = RowSetUtils.buildDelta(original, update);
		List<PartialRow> delta = prs.getRows();
		assertNotNull(delta);
		assertEquals(0, delta.size());
	}
	
	@Test
	public void testUpdateWithChanges() throws JSONObjectAdapterException {
		RowSet original = new RowSet();
		rowOne.setRowId(123L);
		rowTwo.setRowId(456L);
		original.setRows(Arrays.asList(rowOne, rowTwo));
		// Clone the data.
		List<Row> updates = TableModelTestUtils.cloneObject(original.getRows(), Row.class);
		// Change some values
		updates.get(0).getValues().set(0, "new");
		updates.get(1).getValues().set(1, "newTwo");
		RowSet update = new RowSet();
		update.setHeaders(headers);
		update.setRows(updates);
		PartialRowSet prs = RowSetUtils.buildDelta(original, update);
		List<PartialRow> delta = prs.getRows();
		assertNotNull(delta);
		assertEquals(2, delta.size());
		// one
		PartialRow pr = delta.get(0);
		assertNotNull(pr.getValues());
		assertEquals(1, pr.getValues().size());
		assertEquals(rowOne.getRowId(), pr.getRowId());
		assertEquals("new", pr.getValues().get(headers.get(0)));
		// two
		pr = delta.get(1);
		assertNotNull(pr.getValues());
		assertEquals(1, pr.getValues().size());
		assertEquals(rowTwo.getRowId(), pr.getRowId());
		assertEquals("newTwo", pr.getValues().get(headers.get(1)));
	}
	
	@Test
	public void testDeleteOnly() throws JSONObjectAdapterException{
		RowSet original = new RowSet();
		rowOne.setRowId(123L);
		rowTwo.setRowId(456L);
		original.setRows(Arrays.asList(rowOne, rowTwo));
		// Clone the data.
		List<Row> updates = TableModelTestUtils.cloneObject(original.getRows(), Row.class);
		// remove the first row
		updates.remove(0);
		RowSet update = new RowSet();
		update.setHeaders(headers);
		update.setRows(updates);
		PartialRowSet prs = RowSetUtils.buildDelta(original, update);
		List<PartialRow> delta = prs.getRows();
		assertNotNull(delta);
		assertEquals(1, delta.size());
		// delete
		PartialRow pr = delta.get(0);
		assertEquals(rowOne.getRowId(), pr.getRowId());
		assertEquals(null, pr.getValues());
	}

	@Test
	public void testAddUpdateDelete() throws JSONObjectAdapterException{
		RowSet original = new RowSet();
		rowOne.setRowId(123L);
		rowTwo.setRowId(456L);
		original.setRows(Arrays.asList(rowOne, rowTwo));
		// Clone the data.
		List<Row> updates = TableModelTestUtils.cloneObject(original.getRows(), Row.class);
		// remove the first row
		updates.remove(0);
		RowSet update = new RowSet();
		update.setHeaders(headers);
		update.setRows(updates);
		// Update the second row.
		updates.get(0).getValues().set(0, "updated");
		// add a new row
		Row newRow = new Row();
		newRow.setRowId(null);
		newRow.setValues(Arrays.asList("a", "b"));
		updates.add(newRow);
		PartialRowSet prs = RowSetUtils.buildDelta(original, update);
		List<PartialRow> delta = prs.getRows();
		assertNotNull(delta);
		assertEquals(3, delta.size());
		// delete
		PartialRow pr = delta.get(0);
		assertEquals(rowOne.getRowId(), pr.getRowId());
		assertEquals(null, pr.getValues());
		// update
		pr = delta.get(1);
		assertEquals(rowTwo.getRowId(), pr.getRowId());
		assertEquals(1, pr.getValues().size());
		assertEquals("updated", pr.getValues().get(headers.get(0)));
		// added
		pr = delta.get(2);
		assertEquals(null, pr.getRowId());
		assertEquals(2, pr.getValues().size());
		assertEquals("a", pr.getValues().get(headers.get(0)));
		assertEquals("b", pr.getValues().get(headers.get(1)));
	}
	
	/**
	 * If the original value contains empty strings and the update also contains empty strings
	 * there should be no update.
	 * 
	 * @throws JSONObjectAdapterException
	 */
	@Test
	public void testOriginalEmptyStringAndChangeEmptyString() throws JSONObjectAdapterException{
		RowSet original = new RowSet();
		rowOne.setRowId(123L);
		rowOne.setValues(Arrays.asList("", ""));
		original.setRows(Arrays.asList(rowOne));
		// Clone the data.
		List<Row> updates = TableModelTestUtils.cloneObject(original.getRows(), Row.class);
		RowSet update = new RowSet();
		update.setHeaders(headers);
		update.setRows(updates);
		// set the value of the one row to also be an empty string.
		updates.get(0).setValues(Arrays.asList("", ""));
		PartialRowSet prs = RowSetUtils.buildDelta(original, update);
		assertTrue(prs.getRows().isEmpty());
	}
	
	/**
	 * If the original value contains nulls and the update also contains empty strings
	 * there should be no update.
	 * 
	 * @throws JSONObjectAdapterException
	 */
	@Test
	public void testOriginalNullAndChangeEmptyString() throws JSONObjectAdapterException{
		RowSet original = new RowSet();
		rowOne.setRowId(123L);
		rowOne.setValues(Arrays.asList((String)null,(String)null));
		original.setRows(Arrays.asList(rowOne));
		// Clone the data.
		List<Row> updates = TableModelTestUtils.cloneObject(original.getRows(), Row.class);
		RowSet update = new RowSet();
		update.setHeaders(headers);
		update.setRows(updates);
		updates.get(0).setValues(Arrays.asList("", ""));
		PartialRowSet prs = RowSetUtils.buildDelta(original, update);
		assertTrue(prs.getRows().isEmpty());
	}
	
	/**
	 * If the original value contains nulls and the update also contains empty strings
	 * there should be no update.
	 * 
	 * @throws JSONObjectAdapterException
	 */
	@Test
	public void testOriginalEmptyStringAndChangeNulls() throws JSONObjectAdapterException{
		RowSet original = new RowSet();
		rowOne.setRowId(123L);
		rowOne.setValues(Arrays.asList("",""));
		original.setRows(Arrays.asList(rowOne));
		// Clone the data.
		List<Row> updates = TableModelTestUtils.cloneObject(original.getRows(), Row.class);
		RowSet update = new RowSet();
		update.setHeaders(headers);
		update.setRows(updates);
		updates.get(0).setValues(Arrays.asList((String)null,(String)null));
		PartialRowSet prs = RowSetUtils.buildDelta(original, update);
		assertTrue(prs.getRows().isEmpty());
	}
	
	/**
	 * Adding a row with null or empty values should both be treated as null values.
	 * @throws JSONObjectAdapterException
	 */
	@Test
	public void testAddRowWithNullAndEmptyStrings() throws JSONObjectAdapterException{
		RowSet original = new RowSet();
		original.setTableId("syn999");
		Row emptyRow = new Row();
		emptyRow.setValues(Arrays.asList("",null));
		List<Row> updateRows = Arrays.asList(emptyRow);
		RowSet update = new RowSet();
		update.setHeaders(headers);
		update.setRows(updateRows);
		PartialRowSet prs = RowSetUtils.buildDelta(original, update);
		assertNotNull(prs);
		assertNotNull(prs.getRows());
		PartialRow pr = prs.getRows().get(0);
		assertEquals(null, pr.getValues().get(headers.get(0)));
		assertEquals(null, pr.getValues().get(headers.get(1)));
	}
	

	

	/**
	 * This is currently throwing an exception.
	 * @throws JSONObjectAdapterException
	 */
	@Test
	public void testPartialRowWithNulls() throws JSONObjectAdapterException{
		PartialRow pr = new PartialRow();
		pr.setRowId(123L);
		Map<String, String> values = new HashMap<String, String>();
		values.put("a", null);
		values.put("b", "");
		pr.setValues(values);
		String json = EntityFactory.createJSONStringForEntity(pr);
		PartialRow clone = EntityFactory.createEntityFromJSONString(json, PartialRow.class);
		assertNotNull(clone);
	}
}
