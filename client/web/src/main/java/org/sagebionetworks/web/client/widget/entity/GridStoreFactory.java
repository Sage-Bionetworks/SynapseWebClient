package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.sagebionetworks.web.client.widget.entity.row.EntityRow;
import org.sagebionetworks.web.client.widget.entity.row.EntityRowModel;

import com.extjs.gxt.ui.client.store.ListStore;

/**
 * Factory for creating a Grid ListStore from List<EntityRow<?>>
 * @author John
 *
 */
public class GridStoreFactory {
	
	

	/**
	 * Create a read-only list store form a list of entityRows.
	 * @param rows
	 * @return
	 */
	public static ListStore<EntityRowModel> createListStore(List<EntityRow<?>> rows){
		List<EntityRowModel> models = new ArrayList<EntityRowModel>();
		// Populate the list
		for(EntityRow<?> row: rows){
			// This is the value that is shown in the table.
			EntityRowModel model = new EntityRowModel(row);
			models.add(model);
		}
		ListStore<EntityRowModel> store = new ListStore<EntityRowModel>();
		store.add(models);
		return store;
	}
	

}
