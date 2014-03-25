package org.sagebionetworks.web.unitclient.widget.table;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.sagebionetworks.repo.model.table.Row;
import org.sagebionetworks.web.client.widget.table.TableModel;
import org.sagebionetworks.web.client.widget.table.TableUtils;

public class TableUtilsTest {
	
	@Test
	public void convertRowToModelTest() {
		List<String> headers = Arrays.asList(new String[] { "1", "2", "3" });
		Long rowId = 1234L;
		Row row = new Row();
		row.setRowId(rowId);
		row.setValues(Arrays.asList(new String []{ "v1", "v2", "v3" }));
		TableModel model = TableUtils.convertRowToModel(headers, row);
		assertEquals(rowId.toString(), model.getId());
		assertEquals("v1", model.get("1"));
		assertEquals("v2", model.get("2"));
		assertEquals("v3", model.get("3"));
	}
	
	
}
