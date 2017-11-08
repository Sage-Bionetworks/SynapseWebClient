package org.sagebionetworks.web.client.plotly;

import com.google.gwt.core.client.JavaScriptObject;

import jsinterop.annotations.JsIgnore;

public class PlotlyTraceWrapper {
	String[] x, y;
	String name, type;
	
	public PlotlyTraceWrapper() {
	}
	public void setType(GraphType newType) {
		type = newType.name().toLowerCase();
	}
	
	@JsIgnore
	public void setX(String[] x) {
		removeNulls(x);
		this.x = x;
	}
	
	public void setY(String[] y) {
		removeNulls(y);
		this.y = y;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
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
	public JavaScriptObject getTrace() {
		return _getTrace(name, type, x, y);
	}


	private static native JavaScriptObject _getTrace(String nameValue, String typeValue, String[] xValue, String[] yValue) /*-{
		return {
			type : typeValue,
			name : nameValue,
			x : xValue,
			y : yValue
		}
	}-*/;

}
