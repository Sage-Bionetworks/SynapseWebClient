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
	String version = "456";

	
	@Test
	public void convertRowToModelTest() {
		Row row = new Row();
		row.setRowId(rowId);
		row.setVersionNumber(Long.parseLong(version));
		row.setValues(Arrays.asList(new String []{ "v1", "v2", null }));
		TableModel model = TableUtils.convertRowToModel(headers, row);
		assertEquals(rowId.toString(), model.getId());
		assertEquals("v1", model.get("1"));
		assertEquals("v2", model.get("2"));
		assertEquals(null, model.get("3"));
		assertEquals(version, model.getVersionNumber());
	}
	
	@Test
	public void convertModelToRowTest() {
		TableModel model = new TableModel(rowId.toString(), version.toString());
		model.put("1", "v1");
		model.put("2", "v2");
		model.put("3", null);
		Row row = TableUtils.convertModelToRow(headers, model);
		assertEquals(rowId, row.getRowId());
		assertEquals("v1", row.getValues().get(0));
		assertEquals("v2", row.getValues().get(1));
		assertEquals(null, row.getValues().get(2));		
		assertEquals(version, row.getVersionNumber().toString());
	}

	@Test
	public void testEscapeColumnName() {
		assertEquals("\"name with spaces\"", TableUtils.escapeColumnName("name with spaces"));
		assertEquals("\"name with \"\" quotes\"", TableUtils.escapeColumnName("name with \" quotes"));
	}
}
