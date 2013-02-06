package org.sagebionetworks.web.client.widget.provenance.nchart;

import com.google.gwt.core.client.JavaScriptObject;

public class NChartCharacters extends JavaScriptObject {

	protected NChartCharacters() { }
	
	public static NChartCharacters newInstance() {
		return (NChartCharacters) JavaScriptObject.createObject();
	}
	
	public final native void addCharacter(String characterId) /*-{
		this[characterId] = {color:'#ffffff'};
	}-*/;

}
