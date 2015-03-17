package org.sagebionetworks.web.client.widget.entity.dialog;

import java.util.List;

public class Annotation {
	ANNOTATION_TYPE type;
	String key;
	List<String> values;
	public Annotation(ANNOTATION_TYPE type, String key, List<String> values) {
		super();
		this.type = type;
		this.key = key;
		this.values = values;
	}
	public ANNOTATION_TYPE getType() {
		return type;
	}
	public void setType(ANNOTATION_TYPE type) {
		this.type = type;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	
	public List<String> getValues() {
		return values;
	}
	public void setValues(List<String> values) {
		this.values = values;
	}
	
	public String getValuesString() {
		StringBuilder builder = new StringBuilder();
		boolean isAfterFirst = false;
		for(Object value: values){
			if(isAfterFirst){
				builder.append(", ");
			}
			isAfterFirst = true;
			builder.append(value);
		}
		return builder.toString();
	}
	
}
