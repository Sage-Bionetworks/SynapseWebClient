package org.sagebionetworks.web.client.widget.entity.row;

import java.util.List;

import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.widget.entity.AdapterUtils;

/**
 * Provides a view on a list of string properties
 * 
 * @author jmhill
 *
 */
public class EntityRowListImpl<T> extends AbstractEntityRow<List<T>> implements EntityRowList<T> {

	Class<? extends T> clazz;
	
	/**
	 * 
	 * @param adapter
	 * @param key
	 * @param propertySchema
	 * @param clazz The type of the list.
	 */
	public EntityRowListImpl(JSONObjectAdapter adapter, String key,
			ObjectSchema propertySchema, Class<? extends T> clazz) {
		super(adapter, key, propertySchema);
		if(clazz == null) throw new IllegalArgumentException("Clazz cannot be null");
		this.clazz = clazz;
		// Set the value
		updateDisplayValue(getValue());
	}

	@Override
	public List<T> getValue() {
		return AdapterUtils.getListValue(adapter, propertySchema.getItems().getType(), key, clazz);
	}


	@Override
	public void setValueInternal(List<T> newValue) {
		AdapterUtils.setListValue(adapter, propertySchema.getItems().getType(), key, newValue, clazz);
	}
	
	public Class<? extends T> getListClass(){
		return clazz;
	}

}
