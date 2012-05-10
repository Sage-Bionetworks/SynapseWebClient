package org.sagebionetworks.web.client.widget.entity.row;

import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.widget.entity.AdapterUtils;

/**
 * A row that is backed by a single value (or a scalar).
 * 
 * @author jmhill
 *
 * @param <T>
 */
public class EntityRowScalar<T> extends AbstractEntityRow<T> {
	
	Class<? extends T> clazz;
	
	public EntityRowScalar(JSONObjectAdapter adapter, String key,
			ObjectSchema propertySchema, Class<? extends T> clazz) {
		super(adapter, key, propertySchema);
		this.clazz = clazz;
		// Set the value
		updateDisplayValue(getValue());
	}

	@Override
	public T getValue() {
		// The utils does all the work
		return AdapterUtils.getValue(adapter, propertySchema.getType(), key, clazz);
	}

	@Override
	public void setValueInternal(T newValue) {
		// The utils does all the work.
		AdapterUtils.setValue(adapter, propertySchema.getType(), key, newValue);
		
	}
	
	/**
	 * What it the type of this class
	 * @return
	 */
	public Class<? extends T> getTypeClass(){
		return clazz;
	}

}
