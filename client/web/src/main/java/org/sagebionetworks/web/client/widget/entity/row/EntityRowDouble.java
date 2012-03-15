package org.sagebionetworks.web.client.widget.entity.row;

import java.util.Date;

import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;

/**
 * Provides a view on a double property
 * @author John
 *
 */
public class EntityRowDouble extends AbstractEntityRow<Double>{

	public EntityRowDouble(JSONObjectAdapter adapter, String key,
			ObjectSchema propertySchema) {
		super(adapter, key, propertySchema);
	}

	@Override
	public Double getValue() {
		if(adapter.has(key)){
			if(adapter.isNull(key)) return null;
			try {
				// stored as a long
				return adapter.getDouble(key);
			} catch (JSONObjectAdapterException e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}

	@Override
	public void setValue(Double newValue) {
		try {
			if(newValue == null){
				adapter.putNull(key);
			}else{
				adapter.put(key, newValue);
			}
		} catch (JSONObjectAdapterException e) {
			throw new RuntimeException(e);
		}
	}

}
