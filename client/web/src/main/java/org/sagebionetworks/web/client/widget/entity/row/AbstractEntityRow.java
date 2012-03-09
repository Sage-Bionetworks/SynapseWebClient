package org.sagebionetworks.web.client.widget.entity.row;

import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;

/**
 * Provides shared support for entity row implementaions
 * @author John
 *
 * @param <T>
 */
public abstract class AbstractEntityRow<T> implements EntityRow<T> {

	JSONObjectAdapter adapter;
	String key;
	ObjectSchema propertySchema;
	

	public AbstractEntityRow(JSONObjectAdapter adapter, String key,
			ObjectSchema propertySchema) {
		this.adapter = adapter;
		this.key = key;
		this.propertySchema = propertySchema;
	}
	
	@Override
	public String getLabel() {
		// Use the title if there is one
		String label = propertySchema.getTitle();
		if(label == null){
			label = "Missing title for: "+propertySchema.getId()+"."+key;
		}
		return label;
	}
	
	@Override
	public String getDescription() {
		return propertySchema.getDescription();
	}

}
