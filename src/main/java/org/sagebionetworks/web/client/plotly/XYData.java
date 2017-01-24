package org.sagebionetworks.web.client.plotly;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType
public class XYData {
	
	@JsProperty
	int x[];
	
	@JsProperty
	int y[];
	
	@JsProperty
	String type;
	
	@JsIgnore
	public void setType(GraphType type) {
		this.type = type.name().toLowerCase();
	}
	
	@JsIgnore
	public void setX(int[] x) {
		this.x = x;
	}
	
	@JsIgnore
	public void setY(int[] y) {
		this.y = y;
	}
}
