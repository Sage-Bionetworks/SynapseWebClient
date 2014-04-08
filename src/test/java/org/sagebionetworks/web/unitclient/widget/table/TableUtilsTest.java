package org.sagebionetworks.web.unitclient.widget.table;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.sagebionetworks.repo.model.table.Row;
import org.sagebionetworks.web.client.widget.table.TableModel;
import org.sagebionetworks.web.client.widget.table.TableUtils;

public class TableUtilsTest {
	
	List<String> headers = Arrays.asList(new String[] { "1", "2", "3" });
	Long rowId = 1234L;

	
	@Test
	public void convertRowToModelTest() {
		Row row = new Row();
		row.setRowId(rowId);
		row.setValues(Arrays.asList(new String []{ "v1", "v2", null }));
		TableModel model = TableUtils.convertRowToModel(headers, row);
		assertEquals(rowId.toString(), model.getId());
		assertEquals("v1", model.get("1"));
		assertEquals("v2", model.get("2"));
		assertEquals(null, model.get("3"));
	}
	
	@Test
	public void convertModelToRowTest() {
		TableModel model = new TableModel(rowId.toString());
		model.put("1", "v1");
		model.put("2", "v2");
		model.put("3", null);
		Row row = TableUtils.convertModelToRow(headers, model);
		assertEquals(rowId, row.getRowId());
		assertEquals("v1", row.getValues().get(0));
		assertEquals("v2", row.getValues().get(1));
		assertEquals(null, row.getValues().get(2));		
	}
	
}
