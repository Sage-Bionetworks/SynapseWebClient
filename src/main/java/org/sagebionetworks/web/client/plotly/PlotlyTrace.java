package org.sagebionetworks.web.client.plotly;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType
public class PlotlyTrace {
	
	@JsProperty
	String x[];
	
	@JsProperty
	String y[];
	
	@JsProperty
	String type;
	
	@JsProperty
	String name;
	
	@JsIgnore
	public void setType(GraphType type) {
		this.type = type.name().toLowerCase();
	}
	
	@JsIgnore
	public void setX(String[] x) {
		this.x = x;
		removeNulls(x);
	}
	
	@JsIgnore
	public void setY(String[] y) {
		this.y = y;
		removeNulls(y);
	}
	
	@JsIgnore
	public void setName(String name) {
		this.name = name;
	}
	
	@JsIgnore
	public String getType() {
		return type;
	}
	
	private static final void removeNulls(String[] a) {
		for (int i = 0; i < a.length; i++) {
			if (a[i] == null) {
				a[i] = "";
			}
		}
	}
	public String getName() {
		return name;
	}
	public String[] getX() {
		return x;
	}
	public String[] getY() {
		return y;
	}
}
