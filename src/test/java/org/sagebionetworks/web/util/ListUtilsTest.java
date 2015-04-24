package org.sagebionetworks.web.util;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.web.client.ClientProperties;

/**
 * 
 * @author jmhill
 *
 */
public class ListUtilsTest {
	
	@Test
	public void testStudySort(){
		
		// Create a few datset
		Folder one = new Folder();
		one.setId(ClientProperties.DEFAULT_PLACE_TOKEN);
		one.setName("beta");
		one.setCreatedOn(new Date(99));
		Folder two = new Folder();
		two.setId("1");
		two.setName("alpha");
		two.setCreatedOn(new Date(98));
		Folder allNull = new Folder();
		// Add them to the list
		
		List<Folder> list = new ArrayList<Folder>();
		list.add(allNull);
		list.add(one);
		list.add(two);
		
		// Now sort the list on name
		List<Folder> sorted = ListUtils.getSortedCopy("name", false, list, Folder.class);
		assertNotNull(sorted);
		System.out.println(sorted);
		
	}
	
	@Test
	public void testMapSort(){
		List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
		String[] columns = new String[]{"col1", "col2", "col3"};
		int numberRows = 3;
		for(int i=0; i<numberRows; i++){
			Map<String, Object> row = new HashMap<String, Object>();
			// Add a column for each column
			for(int col=0; col<columns.length; col++){
				row.put(columns[col], ""+i+"#"+col);
			}
			rows.add(row);
		}
		// Now get a sorted copy
		List<Map<String, Object>> sorted = ListUtils.getSortedCopy(columns[0], false, rows, Map.class);
		assertNotNull(sorted);
		System.out.println(sorted);
	}

}
