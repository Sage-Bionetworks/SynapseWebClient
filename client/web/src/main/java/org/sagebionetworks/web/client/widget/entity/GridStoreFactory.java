package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
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
			String stringValue = null;
			Object value = row.getValue();
			if(value != null){
				stringValue = value.toString();
			}
			EntityRowModel model = new EntityRowModel(row.getLabel(), stringValue, row.getDescription(), stringValue);
			models.add(model);
		}
		ListStore<EntityRowModel> store = new ListStore<EntityRowModel>();
		store.add(models);
		return store;
	}
}
