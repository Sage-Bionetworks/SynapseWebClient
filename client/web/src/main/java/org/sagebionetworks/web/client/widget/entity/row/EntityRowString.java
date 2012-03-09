package org.sagebionetworks.web.client.widget.entity.row;

import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;

/**
 * Provides a view on a string property
 * @author John
 *
 */
public class EntityRowString extends AbstractEntityRow<String>{
	

	/**
	 * Default constructor
	 * @param adapter
	 * @param key
	 * @param propertySchema
	 */
	public EntityRowString(JSONObjectAdapter adapter, String key, ObjectSchema propertySchema) {
		super(adapter, key, propertySchema);
	}

	@Override
	public String getValue() {
		if(adapter.has(key)){
			try {
				return adapter.getString(key);
			} catch (JSONObjectAdapterException e) {
				// Convert these to runtime
				throw new RuntimeException(e);
			}
		}
		// No value.
		return null;
	}

	@Override
	public void setValue(String newValue) {
		// Set the value in the adapter
		try {
			adapter.put(key, newValue);
		} catch (JSONObjectAdapterException e) {
			// Convert these to runtime
			throw new RuntimeException(e);
		}
	}


}
