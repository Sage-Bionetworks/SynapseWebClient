package org.sagebionetworks.web.client.plotly;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType
public class PlotlyTrace {
	
	@JsProperty
	String x[];
	
	@JsProperty
	double y[];
	
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
	}
	
	@JsIgnore
	public void setY(double[] y) {
		this.y = y;
	}
	
	@JsIgnore
	public void setName(String name) {
		this.name = name;
	}
}
