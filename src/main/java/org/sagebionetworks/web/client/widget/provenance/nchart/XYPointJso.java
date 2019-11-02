package org.sagebionetworks.web.client.widget.provenance.nchart;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Overlay type for dimension
 * 
 * @author dburdick
 *
 */
public class XYPointJso extends JavaScriptObject implements XYPoint {

	protected XYPointJso() {}

	@Override
	public final native int getX() /*-{ return this.x }-*/;

	@Override
	public final native int getY() /*-{ return this.y }-*/;

}
