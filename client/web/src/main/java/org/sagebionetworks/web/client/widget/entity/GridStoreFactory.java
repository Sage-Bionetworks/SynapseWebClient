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
	
	public static final int MAX_CHARS_IN_LIST = 50;

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
			Object value = row.getValue();
			String stringValue = valueToString(value);
			String toolTips = valueToToolTips(value);
			EntityRowModel model = new EntityRowModel(row.getLabel(), stringValue, row.getDescription(), toolTips);
			models.add(model);
		}
		ListStore<EntityRowModel> store = new ListStore<EntityRowModel>();
		store.add(models);
		return store;
	}
	
	/**
	 * Convert the value to a short string to be shown in the table.
	 * @param value
	 * @return
	 */
	public static String valueToString(Object value){
		if(value == null) return null;
		if(value instanceof String){
			String stringValue =  (String) value;
			if(stringValue.length() > MAX_CHARS_IN_LIST){
				return stringValue.substring(0,MAX_CHARS_IN_LIST-1);
			}else{
				return stringValue;
			}
		}else if(value instanceof Long){
			return value.toString();
		}else if(value instanceof Double){
			return value.toString();
		}else if(value instanceof Date){
			return value.toString();
		}else if(value instanceof List<?>){
			StringBuilder builder = new StringBuilder();
			List list = (List) value;
			int index =0;
			for(Object child: list){
				if(index > 0){
					builder.append(", ");
				}
				builder.append(valueToString(child));
				if(builder.length() > MAX_CHARS_IN_LIST){
					return builder.toString();
				}
				index++;
			}
			return builder.toString();
		}else{
			throw new IllegalArgumentException("Unknonw type: "+value.toString());
		}
	}
	
	/**
	 * Convert the value to a short string to be shown in the table.
	 * @param value
	 * @return
	 */
	public static String valueToToolTips(Object value){
		if(value == null) return null;
		if(value instanceof String){
			return (String) value;
		}else if(value instanceof Long){
			return value.toString();
		}else if(value instanceof Double){
			return value.toString();
		}else if(value instanceof Date){
			return value.toString();
		}else if(value instanceof List<?>){
			StringBuilder builder = new StringBuilder();
			builder.append("<ul>");
			List list = (List) value;
			for(Object child: list){
				builder.append("<li>");
				builder.append(valueToToolTips(child));
				builder.append("</li>");
			}
			builder.append("</ul>");
			return builder.toString();
		}else{
			throw new IllegalArgumentException("Unknonw type: "+value.toString());
		}
	}
}
