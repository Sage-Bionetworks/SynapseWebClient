package org.sagebionetworks.web.client.widget.entity.row;

import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;

/**
 * Provides a view on a long property
 * @author John
 *
 */
public class EntityRowLong extends AbstractEntityRow<Long> {

	public EntityRowLong(JSONObjectAdapter adapter, String key,
			ObjectSchema propertySchema) {
		super(adapter, key, propertySchema);
	}

	@Override
	public Long getValue() {
		if(adapter.has(key)){
			if(adapter.isNull(key)) return null;
			try {
				return adapter.getLong(key);
			} catch (JSONObjectAdapterException e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}

	@Override
	public void setValue(Long newValue) {
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
