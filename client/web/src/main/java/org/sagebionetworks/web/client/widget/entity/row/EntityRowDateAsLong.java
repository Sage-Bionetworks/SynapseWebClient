package org.sagebionetworks.web.client.widget.entity.row;


import java.util.Date;

import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;

/**
 * This is used for a property that is a Date to the user but stored as a long in the adapter.
 * @author John
 *
 */
public class EntityRowDateAsLong extends AbstractEntityRowDate{

	public EntityRowDateAsLong(JSONObjectAdapter adapter, String key,	ObjectSchema propertySchema) {
		super(adapter, key, propertySchema);
	}

	@Override
	public Date getValue() {
		if(adapter.has(key)){
			try {
				// the date is stored as a long in the adapter
				return new Date(adapter.getLong(key));
			} catch (JSONObjectAdapterException e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}

	@Override
	public void setValue(Date newValue) {
		// store the value as a long in the adapter
		try {
			adapter.put(key, newValue.getTime());
		} catch (JSONObjectAdapterException e) {
			throw new RuntimeException(e);
		}
		
	}

}
