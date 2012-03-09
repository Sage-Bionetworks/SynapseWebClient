package org.sagebionetworks.web.client.widget.entity.row;

import java.util.Date;

import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;

/**
 * Abstraction for any date row.
 * @author John
 *
 */
public abstract class AbstractEntityRowDate extends AbstractEntityRow<Date>{

	public AbstractEntityRowDate(JSONObjectAdapter adapter, String key,
			ObjectSchema propertySchema) {
		super(adapter, key, propertySchema);
	}

}
