package org.sagebionetworks.web.client.widget.provenance.nchart;

import com.google.gwt.core.client.JavaScriptObject;

public class NChartCharactersJso extends JavaScriptObject implements NChartCharacters {

	protected NChartCharactersJso() {}

	public final native void addCharacter(String characterId) /*-{
																														this[characterId] = {color:'#ffffff'};
																														}-*/;

}
