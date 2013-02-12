package org.sagebionetworks.web.client.transform;

import org.sagebionetworks.web.client.widget.provenance.nchart.LayoutResult;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartCharacters;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartLayer;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartLayerNode;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartLayersArray;

/**
 * Provider interface for objects that have implementations that subclass JavaScriptObject.
 * Generally used for JSNI Overlay types.
 * 
 * Class needed as Gin doesn't properly instantiate overlay types
 * @author dburdick
 *
 */
public interface JsoProvider {

	LayoutResult newLayerResult();

	NChartCharacters newNChartCharacters();
	
	NChartLayer newNChartLayer();
	
	NChartLayerNode newNChartLayerNode();

	NChartLayersArray newNChartLayersArray();
	
	//<T> JsoArray<T> newJsArray(List<T> list);
}
