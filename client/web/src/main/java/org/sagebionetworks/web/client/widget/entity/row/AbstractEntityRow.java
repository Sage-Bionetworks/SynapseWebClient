package org.sagebionetworks.web.client.widget.entity.row;

import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.widget.entity.AdapterUtils;
import org.sagebionetworks.web.client.widget.entity.StringValueUtils;

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
	String toolTipsBody;
	String dislplayValue;
	

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

	public String getToolTipsBody() {
		return toolTipsBody;
	}

	public String getDislplayValue() {
		return dislplayValue;
	}
	
	/**
	 * Update the display value
	 * @param newValue
	 */
	public final void updateDisplayValue(Object newValue){
		this.toolTipsBody = StringValueUtils.valueToToolTips(newValue);
		this.dislplayValue = StringValueUtils.valueToString(newValue);
	}


	@Override
	public final void setValue(T newValue) {
		// Set the new value
		setValueInternal(newValue);
		updateDisplayValue(newValue);
	}
	
	public abstract void setValueInternal(T newValue);

}
