package org.sagebionetworks.web.client.widget.entity.row;

import java.util.Date;

import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;

/**
 * This is used for a property that is a Date to the user but stored as a string in the adapter.
 * 
 * @author John
 *
 */
public class EntityRowDateAsString extends AbstractEntityRowDate {

	public EntityRowDateAsString(JSONObjectAdapter adapter, String key,	ObjectSchema propertySchema) {
		super(adapter, key, propertySchema);
	}

	@Override
	public Date getValue() {
		// Dates are stored as strings for this class
		if(adapter.has(key)){
			if(adapter.isNull(key)) return null;
			try {
				String dateString = adapter.getString(key);
				// Convert it to a data
				return adapter.convertStringToDate(propertySchema.getFormat(), dateString);
			} catch (JSONObjectAdapterException e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}

	@Override
	public void setValue(Date newValue) {
		try {
			if(newValue == null){
				adapter.putNull(key);
			}else{
				// This value is stored as a string
				String dataString = adapter.convertDateToString(propertySchema.getFormat(), newValue);
				adapter.put(key, dataString);
			}
		} catch (JSONObjectAdapterException e) {
			throw new RuntimeException(e);
		}
	}


}
