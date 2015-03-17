package org.sagebionetworks.web.client.widget.entity.dialog;

public class Annotation {
	ANNOTATION_TYPE type;
	String key, value;
	public Annotation(ANNOTATION_TYPE type, String key, String value) {
		super();
		this.type = type;
		this.key = key;
		this.value = value;
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
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
}
