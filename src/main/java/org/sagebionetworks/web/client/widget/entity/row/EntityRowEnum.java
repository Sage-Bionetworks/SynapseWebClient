package org.sagebionetworks.web.client.widget.entity.row;

import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;

/**
 * This is a string constrained by an enumeration of values.
 * @author jmhill
 *
 */
public class EntityRowEnum extends EntityRowScalar<String> {

	public EntityRowEnum(JSONObjectAdapter adapter, String key,
			ObjectSchema propertySchema) {
		super(adapter, key, propertySchema, String.class);
	}
	
	/**
	 * The possible values for an enumeration.
	 * @return
	 */
	public String[] getEnumValues(){
		return propertySchema.getEnum();
	}

}
