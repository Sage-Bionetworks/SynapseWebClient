package org.sagebionetworks.web.client.widget.entity;

import com.extjs.gxt.ui.client.data.BaseModel;

/**
 * A single value in a combo box.
 * 
 * @author jmhill
 *
 */
public class ComboValue extends BaseModel {
	
	public static String VALUE_KEY = "VALUE_KEY";

	/**
	 * Create a combo value from a string
	 * @param value
	 */
	public ComboValue(String value){
		super();
		set(VALUE_KEY, value);
	}
	
	/**
	 * Get the string value for this combo.
	 * @return
	 */
	public String getValue(){
		return get(VALUE_KEY);
	}
}
