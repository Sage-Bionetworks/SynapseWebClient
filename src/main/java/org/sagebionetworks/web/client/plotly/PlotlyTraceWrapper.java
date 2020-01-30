package org.sagebionetworks.web.client.plotly;

import com.google.gwt.core.client.JavaScriptObject;
import jsinterop.annotations.JsIgnore;

public class PlotlyTraceWrapper {
	String[] x, y;
	String name, type;
	boolean isHorizontal = false;

	public PlotlyTraceWrapper() {}

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

	public void setIsHorizontal(boolean isHorizontal) {
		this.isHorizontal = isHorizontal;
	}

	public boolean isHorizontal() {
		return isHorizontal;
	}

	public JavaScriptObject getTrace() {
		String orientationValue = isHorizontal ? "h" : "v";
		String[] xAxis = isHorizontal ? y : x;
		String[] yAxis = isHorizontal ? x : y;
		return _getTrace(name, type, xAxis, yAxis, orientationValue);
	}

	private static native JavaScriptObject _getTrace(String nameValue, String typeValue, String[] xValue, String[] yValue, String orientationValue) /*-{
		return {
			type : typeValue,
			name : nameValue,
			x : xValue,
			y : yValue,
			orientation : orientationValue
		}
	}-*/;

}
