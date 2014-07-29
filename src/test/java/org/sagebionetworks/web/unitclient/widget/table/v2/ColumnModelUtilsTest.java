package org.sagebionetworks.web.unitclient.widget.table.v2;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.web.client.widget.table.v2.ColumnModelTableRow;
import org.sagebionetworks.web.client.widget.table.v2.ColumnModelUtils;

public class ColumnModelUtilsTest {
	
	@Test
	public void testTreatEmptyAsNull(){
		assertEquals(null, ColumnModelUtils.treatEmptyAsNull(""));
		assertEquals(null, ColumnModelUtils.treatEmptyAsNull(" \n \t"));
		assertEquals("one", ColumnModelUtils.treatEmptyAsNull("one"));
	}
	
	@Test
	public void testColumnModelTableRowRoundTrip(){
		// Start with a ColumnModel
		ColumnModel model = new ColumnModel();
		model.setName("James");
		model.setId("007");
		model.setColumnType(ColumnType.STRING);
		model.setDefaultValue("foo and bar");
		model.setMaximumSize(3L);
		// stub the view
		ColumnModelTableRowStub row = new ColumnModelTableRowStub();
		// apply to the row.
		ColumnModelUtils.applyColumnModelToRow(model, row);
		ColumnModel clone = ColumnModelUtils.extractColumnModel(row);
		assertEquals(model, clone);
	}

	
	@Test
	public void testColumnModelTableRowRoundTripWithNulls(){
		// Start with a ColumnModel
		ColumnModel model = new ColumnModel();
		model.setName(null);
		model.setId(null);
		model.setColumnType(ColumnType.BOOLEAN);
		model.setDefaultValue(null);
		model.setMaximumSize(null);
		// stub the view
		ColumnModelTableRowStub row = new ColumnModelTableRowStub();
		// apply to the row.
		ColumnModelUtils.applyColumnModelToRow(model, row);
		ColumnModel clone = ColumnModelUtils.extractColumnModel(row);
		assertEquals(model, clone);
	}
	
	@Test
	public void testColumnModelTableRowRoundTripAllTypes(){
		// Start with a ColumnModel
		List<ColumnModel> models = new LinkedList<ColumnModel>();
		List<ColumnModelTableRow> rows = new LinkedList<ColumnModelTableRow>();
		for(ColumnType type: ColumnType.values()){
			ColumnModel model = new ColumnModel();
			model.setName("name");
			model.setId("id");
			model.setColumnType(type);
			models.add(model);
			// stub the view
			ColumnModelTableRowStub row = new ColumnModelTableRowStub();
			rows.add(row);
			// apply to the row.
			ColumnModelUtils.applyColumnModelToRow(model, row);
		}
		// Extract as list
		List<ColumnModel> clones = ColumnModelUtils.extractColumnModels(rows);
		assertEquals(models, clones);
	}

}
