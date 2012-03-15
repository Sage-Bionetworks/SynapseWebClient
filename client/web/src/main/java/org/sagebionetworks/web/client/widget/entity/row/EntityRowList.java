package org.sagebionetworks.web.client.widget.entity.row;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.adapter.AdapterCollectionUtils;
import org.sagebionetworks.schema.adapter.JSONArrayAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;

/**
 * Provides a view on a list of string properties
 * 
 * @author jmhill
 *
 */
public class EntityRowList<T> extends AbstractEntityRow<List<T>> {

	Class<? extends T> clazz;
	
	/**
	 * 
	 * @param adapter
	 * @param key
	 * @param propertySchema
	 * @param clazz The type of the list.
	 */
	public EntityRowList(JSONObjectAdapter adapter, String key,
			ObjectSchema propertySchema, Class<? extends T> clazz) {
		super(adapter, key, propertySchema);
		if(clazz == null) throw new IllegalArgumentException("Clazz cannot be null");
		this.clazz = clazz;
	}

	@Override
	public List<T> getValue() {
		if(adapter.has(key)){
			// Nothing to do it null
			if(adapter.isNull(key)) return null;
			try {
				JSONArrayAdapter array = adapter.getJSONArray(key);
				return AdapterCollectionUtils.readListFromArray(array, clazz);
			} catch (JSONObjectAdapterException e) {
				// Convert to runtime
				throw new RuntimeException(e);
			}
		}
		// return an empty list
		return new ArrayList<T>();
		
	}

	@Override
	public void setValue(List<T> newValue) {
		//
		try {
			if(newValue == null){
				adapter.putNull(key);
			}else{
				JSONArrayAdapter array = adapter.createNewArray();
				AdapterCollectionUtils.writeToArray(array, newValue, clazz);
				adapter.put(key, array);
			}
		} catch (JSONObjectAdapterException e) {
			// Convert to runtime
			throw new RuntimeException(e);
		}
		
	}
	
	public Class<? extends T> getListClass(){
		return clazz;
	}

}
