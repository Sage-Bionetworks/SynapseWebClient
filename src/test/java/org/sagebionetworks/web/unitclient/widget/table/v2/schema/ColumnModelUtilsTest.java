package org.sagebionetworks.web.unitclient.widget.table.v2.schema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.repo.model.table.FacetType;
import org.sagebionetworks.repo.model.table.SelectColumn;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRow;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelUtils;
import org.sagebionetworks.web.unitclient.widget.table.v2.TableModelTestUtils;

public class ColumnModelUtilsTest {

	@Test
	public void testTreatEmptyAsNull() {
		assertEquals(null, ColumnModelUtils.treatEmptyAsNull(""));
		assertEquals(null, ColumnModelUtils.treatEmptyAsNull(" \n \t"));
		assertEquals("one", ColumnModelUtils.treatEmptyAsNull("one"));
	}

	@Test
	public void testColumnModelTableRowRoundTrip() {
		// Start with a ColumnModel
		ColumnModel model = new ColumnModel();
		model.setName("James");
		model.setId("007");
		model.setColumnType(ColumnType.STRING);
		model.setDefaultValue("foo and bar");
		model.setMaximumSize(3L);
		model.setEnumValues(Arrays.asList("one", "two", "three"));
		model.setFacetType(FacetType.enumeration);
		// stub the view
		ColumnModelTableRowStub row = new ColumnModelTableRowStub();
		// apply to the row.
		ColumnModelUtils.applyColumnModelToRow(model, row);
		ColumnModel clone = ColumnModelUtils.extractColumnModel(row);
		assertEquals(model, clone);
	}


	@Test
	public void testColumnModelTableRowRoundTripWithNulls() {
		// Start with a ColumnModel
		ColumnModel model = new ColumnModel();
		model.setName(null);
		model.setId(null);
		model.setColumnType(ColumnType.BOOLEAN);
		model.setDefaultValue(null);
		model.setMaximumSize(null);
		model.setFacetType(null);
		// stub the view
		ColumnModelTableRowStub row = new ColumnModelTableRowStub();
		// apply to the row.
		ColumnModelUtils.applyColumnModelToRow(model, row);
		ColumnModel clone = ColumnModelUtils.extractColumnModel(row);
		assertEquals(model, clone);
	}

	@Test
	public void testColumnModelTableRowRoundTripAllTypes() {
		// Start with a ColumnModel
		List<ColumnModel> models = new LinkedList<ColumnModel>();
		List<ColumnModelTableRow> rows = new LinkedList<ColumnModelTableRow>();
		for (ColumnType type : ColumnType.values()) {
			ColumnModel model = new ColumnModel();
			model.setName("name");
			model.setId("id");
			model.setColumnType(type);
			model.setFacetType(FacetType.range);
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


	@Test
	public void testCSVRoundTripEmpty() {
		List<String> list = new LinkedList<String>();
		String csv = ColumnModelUtils.listToCSV(list);
		assertEquals("", csv);
		List<String> clone = ColumnModelUtils.csvToList(csv);
		assertEquals(list, clone);
	}

	@Test
	public void testCSVRoundTripOne() {
		List<String> list = Arrays.asList("one");
		String csv = ColumnModelUtils.listToCSV(list);
		assertEquals("one", csv);
		List<String> clone = ColumnModelUtils.csvToList(csv);
		assertEquals(list, clone);
	}

	@Test
	public void testCSVRoundTripMany() {
		List<String> list = Arrays.asList("one", "two");
		String csv = ColumnModelUtils.listToCSV(list);
		assertEquals("one, two", csv);
		List<String> clone = ColumnModelUtils.csvToList(csv);
		assertEquals(list, clone);
	}

	@Test
	public void testbuildMapColumnIdtoModel() {
		ColumnModel one = new ColumnModel();
		one.setId("one");
		ColumnModel two = new ColumnModel();
		two.setId("two");
		List<ColumnModel> models = Arrays.asList(one, two);
		Map<String, ColumnModel> map = ColumnModelUtils.buildMapColumnIdtoModel(models);
		assertNotNull(map);
		assertEquals(2, map.size());
		assertEquals(one, map.get(one.getId()));
		assertEquals(two, map.get(two.getId()));
	}

	@Test
	public void testBuildTypesForQueryResults() {
		ColumnModel one = new ColumnModel();
		one.setId("1");
		one.setName("one");
		one.setColumnType(ColumnType.DOUBLE);
		ColumnModel two = new ColumnModel();
		two.setId("2");
		two.setName("two");
		two.setColumnType(ColumnType.STRING);
		List<ColumnModel> models = Arrays.asList(one, two);
		// headers can includes the names of aggregation functions.
		List<SelectColumn> headers = TableModelTestUtils.buildSelectColumns(models);
		SelectColumn derived = new SelectColumn();
		derived.setColumnType(ColumnType.INTEGER);
		derived.setName("sum(one)");
		headers.add(0, derived);
		List<ColumnModel> results = ColumnModelUtils.buildTypesForQueryResults(headers, models);
		assertNotNull(results);
		assertEquals(3, results.size());
		// the first column should have the name of the aggregate function.
		ColumnModel cm = results.get(0);
		assertEquals("sum(one)", cm.getName());
		assertEquals(ColumnType.INTEGER, cm.getColumnType());
		assertEquals(null, cm.getId());
		// the second should match two
		cm = results.get(2);
		assertEquals(two, cm);
		// The last should match first
		cm = results.get(1);
		assertEquals(one, cm);
	}


	@Test
	public void testBuildTypesForQueryResultsNullHeaders() {
		ColumnModel one = new ColumnModel();
		one.setId("1");
		one.setName("one");
		one.setColumnType(ColumnType.DOUBLE);
		ColumnModel two = new ColumnModel();
		two.setId("2");
		two.setName("two");
		two.setColumnType(ColumnType.STRING);
		List<ColumnModel> models = Arrays.asList(one, two);
		List<ColumnModel> results = ColumnModelUtils.buildTypesForQueryResults(null, models);
		// For this case the results should match the models
		assertEquals(models, results);
	}
}
