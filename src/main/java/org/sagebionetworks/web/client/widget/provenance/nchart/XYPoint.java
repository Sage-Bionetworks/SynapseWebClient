package org.sagebionetworks.web.client.widget.provenance.nchart;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Overlay type for dimension
 * @author dburdick
 *
 */
public class XYPoint extends JavaScriptObject {
	
	protected XYPoint() { }
	
	public final native int getX() /*-{ return this.x }-*/;
	
	public final native int getY() /*-{ return this.y }-*/;
}
