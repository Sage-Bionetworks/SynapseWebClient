package org.sagebionetworks.web.unitclient.widget.table.entity;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.repo.model.table.TableBundle;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.widget.table.entity.TableModelUtils;

public class TableModelUtilsTest {
	
	private AdapterFactory adapterFactory;
	private TableModelUtils utils;
	
	@Before
	public void before(){
		// use the non-gwt adaptor for this test.
		adapterFactory = new AdapterFactoryImpl();
		utils = new TableModelUtils(adapterFactory);
	}
	
	@Test
	public void testColumnModelRoundTrip() throws JSONObjectAdapterException{
		ColumnModel cm = new ColumnModel();
		cm.setColumnType(ColumnType.BOOLEAN);
		cm.setId("123");
		cm.setName("One");
		// to json
		String json = utils.toJSON(cm);
		ColumnModel clone = utils.columnModelFromJSON(json);
		assertEquals(cm, clone);
	}
	
	@Test
	public void testListRoundTrip() throws JSONObjectAdapterException{
		ColumnModel one = new ColumnModel();
		one.setColumnType(ColumnType.BOOLEAN);
		one.setId("123");
		one.setName("One");
		List<ColumnModel> list = new ArrayList<ColumnModel>(2);
		list.add(one);
		list.add(one);
		List<String> jsons = utils.toJSONList(list);
		assertEquals(list.size(), jsons.size());
		// Back 
		List<ColumnModel> clone = utils.columnModelFromJSON(jsons);
		assertEquals(list, clone);
	}
	
	@Test
	public void testTableBundleRoundTrip() throws JSONObjectAdapterException{
		TableBundle bundle = new TableBundle();
		ColumnModel one = new ColumnModel();
		one.setColumnType(ColumnType.BOOLEAN);
		one.setId("123");
		one.setName("One");
		List<ColumnModel> list = Arrays.asList(one);
		bundle.setColumnModels(list);
		bundle.setMaxRowsPerPage(new Long(4444));
		String json = utils.toJSON(bundle);
		TableBundle clone = utils.tableBundleFromJSON(json);
		assertEquals(bundle, clone);
	}

}
